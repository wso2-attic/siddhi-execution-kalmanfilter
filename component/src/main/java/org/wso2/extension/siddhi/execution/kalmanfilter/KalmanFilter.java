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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.executor.function.FunctionExecutor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

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


@Extension(name = "kalmanFilter", namespace = "kf", description = " This extension provides Kalman filtering " +
        "capabilities to Siddhi. This allows you to detect outliers of input data. This function uses " +
        "measurements observed over time containing noise and other inaccuracies, and produces estimated " +
        "values for the current measurement using the Kalman algorithm.",
        parameters = {
        @Parameter(
                name = "measured.value",
                description = "The sequential change in the observed measurement.",
                type = DataType.DOUBLE),
        @Parameter(
                name = "measured.changing.rate",
                description = "The rate at which the measured change is taking place.",
                type = DataType.DOUBLE),
        @Parameter(
                name = "measurement.noise.sd",
                description = "The standard deviation of the noise.",
                type = DataType.DOUBLE),
        @Parameter(
                name = "timestamp",
                description = "The time stamp of the time at which the measurement was carried out.",
                type = DataType.LONG)},
        examples = {
                @Example(syntax =
                        "from cleanedStream " +
                        "\nselect kf:kalmanFilter(latitude) as kalmanEstimatedValue " +
                        "\ninsert into dataOut;",
                        description = "This function produces estimated values for the current measurement using the " +
                                "Kalman algorithm. In order to do this, it is assumed that the current " +
                                "measurement is a static value. The lattitude is a double value indicated by the" +
                                " `measuredValue`. " +
                                " e.g., 40.695881" +
                                "\nEx:\t\n\n" +
                                "\t1st round: kf:kalmanFilter(-74.178444) returns an estimated value of -74.178444.\n" +
                                "\t2nd round: kf:kalmanFilter(-74.175703) returns an estimated value of " +
                                "-74.1770735006853.\n" +
                                "\t3rd round: kf:kalmanFilter(-74.177872) returns an estimated value of  " +
                                "-74.1773396670348."),
                @Example(syntax =
                                "from cleanedStream " +
                                "\nselect kf:kalmanFilter(latitude, noisesd) as kalmanEstimatedValue " +
                                "\ninsert into dataOut;",
                        description = "This function produces estimated values for the current measurement using the" +
                                " Kalman algorithm. In order to do this, it is assumed that the current measurement" +
                                " is a static value, and the distributed standard deviation is considered as the " +
                                "standard deviation of noise. The standard deviation of noise is a double value as " +
                                "indicated by the `measurementNoiseSD` parameter." +
                                " e.g., 0.01" +
                                "\nEx: \t\n\n" +
                                "\t1st round: kf:kalmanFilter(-74.178444, 0.003) returns an estimated value" +
                                " of -74.178444.\n" +
                                "\t2nd round: kf:kalmanFilter(-74.175703, 0.003) returns an estimated value of " +
                                "-74.17707350205573.\n" +
                                "\t3rd round: kf:kalmanFilter(-74.177872, 0.003) returns an estimated value of " +
                                " -74.177339667771."),
                @Example(syntax =
                                "from cleanedStream " +
                                "\nselect kf:kalmanFilter(latitude, measuredchangingrate, noisesd, timestamp) as " +
                                "kalmanEstimatedValue " +
                                "\ninsert into dataOut;",
                        description = "This function produces estimated values for the current measurement using " +
                                "the Kalman algorithm. In order to do this, it is assumed that the current " +
                                "measurement is a dynamic value that can be changed with the given value. The " +
                                "`timestamp` is a long value and it indicates the time at which the measurement is " +
                                "carried out." +
                                "\nEx:\t\n\n" +
                                "\t1st round: kf:kalmanFilter(-74.178444, 0.003, 0.01, time:" +
                                "timestampInMilliseconds() ) returns an estimated value of -74.1784439700006.\n" +
                                "\t2nd round: kf:kalmanFilter(-74.178444, 0.003, 0.01, time:" +
                                "timestampInMilliseconds() ) returns an estimated value of -74.1784439700006.\n" +
                                "\t3rd round: kf:kalmanFilter(-74.177872, 0.003, 0.01, time:" +
                                "timestampInMilliseconds()) returns an estimated value of  -74.17697314316393.")
        },
        returnAttributes = {
                @ReturnAttribute(
                        description = "Return the function calculated value." ,
                        type = {DataType.DOUBLE})}
)
/**
 * Http source for receive the http and https request.
 */
public class KalmanFilter extends FunctionExecutor<KalmanFilter.ExtensionState> {

    private Attribute.Type returnType = Attribute.Type.DOUBLE;


    @Override
    protected StateFactory<ExtensionState> init(ExpressionExecutor[] attributeExpressionExecutors,
                                                ConfigReader configReader, SiddhiQueryContext siddhiQueryContext) {
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
        return () -> new ExtensionState();
    }

