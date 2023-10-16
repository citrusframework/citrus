/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.junit.spring;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusSpringContextProvider;
import org.citrusframework.junit.TestSuiteState;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Test execution listener that will load a test suite instance from the application context and execute
 * tasks before and after the test run. The tasks after are executed as ShutdownHook thread that is added
 * to the JVM runtime.
 *
 * @author Christoph Deppisch
 */
public class TestSuiteExecutionListener extends AbstractTestExecutionListener {

    /** Test suite name */
    private static final String SUITE_NAME = "citrus-junit4-suite";

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        if (TestSuiteState.shouldExecuteBeforeSuite()) {
            ApplicationContext ctx = testContext.getApplicationContext();

            Citrus citrus = Citrus.newInstance(new CitrusSpringContextProvider(ctx));
            citrus.beforeSuite(SUITE_NAME);

            Runtime.getRuntime().addShutdownHook(new Thread(new AfterSuiteShutdownHook(citrus)));
        }
    }

    /**
     * Shutdown hook runnable gets executed during JVM shutdown.
     * This is our only chance to provide after suite logic when using JUnit. After
     * all tests are executed this after suite logic get executed before processing ends.
     */
    private static class AfterSuiteShutdownHook implements Runnable {
        /** Citrus instance */
        private final Citrus citrus;

        /**
         * Default constructor using citrus instance.
         */
        public AfterSuiteShutdownHook(Citrus citrus) {
            this.citrus = citrus;
        }

        @Override
        public void run() {
            citrus.afterSuite(SUITE_NAME);
        }
    }
}
