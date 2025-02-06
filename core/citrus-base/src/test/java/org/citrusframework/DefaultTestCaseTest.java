package org.citrusframework;

import org.citrusframework.TestCaseMetaInfo.Status;
import org.citrusframework.actions.AbstractAsyncTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.container.AfterTest;
import org.citrusframework.container.Async;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.citrusframework.report.TestListener;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.TestResult.RESULT.FAILURE;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.TestUtils.WAIT_THREAD_PREFIX;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.fail;

public class DefaultTestCaseTest extends UnitTestSupport {

    private DefaultTestCase fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        fixture = new DefaultTestCase();
    }

    @Test
    public void failEmptyTestResult() {
        assertNull(fixture.getTestResult());

        var throwableMock = mock(Throwable.class);
        fixture.fail(throwableMock);

        var result = fixture.getTestResult();
        assertNotNull(result);

        assertEquals(result.getCause(), throwableMock);
        assertEquals(result.getResult(), FAILURE.name());
        assertNotNull(result.getDuration());
    }

    @Test
    public void failOverridesOtherTestResult() {
        fixture.setTestResult(success("failOverridesOtherTestResult", getClass().getSimpleName()));

        var throwableMock = mock(Throwable.class);
        fixture.fail(throwableMock);

        var result = fixture.getTestResult();
        assertNotNull(result);

        assertEquals(result.getCause(), throwableMock);
        assertEquals(result.getResult(), FAILURE.name());
        assertNotNull(result.getDuration());
    }

    @Test
    public void testExecution() {
        fixture.setName("MyTestCase");

        fixture.addTestAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testWaitForFinish() {
        fixture.setName("MyTestCase");

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

    @Test(expectedExceptions = TestCaseFailedException.class, expectedExceptionsMessageRegExp = "Failed to wait for test container to finish properly")
    public void testWaitForFinishTimeout() {
        fixture.setTimeout(500L);
        fixture.setName("MyTestCase");

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
        fixture.finish(context);
    }

    @Test
    public void testWaitForFinishAsync() {
        fixture.setName("MyTestCase");

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
                .filter(t -> t.getName().startsWith(WAIT_THREAD_PREFIX.concat("MyTestCase")))
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
        fixture.setName("MyTestCase");

        final Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("name", "Citrus");
        variables.put("framework", "${name}");
        variables.put("hello", "citrus:concat('Hello ', ${name}, '!')");
        variables.put("goodbye", "Goodbye ${name}!");
        variables.put("welcome", "Welcome ${name}, today is citrus:currentDate()!");
        fixture.setVariableDefinitions(variables);

        fixture.addTestAction(action(context -> {
            assertEquals(context.getVariables().get(CitrusSettings.TEST_NAME_VARIABLE), "MyTestCase");
            assertEquals(context.getVariables().get(CitrusSettings.TEST_PACKAGE_VARIABLE), TestCase.class.getPackage().getName());
            assertEquals(context.getVariable("${name}"), "Citrus");
            assertEquals(context.getVariable("${framework}"), "Citrus");
            assertEquals(context.getVariable("${hello}"), "Hello Citrus!");
            assertEquals(context.getVariable("${goodbye}"), "Goodbye Citrus!");
            assertEquals(context.getVariable("${welcome}"), "Welcome Citrus, today is " + new CurrentDateFunction().execute(new ArrayList<>(), context) + "!");
        }));

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testUnknownVariable() {
        fixture.setName("MyTestCase");

        var message = "Hello TestFramework!";
        fixture.setVariableDefinitions(singletonMap("text", message));

        fixture.addTestAction(action(context -> assertEquals(context.getVariable("${unknown}"), message)));

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test(expectedExceptions = {TestCaseFailedException.class}, expectedExceptionsMessageRegExp = "This failed in forked action")
    public void testExceptionInContext() {
        fixture.setName("MyTestCase");

        fixture.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        fixture.addTestAction(new EchoAction.Builder().message("Everything is fine!").build());

        fixture.execute(context);
        fixture.finish(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testExceptionInContextInFinish() {
        fixture.setName("MyTestCase");

        fixture.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        fixture.execute(context);
        fixture.finish(context);
    }

    @Test
    public void testFinalActions() {
        fixture.setName("MyTestCase");

        fixture.addTestAction(new EchoAction.Builder().build());
        fixture.addFinalAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        verifyDurationHasBeenMeasured(fixture.getTestResult());
    }

    @Test
    public void testThreadLeak() {
        fixture.setName("ThreadLeakTest");
        fixture.addTestAction(new EchoAction.Builder().build());

        fixture.execute(context);
        fixture.finish(context);

        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(WAIT_THREAD_PREFIX.concat("ThreadLeakTest")))
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
