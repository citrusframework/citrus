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

package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class managing a list of injected test suite listeners. Each event is spread to all
 * managed listeners.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteListeners implements TestSuiteListener {
    
    /** List of testsuite listeners **/
    @Autowired
    private List<TestSuiteListener> testSuiteListeners = new ArrayList<TestSuiteListener>();
    
    /** List of testsuite reporter **/
    @Autowired
    private List<TestReporter> testReporters = new ArrayList<TestReporter>();
    
    /**
     * Adds a new test suite listener. 
     * @param testSuiteListener the listener.
     */
    public void addTestSuiteListener(TestSuiteListener testSuiteListener) {
        testSuiteListeners.add(testSuiteListener);
    }
    
    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish()
     */
    public void onFinish() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinish();
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(java.lang.Throwable)
     */
    public void onFinishFailure(Throwable cause) {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinishFailure(cause);
        }
        
        for (TestReporter reporter : testReporters) {
            reporter.generateTestResults();
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess()
     */
    public void onFinishSuccess() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinishSuccess();
        }
        
        for (TestReporter reporter : testReporters) {
            reporter.generateTestResults();
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart()
     */
    public void onStart() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStart();
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(java.lang.Throwable)
     */
    public void onStartFailure(Throwable cause) {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStartFailure(cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess()
     */
    public void onStartSuccess() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStartSuccess();
        }
    }
}
