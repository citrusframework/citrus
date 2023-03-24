package org.citrusframework.cucumber.backend;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestResult;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.citrusframework.cucumber.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import io.cucumber.core.backend.Status;
import io.cucumber.core.backend.TestCaseState;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class CitrusLifecycleHooksTest extends UnitTestSupport {

    @Mock
    private TestCaseState state;

    @Mock
    private TestCaseRunner runner;

    private CitrusLifecycleHooks citrusLifecycleHooks;

    @BeforeMethod
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
        citrusLifecycleHooks = new CitrusLifecycleHooks();
        CitrusAnnotations.injectTestContext(citrusLifecycleHooks, context);
        CitrusAnnotations.injectTestRunner(citrusLifecycleHooks, runner);
    }

    @Test
    public void shouldStartTestCaseRunner() {
        when(state.getId()).thenReturn("mockedScenario");
        when(state.getName()).thenReturn("This is a mocked scenario");

        citrusLifecycleHooks.before(new Scenario(state));
        verify(runner).name("This is a mocked scenario");
        verify(runner).description("mockedScenario");
        verify(runner).start();
    }

    @Test
    public void shouldHandleSuccessfulScenario() {
        when(state.isFailed()).thenReturn(false);
        citrusLifecycleHooks.after(new Scenario(state));

        verify(runner).stop();
        verify(runner, never()).getTestCase();
    }

    @Test
    public void shouldOverwriteFailureState() {
        TestCase testCase = new DefaultTestCase();
        testCase.setTestResult(TestResult.success("foo", "FooClass"));

        when(state.getId()).thenReturn("mockedScenario");
        when(state.getName()).thenReturn("Mocked Scenario");
        when(state.getStatus()).thenReturn(Status.FAILED);
        when(state.isFailed()).thenReturn(true);
        when(runner.getTestCase()).thenReturn(testCase);
        citrusLifecycleHooks.after(new Scenario(state));

        Assert.assertTrue(testCase.getTestResult().isFailed());
        Assert.assertEquals(testCase.getTestResult().getCause().getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(testCase.getTestResult().getCause().getMessage(), "Scenario 'Mocked Scenario' (mockedScenario) status FAILED");

        verify(runner).stop();
        verify(runner, atLeastOnce()).getTestCase();
    }

    @Test
    public void shouldOverwriteEmptyTestResult() {
        TestCase testCase = new DefaultTestCase();

        when(state.getId()).thenReturn("mockedScenario");
        when(state.getName()).thenReturn("Mocked Scenario");
        when(state.getStatus()).thenReturn(Status.FAILED);
        when(state.isFailed()).thenReturn(true);
        when(runner.getTestCase()).thenReturn(testCase);
        citrusLifecycleHooks.after(new Scenario(state));

        Assert.assertTrue(testCase.getTestResult().isFailed());
        Assert.assertEquals(testCase.getTestResult().getCause().getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(testCase.getTestResult().getCause().getMessage(), "Scenario 'Mocked Scenario' (mockedScenario) status FAILED");

        verify(runner).stop();
        verify(runner, atLeastOnce()).getTestCase();
    }

    @Test
    public void shouldPreserveTestCaseFailure() {
        TestCase testCase = new DefaultTestCase();
        testCase.setTestResult(TestResult.failed("foo", "FooClass", new ValidationException("Error!")));

        when(state.isFailed()).thenReturn(true);
        when(runner.getTestCase()).thenReturn(testCase);
        citrusLifecycleHooks.after(new Scenario(state));

        Assert.assertTrue(testCase.getTestResult().isFailed());
        Assert.assertEquals(testCase.getTestResult().getCause().getClass(), ValidationException.class);
        Assert.assertEquals(testCase.getTestResult().getCause().getMessage(), "Error!");

        verify(runner).stop();
        verify(runner, atLeastOnce()).getTestCase();
    }
}