    @Override
    protected Object execute(Object[] data, ExtensionState state) {
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
            if (state.prevEstimatedValue == 0) {
                state.transition = 1;
                state.variance = 1000;
                state.measurementNoiseSD = (Double) data[1];
                state.prevEstimatedValue = measuredValue;
            }
            state.prevEstimatedValue = state.transition * state.prevEstimatedValue;
            double kalmanGain = state.variance / (state.variance + state.measurementNoiseSD);
            state.prevEstimatedValue = state.prevEstimatedValue + kalmanGain * (measuredValue -
                    state.prevEstimatedValue);
            state.variance = (1 - kalmanGain) * state.variance;
            return state.prevEstimatedValue;
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

            if (state.measurementMatrixH == null) {
                timestampDiff = 1;
                double[][] varianceValues = {{1000, 0}, {0, 1000}};
                double[][] measurementValues = {{1, 0}, {0, 1}};
                state.measurementMatrixH = MatrixUtils.createRealMatrix(measurementValues);
                state.varianceMatrixP = MatrixUtils.createRealMatrix(varianceValues);
                state.prevMeasuredMatrix = MatrixUtils.createRealMatrix(measuredValues);
            } else {
                timestampDiff = (timestamp - state.prevTimestamp);
            }
            double[][] rValues = {{measurementNoiseSD, 0}, {0, measurementNoiseSD}};
            RealMatrix rMatrix = MatrixUtils.createRealMatrix(rValues);
            double[][] transitionValues = {{1d, timestampDiff}, {0d, 1d}};
            RealMatrix transitionMatrixA = MatrixUtils.createRealMatrix(transitionValues);
            RealMatrix measuredMatrixX = MatrixUtils.createRealMatrix(measuredValues);

            //Xk = (A * Xk-1)
            state.prevMeasuredMatrix = transitionMatrixA.multiply(state.prevMeasuredMatrix);

            //Pk = (A * P * AT) + Q
            state.varianceMatrixP = (transitionMatrixA.multiply(state.varianceMatrixP)).multiply(
                    transitionMatrixA.transpose());

            //sMat = (H * P * HT) + R
            RealMatrix sMat = ((state.measurementMatrixH.multiply(state.varianceMatrixP)).multiply(
                    state.measurementMatrixH.transpose()))
                    .add(rMatrix);
            RealMatrix s1Mat = new LUDecomposition(sMat).getSolver().getInverse();

            //P * HT * sMat-1
            RealMatrix kalmanGainMatrix = (state.varianceMatrixP.multiply(state.measurementMatrixH.transpose())).
                    multiply(s1Mat);

            //Xk = Xk + kalmanGainMatrix (Zk - HkXk )
            state.prevMeasuredMatrix = state.prevMeasuredMatrix.add(kalmanGainMatrix.multiply(
                    (measuredMatrixX.subtract(state.measurementMatrixH.multiply(state.prevMeasuredMatrix)))));

            //Pk = Pk - K.Hk.Pk
            state.varianceMatrixP = state.varianceMatrixP.subtract(
                    (kalmanGainMatrix.multiply(state.measurementMatrixH)).multiply(state.varianceMatrixP));

            state.prevTimestamp = timestamp;
            return state.prevMeasuredMatrix.getRow(0)[0];
        }
    }

    @Override
    protected Object execute(Object data, ExtensionState state) {
        if (data == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to kf:kalmanFilter() " +
                    "function. Argument should be a double");
        }
        double measuredValue = (Double) data; //to remain as the initial state
        if (state.transition == 0) {
            state.transition = 1;
            state.variance = 1000;
            state.measurementNoiseSD = 0.001d;
            state.prevEstimatedValue = measuredValue;
        }
        state.prevEstimatedValue = state.transition * state.prevEstimatedValue;
        double kalmanGain = state.variance / (state.variance + state.measurementNoiseSD);
        state.prevEstimatedValue = state.prevEstimatedValue + kalmanGain * (measuredValue - state.prevEstimatedValue);
        state.variance = (1 - kalmanGain) * state.variance;
        return state.prevEstimatedValue;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    static class ExtensionState extends State {
        //for static kalman filter
        private  double transition; //A
        private  double measurementNoiseSD; //standard deviation of the measurement noise
        private  double prevEstimatedValue; //to remain as the initial state
        private  double variance; //P
        //for dynamic kalman filter
        private  RealMatrix measurementMatrixH = null;
        private  RealMatrix varianceMatrixP;
        private  RealMatrix prevMeasuredMatrix;
        private  long prevTimestamp;

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Map<String, Object> snapshot() {
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
        public void restore(Map<String, Object> map) {
            transition = (double) map.get(KalmanFilterConstants.TRANSITION);
            measurementNoiseSD = (double) map.get(KalmanFilterConstants.MEASUREMENT_NOISE_DS);
            prevEstimatedValue = (double) map.get(KalmanFilterConstants.PRE_ESTIMATED_VALUE);
            variance = (double) map.get(KalmanFilterConstants.VARIENCE);
            measurementMatrixH = (RealMatrix) map.get(KalmanFilterConstants.MEASUREMENT_MATRIX);
            varianceMatrixP = (RealMatrix) map.get(KalmanFilterConstants.VARIENCE_MATRIX);
            prevMeasuredMatrix = (RealMatrix) map.get(KalmanFilterConstants.PREV_MEASURED_MATRIX);
            prevTimestamp = (long) map.get(KalmanFilterConstants.PREV_TIMESTAMP);
        }
    }
}
