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

import com.consol.citrus.TestCase;

/**
 * Class that spreads test events to all available test listeners 
 * injected by Spring's IoC container.
 * 
 * @author Christoph Deppisch
 */
public class TestListeners implements TestListener {
    
    /** List of test listeners **/
    @Autowired
    private List<TestListener> testListeners = new ArrayList<TestListener>();
   
    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestListener listener : testListeners) {
            listener.onTestFailure(test, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestFinish(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSkipped(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestStart(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSuccess(test);
        }
    }
}
