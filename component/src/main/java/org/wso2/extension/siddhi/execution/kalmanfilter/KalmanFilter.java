/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.execution.kalmanfilter;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.HashMap;
import java.util.Map;


/**
 * kalmanFilter(measuredValue)
 * kalmanFilter(measuredValue, measurementNoiseSD)
 * kalmanFilter(measuredValue, measuredChangingRate, measurementNoiseSD, timestamp)
 * <p>
 * These methods estimate values for noisy data.
 * <p>
 * measuredValue - measured value eg:40.695881
 * measuredChangingRate - Changing rate. eg: Velocity of the point which describes from measured value
 * - 0.003d meters per second
 * measurementNoiseSD - standard deviation of the noise. eg: 0.01
 * timestamp - the timestamp at the measured time eg: 1445234861l
 * <p>
 * Accept Type(s) for kalmanFilter(measuredValue);
 * measuredValue : DOUBLE
 * <p>
 * Accept Type(s) for kalmanFilter(measuredValue, measurementNoiseSD);
 * measuredValue : DOUBLE
 * measurementNoiseSD : DOUBLE
 * <p>
 * Accept Type(s) for kalmanFilter(measuredValue, measuredChangingRate, measurementNoiseSD, timestamp);
 * measuredValue : DOUBLE
 * measuredChangingRate : DOUBLE
 * measurementNoiseSD : DOUBLE
 * timestamp : LONG
 * <p>
 * Return Type(s): DOUBLE
 */


@Extension(name = "kalmanFilter", namespace = "kf", description = " Kalman filter",
        examples = {
                @Example(syntax =
                        "from cleanedStream " +
                        "select kf:kalmanFilter(latitude) as kalmanEstimatedValue " +
                        "insert into dataOut;",
                        description = "Calculated the Kalman filter")},
        returnAttributes = {
                @ReturnAttribute(
                        description = "Return the function calculated value." ,
                        type = {DataType.DOUBLE})}
)
/**
 * Http source for receive the http and https request.
 */
