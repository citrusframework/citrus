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

package org.citrusframework.cucumber.backend;

import io.cucumber.core.backend.Status;
import io.cucumber.core.backend.TestCaseState;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.citrusframework.cucumber.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.base.annotations.CitrusAnnotations.injectTestContext;
import static org.citrusframework.base.annotations.CitrusAnnotations.injectTestRunner;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CitrusLifecycleHooksTest extends UnitTestSupport {

    @Mock
    private TestCaseState state;

    @Mock
    private TestCaseRunner runner;

    private CitrusLifecycleHooks fixture;

    @BeforeEach
    public void setupMocks() {
        fixture = new CitrusLifecycleHooks();

        injectTestContext(fixture, context);
        injectTestRunner(fixture, runner);
    }

    @Test
    public void shouldStartTestCaseRunner() {
        when(state.getId()).thenReturn("mockedScenario");
        when(state.getName()).thenReturn("This is a mocked scenario");

        fixture.before(new Scenario(state));
        verify(runner).name("This is a mocked scenario");
        verify(runner).description("mockedScenario");
        verify(runner).start();
    }

    @Test
    public void shouldHandleSuccessfulScenario() {
        when(state.isFailed()).thenReturn(false);
        fixture.after(new Scenario(state));

        verify(runner).stop();
        verify(runner, never()).getTestCase();
    }

    @Test
    public void shouldOverwriteFailureState() {
        TestCase testCase = new DefaultTestCase();
        testCase.setTestResult(success("shouldOverwriteFailureState", getClass().getSimpleName()));

        when(state.getId()).thenReturn("mockedScenario");
        when(state.getName()).thenReturn("Mocked Scenario");
        when(state.getStatus()).thenReturn(Status.FAILED);
        when(state.isFailed()).thenReturn(true);
        when(runner.getTestCase()).thenReturn(testCase);
        fixture.after(new Scenario(state));

        Assertions.assertTrue(testCase.getTestResult().isFailed());
        Assertions.assertEquals(CitrusRuntimeException.class, testCase.getTestResult().getCause().getClass());
        Assertions.assertEquals("Scenario 'Mocked Scenario' (mockedScenario) status FAILED", testCase.getTestResult().getCause().getMessage());

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
        fixture.after(new Scenario(state));

        Assertions.assertTrue(testCase.getTestResult().isFailed());
        Assertions.assertEquals(CitrusRuntimeException.class, testCase.getTestResult().getCause().getClass());
        Assertions.assertEquals("Scenario 'Mocked Scenario' (mockedScenario) status FAILED", testCase.getTestResult().getCause().getMessage());

        verify(runner).stop();
        verify(runner, atLeastOnce()).getTestCase();
    }

    @Test
    public void shouldPreserveTestCaseFailure() {
        TestCase testCase = new DefaultTestCase();

        var cause = new ValidationException("Error!");
        testCase.setTestResult(failed("shouldPreserveTestCaseFailure", getClass().getSimpleName(), cause));

        when(state.isFailed()).thenReturn(true);
        when(runner.getTestCase()).thenReturn(testCase);
        fixture.after(new Scenario(state));

        Assertions.assertTrue(testCase.getTestResult().isFailed());
        Assertions.assertEquals(cause, testCase.getTestResult().getCause());

        verify(runner).stop();
        verify(runner, atLeastOnce()).getTestCase();
    }
}
