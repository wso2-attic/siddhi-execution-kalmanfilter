# siddhi-execution-kalman-filter
======================================
---
##### New version of Siddhi v4.0.0 is built in Java 8.
##### Latest Released Version v4.0.0-m15.

This extension provides Kalman filtering capabilities to Siddhi. This allows you to detect outliers of input data. 
Following are the functions of the Kalman Filter extension.

Features Supported
------------------
This function uses measurements observed over time containing noise and other inaccuracies, and produces estimated values for the current measurement using Kalman algorithms. The parameters used are as follows.

 - measuredValue : The sequential change in the observed measurement. e.g., 40.695881
 - measuredChangingRate : The rate at which the measured change is taking place. e.g., The velocity with which the measured value is changed can be 0.003 meters per second.
 - measurementNoiseSD : The standard deviation of the noise. e.g., 0.01
 - timestamp : The time stamp of the time at which the measurement was carried out.
 
Prerequisites for using the feature
------------------
  - Siddhi Stream should be defined
  
Deploying the feature
------------------
   Feature can be deploy as a OSGI bundle by putting jar file of component to DAS_HOME/lib directory of DAS 4.0.0 pack. 
   
Example Siddhi Queries
------------------
      - from cleanedStream
        select kf:kalmanFilter(latitude) as kalmanEstimatedValue
        insert into dataOut;
    
      - from cleanedStream
        select kf:kalmanFilter(latitude, noisesd) as kalmanEstimatedValue
        insert into dataOut;
    
      - from cleanedStream
        select kf:kalmanFilter(latitude, measuredchangingrate, noisesd, timestamp) as kalmanEstimatedValue
        insert into dataOut;
   
How to Contribute
------------------
   * Send your bug fixes pull requests to [master branch] (https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/tree/master) 
   
Contact us 
------------------
   Siddhi developers can be contacted via the mailing lists:
     * Carbon Developers List : dev@wso2.org
     * Carbon Architecture List : architecture@wso2.org
   
We welcome your feedback and contribution.
------------------