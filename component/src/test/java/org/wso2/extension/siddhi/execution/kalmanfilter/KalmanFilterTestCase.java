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

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test case for KalmanFilter.
 */
public class KalmanFilterTestCase {
    private static final Logger log = Logger.getLogger(KalmanFilterTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @org.testng.annotations.Test
    public void testStaticKalmanFilter() throws InterruptedException {
        log.info("testStaticKalmanFilter TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "" +
                        "define stream cleanedStream (latitude double, changingRate double, measurementNoiseSD " +
                        "double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream " +
                        "select kf:kalmanFilter(latitude) as kalmanEstimatedValue " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.getAndIncrement();
                    if (count.get() == 1) {
                        Assert.assertEquals(-74.178444d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 2) {
                        Assert.assertEquals(-74.178158000143d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 3) {
                        Assert.assertEquals(-74.1773396670348d, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{-74.178444, 0.003, 0.01d, 1445234861L});
        inputHandler.send(new Object[]{-74.177872, 0.003, 0.01d, 1445234864L});
        inputHandler.send(new Object[]{-74.175703, 0.003, 0.01d, 1445234867L});
        SiddhiTestHelper.waitForEvents(waitTime, 1, count, timeout);
        Assert.assertEquals(count.get(), 3);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testStaticKalmanFilter2() throws InterruptedException {
        log.info("testStaticKalmanFilter with standard deviation for noise TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "" +
                        "define stream cleanedStream (latitude double, changingRate double, measurementNoiseSD " +
                        "double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream " +
                        "select kf:kalmanFilter(latitude, measurementNoiseSD) as kalmanEstimatedValue " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.getAndIncrement();
                    if (count.get() == 1) {
                        Assert.assertEquals(-74.178444d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 2) {
                        Assert.assertEquals(-74.17815800142999d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 3) {
                        Assert.assertEquals(-74.17733967034776d, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{-74.178444, 0.003, 0.01d, 1445234861L});
        inputHandler.send(new Object[]{-74.177872, 0.003, 0.01d, 1445234864L});
        inputHandler.send(new Object[]{-74.175703, 0.003, 0.01d, 1445234867L});
        SiddhiTestHelper.waitForEvents(waitTime, 1, count, timeout);

        Assert.assertEquals(count.get(), 3);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDynamicKalmanFilter() throws InterruptedException {
        log.info("testDynamicKalmanFilter TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "" +
                        "define stream cleanedStream (latitude double, changingRate double, measurementNoiseSD " +
                        "double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream " +
                        "select kf:kalmanFilter(latitude, changingRate, measurementNoiseSD, timestamp) as " +
                        "kalmanEstimatedValue " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.getAndIncrement();
                    if (count.get() == 1) {
                        Assert.assertEquals(-74.1784439700006d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 2) {
                        Assert.assertEquals(-74.17657538193608d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 3) {
                        Assert.assertEquals(-74.17487924016262d, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{-74.178444, 0.003, 0.01d, 1445234861L});
        inputHandler.send(new Object[]{-74.177872, 0.003, 0.01d, 1445234864L});
        inputHandler.send(new Object[]{-74.175703, 0.003, 0.01d, 1445234867L});
        SiddhiTestHelper.waitForEvents(waitTime, 1, count, timeout);

        Assert.assertEquals(count.get(), 3);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDynamicKalmanFilter2() throws InterruptedException {
        log.info("testDynamicKalmanFilter2 TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "" +
                        "define stream cleanedStream (latitude double, changingRate double, measurementNoiseSD " +
                        "double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream " +
                        "select kf:kalmanFilter(latitude, changingRate, measurementNoiseSD, timestamp) as " +
                        "kalmanEstimatedValue " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.getAndIncrement();
                    if (count.get() == 1) {
                        Assert.assertEquals(40.6958810299994d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 2) {
                        Assert.assertEquals(40.69711415696983d, event.getData(0));
                        eventArrived = true;
                    } else if (count.get() == 3) {
                        Assert.assertEquals(40.69632380976617d, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{40.695881, 0.003, 0.01d, 1445234861L});
        inputHandler.send(new Object[]{40.695702, 0.003, 0.01d, 1445234864L});
        inputHandler.send(new Object[]{40.694852999999995, 0.003, 0.01d, 1445234867L});
        SiddhiTestHelper.waitForEvents(waitTime, 1, count, timeout);

        Assert.assertEquals(count.get(), 3);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
