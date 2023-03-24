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

package org.citrusframework.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class managing a list of injected test suite listeners. Each event is spread to all
 * managed listeners.
 *
 * @author Christoph Deppisch
 */
public class TestSuiteListeners implements TestSuiteListenerAware {

    /** List of testsuite listeners **/
    private final List<TestSuiteListener> testSuiteListeners = new ArrayList<>();

    @Override
    public void addTestSuiteListener(TestSuiteListener testSuiteListener) {
        if (!testSuiteListeners.contains(testSuiteListener)) {
            testSuiteListeners.add(testSuiteListener);
        }
    }

    public void onFinish() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinish();
        }
    }

    public void onFinishFailure(Throwable cause) {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinishFailure(cause);
        }
    }

    public void onFinishSuccess() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onFinishSuccess();
        }
    }

    public void onStart() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStart();
        }
    }

    public void onStartFailure(Throwable cause) {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStartFailure(cause);
        }
    }

    public void onStartSuccess() {
        for (TestSuiteListener listener : testSuiteListeners) {
            listener.onStartSuccess();
        }
    }

    /**
     * Obtains the testSuiteListeners.
     * @return
     */
    public List<TestSuiteListener> getTestSuiteListeners() {
        return Collections.unmodifiableList(testSuiteListeners);
    }
}
