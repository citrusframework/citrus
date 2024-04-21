package org.citrusframework.report;

import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;

public class TestReportersTest {

    @Mock
    private TestCase testCaseMock;

    private TestReporters fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        openMocks(this);

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

        assertNotEquals(fixture.getTestResults(), testResults);
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
    public void onTestFinishAddsTestResultToList() {
        var testResultMock = mock(TestResult.class);
        doReturn(testResultMock).when(testCaseMock).getTestResult();

        fixture.onTestFinish(testCaseMock);

        var testResults = fixture.getTestResults().asList();
        assertEquals(1, testResults.size());
        assertEquals(testResultMock, testResults.get(0));
    }

    @Test
    public void onTestFinishIgnoresNullTestResult() {
        doReturn(null).when(testCaseMock).getTestResult();

        fixture.onTestFinish(testCaseMock);

        var testResults = fixture.getTestResults().asList();
        assertEquals(0, testResults.size());
    }
}
