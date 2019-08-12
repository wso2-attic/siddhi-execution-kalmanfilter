siddhi-execution-kalman-filter
======================================

The **siddhi-execution-kalman-filter extension** is an extension to <a target="_blank" href="https://wso2.github
.io/siddhi">Siddhi</a> provides that Kalman filtering capabilities to Siddhi. This allows you to detect outliers of 
input data. 
                                                                                                                                     

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/issues">Issue tracker</a>

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-kalman-filter/api/5.0.1">5.0.1</a>.

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support. 

* This extension is shipped by default with WSO2 Stream Processor, if you wish to use an alternative version of this extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.execution.kalmanfilter</groupId>
        <artifactId>siddhi-execution-kalman-filter</artifactId>
        <version>x.x.x</version>
     </dependency>
```

## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-execution-kalmanfilter/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-execution-kalmanfilter/) |

---

## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-kalman-filter/api/5.0.1/#kalmanfilter-function">kalmanFilter</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#function">Function</a>)*<br> <div style="padding-left: 1em;"><p> This extension provides Kalman filtering capabilities to Siddhi. This allows you to detect outliers of input data. This function uses measurements observed over time containing noise and other inaccuracies, and produces estimated values for the current measurement using the Kalman algorithm.</p></div>

## How to Contribute
 
  * Please report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-kalmanfilter/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to ensuring support for this extension in production. Our kalman filter approach ensures that all 
support leverages our open development methodology and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this kalman filter opportunity contact us via <a target="_blank" 
href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 
