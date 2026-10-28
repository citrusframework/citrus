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

package org.citrusframework.base;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestAction;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestCaseMetaInfo.Status;
import org.citrusframework.TestResult;
import org.citrusframework.actions.AbstractAsyncTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.api.container.AfterTest;
import org.citrusframework.container.Async;
import org.citrusframework.api.container.BeforeTest;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.base.functions.core.CurrentDateFunction;
import org.citrusframework.report.TestListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.TestResult.RESULT.FAILURE;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.TestUtils.WAIT_THREAD_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DefaultTestCaseTest extends UnitTestSupport implements TestActionSupport {

    private DefaultTestCase fixture;

    @BeforeEach
    void beforeEach() {
        fixture = new DefaultTestCase();
    }

    @Test
    public void failEmptyTestResult() {
        assertNull(fixture.getTestResult());

        var throwableMock = mock(Throwable.class);
        fixture.fail(throwableMock);

        var result = fixture.getTestResult();
        assertNotNull(result);

        assertEquals(throwableMock, result.getCause());
        assertEquals(FAILURE.name(), result.getResult());
        assertNotNull(result.getDuration());
    }

    @Test
    public void failOverridesOtherTestResult() {
        fixture.setTestResult(success("failOverridesOtherTestResult", getClass().getSimpleName()));

        var throwableMock = mock(Throwable.class);
        fixture.fail(throwableMock);

        var result = fixture.getTestResult();
        assertNotNull(result);

        assertEquals(throwableMock, result.getCause());
        assertEquals(FAILURE.name(), result.getResult());
        assertNotNull(result.getDuration());
    }

    @Test
    public void testExecution() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testWaitForFinish() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(new EchoAction.Builder().build());
        fixture.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        fixture.execute(context);
        fixture.finish(context);

        var duration = verifyDurationHasBeenMeasured(fixture.getTestResult());
        if (duration.compareTo(Duration.ofMillis(500)) < 0) {
            fail("TestResult / Duration should be more than 500 ms, because that's how long the async action takes!");
        }
    }

    @Test
    public void testWaitForFinishTimeout() {
        fixture.setTimeout(500L);
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(new EchoAction.Builder().build());
        fixture.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        fixture.execute(context);
        TestCaseFailedException thrown = assertThrows(TestCaseFailedException.class, () -> fixture.finish(context));
        assertThat(thrown).hasMessage("Failed to wait for test container in test 'DefaultTestCaseTest' to finish properly");
    }

    @Test
    public void testWaitForFinishAsync() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(new Async.Builder().actions(() -> new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        }).build());

        fixture.execute(context);
        fixture.finish(context);

        // Make sure that waiting thread is completed
        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(WAIT_THREAD_PREFIX.concat("DefaultTestCaseTest")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> LoggerFactory.getLogger("TestWaitForFinishAsync").warn(Arrays.toString(threads.get(thread))));
        waitingThread.ifPresent(thread -> fail(format("Waiting thread still alive: %s", thread)));

        var duration = verifyDurationHasBeenMeasured(fixture.getTestResult());
        if (duration.compareTo(Duration.ofMillis(500)) < 0) {
            fail("TestResult / Duration should be more than 500 ms, because that's how long the async action takes!");
        }
    }

    @Test
    public void testTestListenerEventsWithSuccessfulTestResult() {
        TestListener testListenerMock = mock();
        context.getTestListeners().addTestListener(testListenerMock);

        BeforeTest beforeTestMock = mock();
        List<BeforeTest> beforeTestListMock = new ArrayList<>(){{add(beforeTestMock);}};
        context.setBeforeTest(beforeTestListMock);

        TestAction testActionMock = mock();
        List<TestAction> testActionListMock = new ArrayList<>(){{add(testActionMock);}};
        fixture.setFinalActions(testActionListMock);

        AfterTest afterTestMock = mock();
        List<AfterTest> afterTestListMock = new ArrayList<>(){{add(afterTestMock);}};
        context.setAfterTest(afterTestListMock);

        fixture.doExecute(context);
        fixture.finish(context);

        InOrder inOrder = inOrder(testListenerMock);
        inOrder.verify(testListenerMock, times(1)).onTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestSuccess(fixture);
        inOrder.verify(testListenerMock, times(1)).onAfterTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onAfterTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestEnd(fixture);
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void testTestListenerEventsWithTestActionFailedTestResult() {
        TestAction testActionMock = mock();
        doThrow(new CitrusRuntimeException()).when(testActionMock).execute(context);
        fixture.addTestAction(testActionMock);
        TestListener testListenerMock = mock();
        context.getTestListeners().addTestListener(testListenerMock);

        List<BeforeTest> beforeTestListMock = List.of(mock(BeforeTest.class));
        context.setBeforeTest(beforeTestListMock);

        List<TestAction> testActionListMock = List.of(mock(TestAction.class));
        fixture.setFinalActions(testActionListMock);

        List<AfterTest> afterTestListMock = List.of(mock(AfterTest.class));
        context.setAfterTest(afterTestListMock);

        assertThrows(TestCaseFailedException.class, () -> fixture.doExecute(context));
        fixture.finish(context);

        InOrder inOrder = inOrder(testListenerMock);
        inOrder.verify(testListenerMock, times(1)).onTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestFailure(fixture, fixture.getTestResult().getCause());
        inOrder.verify(testListenerMock, times(1)).onAfterTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onAfterTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestEnd(fixture);
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void testTestListenerEventsWithFinalActionFailedTestResult() {

        TestListener testListenerMock = mock();
        context.getTestListeners().addTestListener(testListenerMock);

        List<BeforeTest> beforeTestListMock = List.of(mock(BeforeTest.class));
        context.setBeforeTest(beforeTestListMock);

        TestAction finalActionMock = mock();
        fixture.addFinalAction(finalActionMock);
        doThrow(new CitrusRuntimeException()).when(finalActionMock).execute(context);

        List<AfterTest> afterTestListMock = List.of(mock(AfterTest.class));
        context.setAfterTest(afterTestListMock);

        fixture.doExecute(context);
        assertThrows(TestCaseFailedException.class, () -> fixture.finish(context));

        InOrder inOrder = inOrder(testListenerMock);
        inOrder.verify(testListenerMock, times(1)).onTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onBeforeTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onFinalActionsEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestExecutionEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestFailure(fixture, fixture.getTestResult().getCause());
        inOrder.verify(testListenerMock, times(1)).onAfterTestStart(fixture);
        inOrder.verify(testListenerMock, times(1)).onAfterTestEnd(fixture);
        inOrder.verify(testListenerMock, times(1)).onTestEnd(fixture);
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void testListenerEventOnTestSkipped() {
        TestCaseMetaInfo testCaseMetaInfoMock = new TestCaseMetaInfo();
        testCaseMetaInfoMock.setStatus(Status.DISABLED);
        fixture.setMetaInfo(testCaseMetaInfoMock);
        TestListener testListenerMock = mock();
        context.getTestListeners().addTestListener(testListenerMock);

        fixture.doExecute(context);
        fixture.finish(context);

        InOrder inOrder = inOrder(testListenerMock);
        inOrder.verify(testListenerMock, times(1)).onTestSkipped(fixture);
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void testExecutionWithVariables() {
        fixture.setName("DefaultTestCaseTest");

        final Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("name", "Citrus");
        variables.put("framework", "${name}");
        variables.put("hello", "citrus:concat('Hello ', ${name}, '!')");
        variables.put("goodbye", "Goodbye ${name}!");
        variables.put("welcome", "Welcome ${name}, today is citrus:currentDate()!");
        variables.put("singleQuote", "citrus:concat(''', ${name}, ''')");
        fixture.setVariableDefinitions(variables);

        fixture.addTestAction(action(context -> {
            assertEquals("DefaultTestCaseTest", context.getVariables().get(CitrusSettings.getTestNameVariable()));
            assertEquals(DefaultTestCase.class.getPackage().getName(), context.getVariables().get(CitrusSettings.getTestPackageVariable()));
            assertEquals("Citrus", context.getVariable("${name}"));
            assertEquals("Citrus", context.getVariable("${framework}"));
            assertEquals("Hello Citrus!", context.getVariable("${hello}"));
            assertEquals("Goodbye Citrus!", context.getVariable("${goodbye}"));
            assertEquals("Welcome Citrus, today is " + new CurrentDateFunction().execute(new ArrayList<>(), context) + "!", context.getVariable("${welcome}"));
            assertEquals("'Citrus'", context.getVariable("${singleQuote}"));
        }));

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testUnknownVariable() {
        fixture.setName("DefaultTestCaseTest");

        var message = "Hello TestFramework!";
        fixture.setVariableDefinitions(singletonMap("text", message));

        fixture.addTestAction(action(context -> assertEquals(message, context.getVariable("${unknown}"))));

        assertThrows(TestCaseFailedException.class, () -> {
            fixture.execute(context);
            fixture.finish(context);
        });
    }

    @Test
    public void testExceptionInContext() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        fixture.addTestAction(new EchoAction.Builder().message("Everything is fine!").build());

        TestCaseFailedException thrown = assertThrows(TestCaseFailedException.class, () -> {
            fixture.execute(context);
            fixture.finish(context);
        });
        assertThat(thrown).hasMessage("This failed in forked action");
    }

    @Test
    public void testExceptionInContextInFinish() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        fixture.execute(context);
        assertThrows(TestCaseFailedException.class, () -> fixture.finish(context));
    }

    @Test
    public void testFinalActions() {
        fixture.setName("DefaultTestCaseTest");

        fixture.addTestAction(new EchoAction.Builder().build());
        fixture.addFinalAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testThreadLeak() {
        fixture.setName("ThreadLeakDefaultTestCase");
        fixture.addTestAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(WAIT_THREAD_PREFIX.concat("ThreadLeakDefaultTestCase")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> fail(format("Waiting thread still alive: %s", thread)));

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testDurationCalculation() {
        Function<Long, TestAction> sleepActionSupplier = (sleepTime) -> new SleepAction.Builder()
                .milliseconds(sleepTime)
                .build();

        context.getBeforeTest()
                .add(new SequenceBeforeTest.Builder()
                        .actions(sleepActionSupplier.apply(250L))
                        .build());

        context.getAfterTest()
                .add(new SequenceAfterTest.Builder()
                        .actions(sleepActionSupplier.apply(250L))
                        .build());

        fixture.addTestAction(sleepActionSupplier.apply(1000L));
        fixture.addFinalAction(sleepActionSupplier.apply(500L));

        fixture.execute(context);
        fixture.finish(context);

        var duration = verifyDurationHasBeenMeasured(fixture.getTestResult());
        if (duration.compareTo(Duration.ofMillis(2000)) < 0) {
            fail("TestResult / Duration should be more than 2000 ms, because that's how long all actions take!");
        }
    }

    private static Duration verifyDurationHasBeenMeasured(TestResult fixture) {
        assertNotNull(fixture);
        assertNotNull(fixture.getDuration());
        return fixture.getDuration();
    }
}
