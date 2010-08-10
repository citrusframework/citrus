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

import com.consol.citrus.TestSuite;

/**
 * Class managing a list of injected test suite listeners. Each event is spread to all
 * managed listeners.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteListeners implements TestSuiteListener {
    
    /** List of testsuite listeners **/
    @Autowired
    private List<TestSuiteListener> tesSuiteListeners = new ArrayList<TestSuiteListener>();
    
    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish(com.consol.citrus.TestSuite)
     */
    public void onFinish(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinish(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishFailure(testsuite, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess(com.consol.citrus.TestSuite)
     */
    public void onFinishSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishSuccess(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    public void onStart(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStart(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartFailure(testsuite, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess(com.consol.citrus.TestSuite)
     */
    public void onStartSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartSuccess(testsuite);
        }
    }
}
