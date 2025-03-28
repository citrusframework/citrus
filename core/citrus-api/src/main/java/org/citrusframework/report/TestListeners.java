/*
 * Copyright the original author or authors.
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
import java.util.List;

import org.citrusframework.TestCase;

/**
 * Class that spreads test events to all available test listeners
 * injected by Spring's IoC container.
 *
 */
public class TestListeners implements TestListenerAware {

    /** List of test listeners **/
    private final List<TestListener> testListeners = new ArrayList<>();

    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestListener listener : testListeners) {
            listener.onTestFailure(test, cause);
        }
    }

    /**
     * @deprecated use on {@link #onTestExecutionEnd(TestCase)}
     */
    @Deprecated(forRemoval = true)
    public void onTestFinish(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestFinish(test);
        }
    }

    public void onTestExecutionStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestExecutionStart(test);
        }

    }

    public void onTestExecutionEnd(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestExecutionEnd(test);
        }
    }

    public void onTestSkipped(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSkipped(test);
        }
    }

    public void onTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestStart(test);
        }
    }

    public void onTestFinalization(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestEnd(test);
        }
    }

    public void onFinalActionsStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onFinalActionsStart(test);
        }
    }

    public void onFinalActionsEnd(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onFinalActionsEnd(test);
        }
    }

    public void onBeforeTestEnd(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onBeforeTestEnd(test);
        }
    }

    public void onAfterTestEnd(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onAfterTestEnd(test);
        }
    }

    public void onBeforeTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onBeforeTestStart(test);
        }
    }

    public void onAfterTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onAfterTestStart(test);
        }
    }

    public void onTestSuccess(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSuccess(test);
        }
    }

    @Override
    public void addTestListener(TestListener listener) {
        if (!testListeners.contains(listener)) {
            this.testListeners.add(listener);
        }
    }

    /**
     * Obtains the testListeners.
     */
    public List<TestListener> getTestListeners() {
        return testListeners;
    }
}
