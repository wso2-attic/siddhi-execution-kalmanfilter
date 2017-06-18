package org.wso2.extension.siddhi.execution.kalmanfilter;

/**
 *Kalman filter Constants.
 */
public class KalmanFilterConstants {
    private KalmanFilterConstants() {
    }

    public static final String TRANSITION = "transition";
    public static final String MEASUREMENT_NOISE_DS = "measurementNoiseSD";
    public static final String PRE_ESTIMATED_VALUE = "prevEstimatedValue";
    public static final String VARIENCE = "variance";
    public static final String MEASUREMENT_MATRIX = "measurementMatrixH";
    public static final String VARIENCE_MATRIX = "varianceMatrixP";
    public static final String PREV_MEASURED_MATRIX = "prevMeasuredMatrix";
    public static final String PREV_TIMESTAMP = "prevTimestamp";
}
