/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.junit;

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Test execution listener that will load a test suite instance from the application context and execute
 * tasks before and after the test run. The tasks after are executed as ShutdownHook thread that is added
 * to the JVM runtime.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        if (TestSuiteState.shouldExecuteBeforeSuite()) {
            ApplicationContext ctx = testContext.getApplicationContext();

            List<SequenceBeforeSuite> beforeSuite = CollectionUtils.arrayToList(ctx.getBeansOfType(SequenceBeforeSuite.class).values().toArray());

            com.consol.citrus.context.TestContext context = ctx.getBean(com.consol.citrus.context.TestContext.class);
            TestSuiteListeners testSuiteListener = ctx.getBean(TestSuiteListeners.class);

            if (!beforeSuite.isEmpty()) {
                for (SequenceBeforeSuite sequenceBeforeSuite : beforeSuite) {
                    try {
                        if (sequenceBeforeSuite.shouldExecute(testContext.getTestInstance().getClass().getPackage().getName(), null)) {
                            sequenceBeforeSuite.execute(context);
                        }
                    } catch (Exception e) {
                        throw new CitrusRuntimeException("Before suite failed with errors", e);
                    }
                }
            }  else {
                testSuiteListener.onStart();
                testSuiteListener.onStartSuccess();
            }

            List<SequenceAfterSuite> afterSuite = CollectionUtils.arrayToList(ctx.getBeansOfType(SequenceAfterSuite.class).values().toArray());
            Runtime.getRuntime().addShutdownHook(new Thread(new AfterSuiteShutdownHook(testContext.getTestInstance().getClass().getPackage().getName(), afterSuite, context, testSuiteListener)));
        }
    }
    
    /**
     * Shutdown hook runnable gets executed during JVM shutdown.
     * This is our only chance to provide after suite logic when using JUnit. After
     * all tests are executed this after suite logic get executed before processing ends. 
     */
    private static class AfterSuiteShutdownHook implements Runnable {
        private final String suiteName;

        /** The test suite to call after suite when executed */
        private final List<SequenceAfterSuite> afterSuite;
        
        /** Listeners */
        private final TestSuiteListeners testSuiteListener;
        
        /** Citrus test context */
        private final com.consol.citrus.context.TestContext context;
        
        /**
         * Default constructor using test suite field.
         */
        public AfterSuiteShutdownHook(String suiteName, List<SequenceAfterSuite> afterSuite, com.consol.citrus.context.TestContext context, TestSuiteListeners testSuiteListener) {
            this.suiteName = suiteName;
            this.afterSuite = afterSuite;
            this.context = context;
            this.testSuiteListener = testSuiteListener;
        }

        @Override
        public void run() {
            if (!afterSuite.isEmpty()) {
                for (SequenceAfterSuite sequenceAfterSuite : afterSuite) {
                    try {
                        if (sequenceAfterSuite.shouldExecute(suiteName, null)) {
                            sequenceAfterSuite.execute(context);
                        }
                    } catch (Exception e) {
                        throw new CitrusRuntimeException("After suite failed with errors", e);
                    }
                }
            } else {
                testSuiteListener.onFinish();
                testSuiteListener.onFinishSuccess();
            }
        }
    }
}
