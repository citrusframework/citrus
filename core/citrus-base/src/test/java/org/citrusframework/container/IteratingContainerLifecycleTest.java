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

package org.citrusframework.container;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.citrusframework.TestAction;
import org.citrusframework.api.container.IteratingActionContainer;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.AbstractTestActionListener;
import org.citrusframework.report.TestActionContainerListener;
import org.citrusframework.report.TestActionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.lang.Thread.currentThread;
import static java.time.Duration.ZERO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.container.Iterate.Builder.iterate;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static org.citrusframework.container.RepeatUntilTrue.Builder.repeat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class IteratingContainerLifecycleTest extends UnitTestSupport {

    @Mock
    private TestActionListener plainTestActionListenerMock;

    private final RecordingContainerListener recordingContainerListener = new RecordingContainerListener();

    private final TestAction noopAction = context -> {
    };

    @BeforeEach
    void beforeEach() {
        context.addTestActionListener(recordingContainerListener);
        context.addTestActionListener(plainTestActionListenerMock);
    }

    @Test
    void repeatUntilTrue_firesContainerAndIterationCallbacksInOrder() {
        RepeatUntilTrue repeatUntilTrue = repeat()
                .until("i = 3")
                .index("i")
                .actions(() -> noopAction)
                .build();

        repeatUntilTrue.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat",
                "iterationStart:1", "iterationSuccess:1",
                "iterationStart:2", "iterationSuccess:2",
                "containerSuccess:repeat");
        verifyNoInteractions(plainTestActionListenerMock);
    }

    @Test
    void iterate_firesContainerAndIterationCallbacksInOrder() {
        Iterate iterateContainer = iterate()
                .times(2)
                .index("i")
                .actions(() -> noopAction)
                .build();

        iterateContainer.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:iterate",
                "iterationStart:1", "iterationSuccess:1",
                "iterationStart:2", "iterationSuccess:2",
                "containerSuccess:iterate");
    }

    @Test
    void repeatOnErrorUntilTrue_firesIterationFailedForEachRetry() {
        TestAction failingUntilThirdIterationAction = context -> {
            if (Integer.parseInt(context.getVariable("i")) < 3) {
                throw new CitrusRuntimeException("failed");
            }
        };
        RepeatOnErrorUntilTrue repeatOnErrorUntilTrue = repeatOnError()
                .until("i = 5")
                .index("i")
                .autoSleep(ZERO)
                .actions(() -> failingUntilThirdIterationAction)
                .build();

        repeatOnErrorUntilTrue.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat-on-error",
                "iterationStart:1", "iterationFailed:1",
                "iterationStart:2", "iterationFailed:2",
                "iterationStart:3", "iterationSuccess:3",
                "containerSuccess:repeat-on-error");
    }

    @Test
    void repeatOnErrorUntilTrue_firesContainerFailedWhenAllRetriesFail() {
        TestAction failingAction = context -> {
            throw new CitrusRuntimeException("failed");
        };
        RepeatOnErrorUntilTrue repeatOnErrorUntilTrue = repeatOnError()
                .until("i = 3")
                .index("i")
                .autoSleep(ZERO)
                .actions(() -> failingAction)
                .build();

        assertThatThrownBy(() -> repeatOnErrorUntilTrue.execute(context))
                .isInstanceOf(CitrusRuntimeException.class);

        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat-on-error",
                "iterationStart:1", "iterationFailed:1",
                "iterationStart:2", "iterationFailed:2",
                "containerFailed:repeat-on-error");
    }

    @Test
    void withTimeout_firesIterationCallbacksOnWorkerThread() {
        RepeatUntilTrue repeatUntilTrue = repeat()
                .until("i = 2")
                .index("i")
                .timeout(Duration.ofSeconds(10))
                .actions(() -> noopAction)
                .build();

        repeatUntilTrue.execute(context);

        String callingThreadName = currentThread().getName();
        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat", "iterationStart:1", "iterationSuccess:1", "containerSuccess:repeat");
        assertThat(recordingContainerListener.containerThreadNames).containsOnly(callingThreadName);
        assertThat(recordingContainerListener.iterationThreadNames).doesNotContain(callingThreadName);
    }

    @Test
    void withTimeout_firesContainerFailedWhenTimeoutIsReached() throws InterruptedException {
        CountDownLatch releaseActionLatch = new CountDownLatch(1);
        TestAction blockingAction = context -> {
            try {
                releaseActionLatch.await(5, SECONDS);
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        };
        RepeatUntilTrue repeatUntilTrue = repeat()
                .until("i = 2")
                .index("i")
                .timeout(Duration.ofMillis(200))
                .actions(() -> blockingAction)
                .build();

        assertThatThrownBy(() -> repeatUntilTrue.execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Iteration reached timeout!");

        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat", "iterationStart:1", "containerFailed:repeat");

        // the worker thread is not interrupted on timeout - its iteration callback arrives after containerFailed
        releaseActionLatch.countDown();
        assertThat(recordingContainerListener.iterationSucceededLatch.await(5, SECONDS)).isTrue();
        assertThat(recordingContainerListener.events).containsExactly(
                "containerStart:repeat", "iterationStart:1", "containerFailed:repeat", "iterationSuccess:1");
    }

    @Test
    void protectedLifecycleHooks_areInvokedForSubclasses() {
        List<String> hookEvents = new CopyOnWriteArrayList<>();
        RepeatUntilTrue.Builder builder = repeat()
                .until("i = 2")
                .index("i")
                .actions(() -> noopAction);
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue(builder) {
            @Override
            protected void onContainerStart(TestContext context) {
                hookEvents.add("containerStart");
            }

            @Override
            protected void onIterationStart(int iterationIndex, TestContext context) {
                hookEvents.add("iterationStart:" + iterationIndex);
            }

            @Override
            protected void onIterationSuccess(int iterationIndex, TestContext context) {
                hookEvents.add("iterationSuccess:" + iterationIndex);
            }

            @Override
            protected void onContainerSuccess(TestContext context) {
                hookEvents.add("containerSuccess");
            }
        };

        repeatUntilTrue.execute(context);

        assertThat(hookEvents).containsExactly(
                "containerStart", "iterationStart:1", "iterationSuccess:1", "containerSuccess");
    }

    private static class RecordingContainerListener extends AbstractTestActionListener
            implements TestActionContainerListener {

        private final List<String> events = new CopyOnWriteArrayList<>();
        private final List<String> containerThreadNames = new CopyOnWriteArrayList<>();
        private final List<String> iterationThreadNames = new CopyOnWriteArrayList<>();
        private final CountDownLatch iterationSucceededLatch = new CountDownLatch(1);

        @Override
        public void onContainerStart(TestActionContainer container, TestContext context) {
            recordContainerEvent("containerStart:" + container.getName());
        }

        @Override
        public void onContainerSuccess(TestActionContainer container, TestContext context) {
            recordContainerEvent("containerSuccess:" + container.getName());
        }

        @Override
        public void onContainerFailed(TestActionContainer container, TestContext context, Throwable cause) {
            recordContainerEvent("containerFailed:" + container.getName());
        }

        @Override
        public void onIterationStart(IteratingActionContainer container, int index, TestContext context) {
            recordIterationEvent("iterationStart:" + index);
        }

        @Override
        public void onIterationSuccess(IteratingActionContainer container, int index, TestContext context) {
            recordIterationEvent("iterationSuccess:" + index);
            iterationSucceededLatch.countDown();
        }

        @Override
        public void onIterationFailed(IteratingActionContainer container, int index, TestContext context, Throwable cause) {
            recordIterationEvent("iterationFailed:" + index);
        }

        private void recordContainerEvent(String event) {
            events.add(event);
            containerThreadNames.add(currentThread().getName());
        }

        private void recordIterationEvent(String event) {
            events.add(event);
            iterationThreadNames.add(currentThread().getName());
        }
    }
}