public class KalmanFilter extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.DOUBLE;
    //for static kalman filter
    private double transition; //A
    private double measurementNoiseSD; //standard deviation of the measurement noise
    private double prevEstimatedValue; //to remain as the initial state
    private double variance; //P
    //for dynamic kalman filter
    private RealMatrix measurementMatrixH = null;
    private RealMatrix varianceMatrixP;
    private RealMatrix prevMeasuredMatrix;
    private long prevTimestamp;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }


    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> map = new HashMap<>();
        map.put(KalmanFilterConstants.TRANSITION, transition);
        map.put(KalmanFilterConstants.MEASUREMENT_NOISE_DS, measurementNoiseSD);
        map.put(KalmanFilterConstants.PRE_ESTIMATED_VALUE, prevEstimatedValue);
        map.put(KalmanFilterConstants.VARIENCE, variance);
        map.put(KalmanFilterConstants.MEASUREMENT_MATRIX, measurementMatrixH);
        map.put(KalmanFilterConstants.VARIENCE_MATRIX, varianceMatrixP);
        map.put(KalmanFilterConstants.PREV_MEASURED_MATRIX, prevMeasuredMatrix);
        map.put(KalmanFilterConstants.PREV_TIMESTAMP, prevTimestamp);
        return map;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        transition = (double) map.get(KalmanFilterConstants.TRANSITION);
        measurementNoiseSD = (double) map.get(KalmanFilterConstants.MEASUREMENT_NOISE_DS);
        prevEstimatedValue = (double) map.get(KalmanFilterConstants.PRE_ESTIMATED_VALUE);
        variance = (double) map.get(KalmanFilterConstants.VARIENCE);
        measurementMatrixH = (RealMatrix) map.get(KalmanFilterConstants.MEASUREMENT_MATRIX);
        varianceMatrixP = (RealMatrix) map.get(KalmanFilterConstants.VARIENCE_MATRIX);
        prevMeasuredMatrix = (RealMatrix) map.get(KalmanFilterConstants.PREV_MEASURED_MATRIX);
        prevTimestamp = (long) map.get(KalmanFilterConstants.PREV_TIMESTAMP);
    }

    @Override
    protected void init(ExpressionExecutor[] expressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 1 && attributeExpressionExecutors.length != 2 &&
                attributeExpressionExecutors.length != 4) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to kf:kalmanFilter() function," +
                    " required 1, 2 or 4, but found " + attributeExpressionExecutors.length);
        } else {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE) {
                throw new SiddhiAppValidationException("Invalid parameter type found for the first argument " +
                        "of kf:kalmanFilter() function, required " +
                        Attribute.Type.DOUBLE + ", but found " +
                        attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors.length == 2 || attributeExpressionExecutors.length == 4) {
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.DOUBLE) {
                    throw new SiddhiAppValidationException("Invalid parameter type found for the second argument " +
                            "of kf:kalmanFilter() function, required " +
                            Attribute.Type.DOUBLE + ", but found " +
                            attributeExpressionExecutors[1].getReturnType().toString());
                }
            }
            if (attributeExpressionExecutors.length == 4) {
                if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.DOUBLE) {
                    throw new SiddhiAppValidationException("Invalid parameter type found for the third argument " +
                            "of kf:kalmanFilter() function, required " +
                            Attribute.Type.DOUBLE + ", but found " +
                            attributeExpressionExecutors[1].getReturnType().toString());
                }
                if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.LONG) {
                    throw new SiddhiAppValidationException("Invalid parameter type found for the fourth argument " +
                            "of kf:kalmanFilter() function, required " +
                            Attribute.Type.LONG + ", but found " +
                            attributeExpressionExecutors[1].getReturnType().toString());
                }
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                    "function. First argument should be a double");
        }
        if (data[1] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                    "function. Second argument should be a double");
        }
        if (data.length == 2) {
            double measuredValue = (Double) data[0]; //to remain as the initial state
            if (prevEstimatedValue == 0) {
                transition = 1;
                variance = 1000;
                measurementNoiseSD = (Double) data[1];
                prevEstimatedValue = measuredValue;
            }
            prevEstimatedValue = transition * prevEstimatedValue;
            double kalmanGain = variance / (variance + measurementNoiseSD);
            prevEstimatedValue = prevEstimatedValue + kalmanGain * (measuredValue - prevEstimatedValue);
            variance = (1 - kalmanGain) * variance;
            return prevEstimatedValue;
        } else {
            if (data[2] == null) {
                throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                        "function. Third argument should be a double");
            }
            if (data[3] == null) {
                throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                        "function. Fourth argument should be a long");
            }

            double measuredXValue = (Double) data[0];
            double measuredChangingRate = (Double) data[1];
            double measurementNoiseSD = (Double) data[2];
            long timestamp = (Long) data[3];
            long timestampDiff;
            double[][] measuredValues = {{measuredXValue}, {measuredChangingRate}};

            if (measurementMatrixH == null) {
                timestampDiff = 1;
                double[][] varianceValues = {{1000, 0}, {0, 1000}};
                double[][] measurementValues = {{1, 0}, {0, 1}};
                measurementMatrixH = MatrixUtils.createRealMatrix(measurementValues);
                varianceMatrixP = MatrixUtils.createRealMatrix(varianceValues);
                prevMeasuredMatrix = MatrixUtils.createRealMatrix(measuredValues);
            } else {
                timestampDiff = (timestamp - prevTimestamp);
            }
            double[][] rValues = {{measurementNoiseSD, 0}, {0, measurementNoiseSD}};
            RealMatrix rMatrix = MatrixUtils.createRealMatrix(rValues);
            double[][] transitionValues = {{1d, timestampDiff}, {0d, 1d}};
            RealMatrix transitionMatrixA = MatrixUtils.createRealMatrix(transitionValues);
            RealMatrix measuredMatrixX = MatrixUtils.createRealMatrix(measuredValues);

            //Xk = (A * Xk-1)
            prevMeasuredMatrix = transitionMatrixA.multiply(prevMeasuredMatrix);

            //Pk = (A * P * AT) + Q
            varianceMatrixP = (transitionMatrixA.multiply(varianceMatrixP)).multiply(transitionMatrixA.transpose());

            //sMat = (H * P * HT) + R
            RealMatrix sMat = ((measurementMatrixH.multiply(varianceMatrixP)).multiply(measurementMatrixH.transpose()))
                    .add(rMatrix);
            RealMatrix s1Mat = new LUDecomposition(sMat).getSolver().getInverse();

            //P * HT * sMat-1
            RealMatrix kalmanGainMatrix = (varianceMatrixP.multiply(measurementMatrixH.transpose())).multiply(s1Mat);

            //Xk = Xk + kalmanGainMatrix (Zk - HkXk )
            prevMeasuredMatrix = prevMeasuredMatrix.add(kalmanGainMatrix.multiply(
                    (measuredMatrixX.subtract(measurementMatrixH.multiply(prevMeasuredMatrix)))));

            //Pk = Pk - K.Hk.Pk
            varianceMatrixP = varianceMatrixP.subtract(
                    (kalmanGainMatrix.multiply(measurementMatrixH)).multiply(varianceMatrixP));

            prevTimestamp = timestamp;
            return prevMeasuredMatrix.getRow(0)[0];
        }
    }

    @Override
    protected Object execute(Object data) {
        if (data == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                    "function. Argument should be a double");
        }
        double measuredValue = (Double) data; //to remain as the initial state
        if (transition == 0) {
            transition = 1;
            variance = 1000;
            measurementNoiseSD = 0.001d;
            prevEstimatedValue = measuredValue;
        }
        prevEstimatedValue = transition * prevEstimatedValue;
        double kalmanGain = variance / (variance + measurementNoiseSD);
        prevEstimatedValue = prevEstimatedValue + kalmanGain * (measuredValue - prevEstimatedValue);
        variance = (1 - kalmanGain) * variance;
        return prevEstimatedValue;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

}
