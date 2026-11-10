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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.api.container.IteratingActionContainer;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.context.TestContext;

/**
 * Class broadcasting test action events to all available test action listeners
 * injected by Spring's IoC container.
 *
 * @since 1.3
 */
public class TestActionListeners implements TestActionListenerAware, TestActionContainerListener {

    /** List of test action listeners **/
    private final List<TestActionListener> testActionListeners = new ArrayList<>();

    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        for (TestActionListener listener : testActionListeners) {
            listener.onTestActionFinish(testCase, testAction);
        }
    }

    public void onTestActionFailed(TestCase testCase, TestAction testAction, Throwable cause) {
        for (TestActionListener listener : testActionListeners) {
            listener.onTestActionFailed(testCase, testAction, cause);
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

    @Override
    public void onContainerStart(TestActionContainer container, TestContext context) {
        containerListeners().forEach(listener -> listener.onContainerStart(container, context));
    }

    @Override
    public void onContainerSuccess(TestActionContainer container, TestContext context) {
        containerListeners().forEach(listener -> listener.onContainerSuccess(container, context));
    }

    @Override
    public void onContainerFailed(TestActionContainer container, TestContext context, Throwable cause) {
        containerListeners().forEach(listener -> listener.onContainerFailed(container, context, cause));
    }

    @Override
    public void onIterationStart(IteratingActionContainer container, int index, TestContext context) {
        containerListeners().forEach(listener -> listener.onIterationStart(container, index, context));
    }

    @Override
    public void onIterationSuccess(IteratingActionContainer container, int index, TestContext context) {
        containerListeners().forEach(listener -> listener.onIterationSuccess(container, index, context));
    }

    @Override
    public void onIterationFailed(IteratingActionContainer container, int index, TestContext context, Throwable cause) {
        containerListeners().forEach(listener -> listener.onIterationFailed(container, index, context, cause));
    }

    @Override
    public void onNestedActionStart(TestActionContainer container, TestAction action, TestContext context) {
        containerListeners().forEach(listener -> listener.onNestedActionStart(container, action, context));
    }

    @Override
    public void onNestedActionSuccess(TestActionContainer container, TestAction action, TestContext context) {
        containerListeners().forEach(listener -> listener.onNestedActionSuccess(container, action, context));
    }

    @Override
    public void onNestedActionFailed(TestActionContainer container, TestAction action, TestContext context, Throwable cause) {
        containerListeners().forEach(listener -> listener.onNestedActionFailed(container, action, context, cause));
    }

    @Override
    public void onNestedActionSkipped(TestActionContainer container, TestAction action, TestContext context) {
        containerListeners().forEach(listener -> listener.onNestedActionSkipped(container, action, context));
    }

    private Stream<TestActionContainerListener> containerListeners() {
        return testActionListeners.stream()
                .filter(TestActionContainerListener.class::isInstance)
                .map(TestActionContainerListener.class::cast);
    }

    @Override
    public void addTestActionListener(TestActionListener listener) {
        this.testActionListeners.add(listener);
    }

    /**
     * Obtains the testActionListeners.
     * @return
     */
    public List<TestActionListener> getTestActionListeners() {
        return Collections.unmodifiableList(testActionListeners);
    }
}
