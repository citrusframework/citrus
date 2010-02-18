/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.junit;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.TestSuite;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Test execution listener that will load a test suite instance from the application context and execute
 * tasks before and after the test run. The tasks after are executed as ShutdownHook thread that is added
 * to the JVM runtime.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteAwareExecutionListener extends AbstractTestExecutionListener {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestSuiteAwareExecutionListener.class);
    
    private static boolean done = false;
    
    private static Object doneMonitor = new Object();
    
    /**
     * @see org.springframework.test.context.support.AbstractTestExecutionListener#prepareTestInstance(org.springframework.test.context.TestContext)
     */
    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        synchronized (doneMonitor) {
            if(done) { 
                return; 
            } else {
                done = true;
            }
            
            ApplicationContext ctx = testContext.getApplicationContext();
        
            final TestSuite suite = getTestSuite(ctx);
            
            if(!suite.beforeSuite()) {
                throw new CitrusRuntimeException("Before suite failed with errors");
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    if(!suite.afterSuite()) {
                        throw new CitrusRuntimeException("After suite failed with errors");
                    }
                }
            }));
        }
    }

    /**
     * Retrieve a custom test suite instance in the application context. In case
     * no suitable instance is found the default test suite is returned instead.
     * 
     * @param ctx ApplicationContext holding a test suite bean.
     * @return test suite instence.
     */
    private TestSuite getTestSuite(ApplicationContext ctx) {
        Map<?, ?> suites = ctx.getBeansOfType(TestSuite.class);
        
        for (Entry<?, ?> entry : suites.entrySet()) {
            if(!entry.getKey().toString().equals(CitrusConstants.DEFAULT_SUITE_NAME)) {
                return (TestSuite)entry.getValue();
            }
        }
        
        log.warn("Could not find custom test suite - using default test suite");
        return (TestSuite)ctx.getBean(CitrusConstants.DEFAULT_SUITE_NAME, TestSuite.class);
    }
}
