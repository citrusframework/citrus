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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.container.RepeatUntilTrue.Builder.repeat;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.container.SequenceAfterSuite.Builder.afterSuite;
import static org.citrusframework.container.SequenceAfterTest.Builder.afterTest;
import static org.citrusframework.container.SequenceBeforeSuite.Builder.beforeSuite;
import static org.citrusframework.container.SequenceBeforeTest.Builder.beforeTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NestedActionCallbacksTest extends UnitTestSupport {

    @Mock
    private TestActionListener plainTestActionListenerMock;

    private final RecordingContainerListener recordingContainerListener = new RecordingContainerListener();

    @BeforeEach
    void beforeEach() {
        context.addTestActionListener(recordingContainerListener);
        context.addTestActionListener(plainTestActionListenerMock);
    }

    static Stream<Arguments> boundaryContainers() {
        return Stream.of(
                arguments("before-test", (Function<TestAction[], AbstractActionContainer>) testActions ->
                        beforeTest().actions(testActions).build()),
                arguments("after-test", (Function<TestAction[], AbstractActionContainer>) testActions ->
                        afterTest().actions(testActions).build()),
                arguments("before-suite", (Function<TestAction[], AbstractActionContainer>) testActions ->
                        beforeSuite().actions(testActions).build()),
                arguments("after-suite", (Function<TestAction[], AbstractActionContainer>) testActions ->
                        afterSuite().actions(testActions).build()));
    }

    @ParameterizedTest
    @MethodSource("boundaryContainers")
    void boundaryContainer_firesNestedActionCallbacksWithoutKeepingState(
            String containerName, Function<TestAction[], AbstractActionContainer> containerFactory) {
        AbstractActionContainer boundaryContainer = containerFactory.apply(new TestAction[]{
                namedAction("first", () -> {}), namedAction("second", () -> {})});

        boundaryContainer.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                containerName + ":start:first", containerName + ":success:first",
                containerName + ":start:second", containerName + ":success:second");
        assertThat(boundaryContainer.getExecutedActions()).isEmpty();
        assertThat(boundaryContainer.getActiveAction()).isNull();
        verifyNoInteractions(plainTestActionListenerMock);
    }

    @ParameterizedTest
    @MethodSource("boundaryContainers")
    void boundaryContainer_firesNestedActionFailed(
            String containerName, Function<TestAction[], AbstractActionContainer> containerFactory) {
        AbstractActionContainer boundaryContainer = containerFactory.apply(new TestAction[]{
                namedAction("failing", () -> {
                    throw new CitrusRuntimeException("failed");
                })});

        assertThatThrownBy(() -> boundaryContainer.execute(context))
                .isInstanceOf(CitrusRuntimeException.class);

        assertThat(recordingContainerListener.events).containsExactly(
                containerName + ":start:failing", containerName + ":failed:failing");
    }

    @ParameterizedTest
    @MethodSource("boundaryContainers")
    void boundaryContainer_firesNestedActionSkippedForDisabledAction(
            String containerName, Function<TestAction[], AbstractActionContainer> containerFactory) {
        AtomicInteger executionCount = new AtomicInteger();
        AbstractActionContainer boundaryContainer = containerFactory.apply(new TestAction[]{
                disabledAction("disabled", executionCount::incrementAndGet)});

        boundaryContainer.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(containerName + ":skipped:disabled");
        // execution semantics are unchanged - the action itself is responsible for honoring its disabled state
        assertThat(executionCount).hasValue(1);
    }

    @Test
    void boundaryContainer_firesNestedActionCallbacksForNestedContainers() {
        Sequence sequence = sequential().actions(namedAction("inner", () -> {})).build();
        SequenceBeforeTest sequenceBeforeTest = beforeTest().actions(sequence).build();

        sequenceBeforeTest.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "before-test:start:sequential",
                "sequential:start:inner", "sequential:success:inner",
                "before-test:success:sequential");
    }

    @Test
    void sequential_firesNestedActionCallbacksAndTracksExecutedActions() {
        TestAction firstAction = namedAction("first", () -> {});
        TestAction secondAction = namedAction("second", () -> {});
        Sequence sequence = sequential().actions(firstAction, secondAction).build();

        sequence.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "sequential:start:first", "sequential:success:first",
                "sequential:start:second", "sequential:success:second");
        assertThat(sequence.getExecutedActions()).containsExactly(firstAction, secondAction);
    }

    @Test
    void repeat_firesNestedActionCallbacksWithinIterations() {
        RepeatUntilTrue repeatUntilTrue = repeat()
                .until("i = 3")
                .index("i")
                .actions(namedAction("polling", () -> {}))
                .build();

        repeatUntilTrue.execute(context);

        assertThat(recordingContainerListener.events).containsExactly(
                "repeat:iterationStart:1", "repeat:start:polling", "repeat:success:polling", "repeat:iterationSuccess:1",
                "repeat:iterationStart:2", "repeat:start:polling", "repeat:success:polling", "repeat:iterationSuccess:2");
    }

    private static TestAction namedAction(String name, Runnable behavior) {
        return new TestAction() {
            @Override
            public void execute(TestContext context) {
                behavior.run();
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private static TestAction disabledAction(String name, Runnable behavior) {
        return new TestAction() {
            @Override
            public void execute(TestContext context) {
                behavior.run();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isDisabled(TestContext context) {
                return true;
            }
        };
    }

    private static class RecordingContainerListener extends AbstractTestActionListener
            implements TestActionContainerListener {

        private final List<String> events = new CopyOnWriteArrayList<>();

        @Override
        public void onIterationStart(IteratingActionContainer container, int index, TestContext context) {
            events.add(container.getName() + ":iterationStart:" + index);
        }

        @Override
        public void onIterationSuccess(IteratingActionContainer container, int index, TestContext context) {
            events.add(container.getName() + ":iterationSuccess:" + index);
        }

        @Override
        public void onNestedActionStart(TestActionContainer container, TestAction action, TestContext context) {
            events.add(container.getName() + ":start:" + action.getName());
        }

        @Override
        public void onNestedActionSuccess(TestActionContainer container, TestAction action, TestContext context) {
            events.add(container.getName() + ":success:" + action.getName());
        }

        @Override
        public void onNestedActionFailed(TestActionContainer container, TestAction action, TestContext context, Throwable cause) {
            events.add(container.getName() + ":failed:" + action.getName());
        }

        @Override
        public void onNestedActionSkipped(TestActionContainer container, TestAction action, TestContext context) {
            events.add(container.getName() + ":skipped:" + action.getName());
        }
    }
}
