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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Class broadcasting test action events to all available test action listeners 
 * injected by Spring's IoC container.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class TestActionListeners {
    
    /** List of test action listeners **/
    @Autowired(required = false)
    private List<TestActionListener> testActionListeners = new ArrayList<TestActionListener>();

    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        for (TestActionListener listener : testActionListeners) {
            listener.onTestActionFinish(testCase, testAction);
        }
    }

    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        for (TestActionListener listener : testActionListeners) {
            listener.onTestActionSkipped(testCase, testAction);
        }
    }

    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        for (TestActionListener listener : testActionListeners) {
            listener.onTestActionStart(testCase, testAction);
        }
    }

    /**
     * Adds a new test action listener.
     * @param listener
     */
    public void addTestActionListener(TestActionListener listener) {
        this.testActionListeners.add(listener);
    }
}
