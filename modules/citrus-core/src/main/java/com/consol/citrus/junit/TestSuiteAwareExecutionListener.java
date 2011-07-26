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

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;

/**
 * Test execution listener that will load a test suite instance from the application context and execute
 * tasks before and after the test run. The tasks after are executed as ShutdownHook thread that is added
 * to the JVM runtime.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteAwareExecutionListener extends AbstractTestExecutionListener {

    private static boolean done = false;
    
    private static Object doneMonitor = new Object();
    
    /**
     * @see org.springframework.test.context.support.AbstractTestExecutionListener#prepareTestInstance(org.springframework.test.context.TestContext)
     */
    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        synchronized (doneMonitor) {
            if (done) { 
                return; 
            } else {
                done = true;
            }
            
            ApplicationContext ctx = testContext.getApplicationContext();

            SequenceBeforeSuite beforeSuite = null;
            if (ctx.getBeansOfType(SequenceBeforeSuite.class).size() == 1) {
                beforeSuite = ctx.getBean(SequenceBeforeSuite.class);
            }
            
            SequenceAfterSuite afterSuite = null;
            if (ctx.getBeansOfType(SequenceAfterSuite.class).size() == 1) {
                afterSuite = ctx.getBean(SequenceAfterSuite.class);
            }
            
            com.consol.citrus.context.TestContext context = ctx.getBean(com.consol.citrus.context.TestContext.class);
            TestSuiteListeners testSuiteListener = ctx.getBean(TestSuiteListeners.class);

            if (beforeSuite != null) {
                try {
                    beforeSuite.execute(context);
                } catch (Exception e) {
                    throw new CitrusRuntimeException("Before suite failed with errors", e);
                }
            }  else {
                testSuiteListener.onStart();
                testSuiteListener.onStartSuccess();
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread(new AfterSuiteShutdownHook(afterSuite, context, testSuiteListener)));
        }
    }

    /**
     * Shutdown hook runnable gets executed during JVM shutdown.
     * This is our only chance to provide after suite logic when using JUnit. After
     * all tests are executed this after suite logic get executed before processing ends. 
     */
    private static class AfterSuiteShutdownHook implements Runnable {
        /** The test suite to call after suite when executed */
        private SequenceAfterSuite afterSuite;
        
        /** Listeners */
        private TestSuiteListeners testSuiteListener;
        
        /** Citrus test context */
        private com.consol.citrus.context.TestContext context;
        
        /**
         * Default constructor using test suite field.
         */
        public AfterSuiteShutdownHook(SequenceAfterSuite afterSuite, com.consol.citrus.context.TestContext context, TestSuiteListeners testSuiteListener) {
            this.afterSuite = afterSuite;
            this.context = context;
            this.testSuiteListener = testSuiteListener;
        }
        
        public void run() {
            if (afterSuite != null) {
                try {
                    afterSuite.execute(context);
                } catch (Exception e) {
                    throw new CitrusRuntimeException("After suite failed with errors", e);
                }
            } else {
                testSuiteListener.onFinish();
                testSuiteListener.onFinishSuccess();
            }
        }
    }
}
