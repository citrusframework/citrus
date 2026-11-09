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

import org.citrusframework.TestAction;
import org.citrusframework.api.container.IteratingActionContainer;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.context.TestContext;

/**
 * Optional listener receiving lifecycle callbacks of test action containers.
 * <p>
 * Implementations get registered like any other {@link TestActionListener}: a listener that implements both
 * interfaces receives the container callbacks in addition to the regular test action events. All methods are
 * no-op by default so implementations only override the callbacks they are interested in.
 * <p>
 * Container and iteration callbacks are fired by iterating containers ({@code iterate}, {@code repeat},
 * {@code repeat-on-error}). When such a container is configured with a timeout, the iterations are executed in a
 * separate worker thread. The iteration callbacks are then invoked on that worker thread while the container
 * callbacks are invoked on the calling thread.
 * <p>
 * Nested action callbacks are fired for every action executed within a container, including the before/after test
 * and before/after suite sequences. Top level test actions are not reported here, they are reported via
 * {@link TestActionListener}. Containers executing their nested actions concurrently (e.g. {@code parallel},
 * {@code async}) invoke the nested action callbacks on their worker threads.
 * <p>
 * Implementations relying on thread bound state (e.g. MDC) must take these threading aspects into account.
 */
public interface TestActionContainerListener {

    /**
     * Invoked when the container starts its execution.
     */
    default void onContainerStart(TestActionContainer container, TestContext context) {
    }

    /**
     * Invoked when the container has finished its execution successfully.
     */
    default void onContainerSuccess(TestActionContainer container, TestContext context) {
    }

    /**
     * Invoked when the container execution has failed.
     * <p>
     * When an iterating container reaches its timeout, this callback is invoked on the calling thread while the
     * worker thread executing the iteration is not interrupted. Iteration callbacks of the still running iteration
     * (e.g. {@link #onIterationSuccess} or {@link #onIterationFailed}) may therefore arrive after this callback.
     */
    default void onContainerFailed(TestActionContainer container, TestContext context, Throwable cause) {
    }

    /**
     * Invoked before the nested actions of an iteration get executed.
     */
    default void onIterationStart(IteratingActionContainer container, int index, TestContext context) {
    }

    /**
     * Invoked after all nested actions of an iteration have been executed successfully.
     * In timeout scenarios this callback may arrive after {@link #onContainerFailed}.
     */
    default void onIterationSuccess(IteratingActionContainer container, int index, TestContext context) {
    }

    /**
     * Invoked when one of the nested actions of an iteration has failed.
     * The container may decide to retry with another iteration (e.g. {@code repeat-on-error}).
     * In timeout scenarios this callback may arrive after {@link #onContainerFailed}.
     */
    default void onIterationFailed(IteratingActionContainer container, int index, TestContext context, Throwable cause) {
    }

    /**
     * Invoked before a nested action of the container gets executed.
     */
    default void onNestedActionStart(TestActionContainer container, TestAction action, TestContext context) {
    }

    /**
     * Invoked after a nested action of the container has been executed successfully.
     */
    default void onNestedActionSuccess(TestActionContainer container, TestAction action, TestContext context) {
    }

    /**
     * Invoked when a nested action of the container has failed.
     */
    default void onNestedActionFailed(TestActionContainer container, TestAction action, TestContext context, Throwable cause) {
    }

    /**
     * Invoked when a nested action of the container is disabled and therefore skipped.
     */
    default void onNestedActionSkipped(TestActionContainer container, TestAction action, TestContext context) {
    }
}
