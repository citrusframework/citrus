package org.citrusframework.report;

import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class TestReportersTest {

    @Mock
    private TestCase testCaseMock;

    private TestReporters fixture;

    @BeforeEach
    void beforeEach() {

        fixture = new TestReporters();
    }

    @Test
    void onStartWithoutAutoClear() {
        var testResults = fixture.getTestResults();

        fixture.setAutoClear(false);
        fixture.onStart();

        assertEquals(fixture.getTestResults(), testResults);
    }

    @Test
    void onStartWithAutoClear() {
        var testResults = fixture.getTestResults();

        fixture.setAutoClear(true);
        fixture.onStart();

        assertNotEquals(testResults, fixture.getTestResults());
    }

    @Test
    void onFinishSuccessGeneratesReports() {
        verifyGenerateReport(() -> fixture.onFinishSuccess());
    }

    @Test
    void onFinishFailureGeneratesReports() {
        var cause = mock(Throwable.class);

        verifyGenerateReport(() -> fixture.onFinishFailure(cause));

        verifyNoInteractions(cause);
    }

    private void verifyGenerateReport(Runnable invocation) {
        var testReporter1 = mock(TestReporter.class);
        fixture.addTestReporter(testReporter1);

        var testReporter2 = mock(TestReporter.class);
        fixture.addTestReporter(testReporter2);

        invocation.run();

        verify(testReporter1).generateReport(any(TestResults.class));
        verify(testReporter2).generateReport(any(TestResults.class));
    }

    @Test
    public void onTestExecutionEndAddsTestResultToList() {
        var testResultMock = mock(TestResult.class);
        doReturn(testResultMock).when(testCaseMock).getTestResult();

        fixture.onTestExecutionEnd(testCaseMock);

        var testResults = fixture.getTestResults().asList();
        assertEquals(1, testResults.size());
        assertEquals(testResultMock, testResults.get(0));
    }

    @Test
    public void onTestExecutionEndIgnoresNullTestResult() {
        doReturn(null).when(testCaseMock).getTestResult();

        fixture.onTestExecutionEnd(testCaseMock);

        var testResults = fixture.getTestResults().asList();
        assertEquals(0, testResults.size());
    }
}
