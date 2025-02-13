package org.citrusframework.context;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.TestListener;
import org.citrusframework.report.TestListeners;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TestContextUnitTest {

    private TestContext fixture;

    @BeforeTest
    void beforeTestClass() {
        fixture = new TestContext();
    }

    @Test
    public void handleErrorGracefullyHandlesErrorInTestStartListener() {
        var testListenerMock = attachTestListenerMockToFixture();

        var cause = new CitrusRuntimeException("thrown with a purpose!");
        doThrow(cause).when(testListenerMock).onTestStart(any(TestContext.EmptyTestCase.class));

        invokeHandleErrorOnFixture(cause);

        verify(testListenerMock).onTestStart(any(TestContext.EmptyTestCase.class));
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void handleErrorGracefullyHandlesErrorInTestFailureListener() {
        var testListenerMock = attachTestListenerMockToFixture();

        var cause = new CitrusRuntimeException("thrown with a purpose!");
        doThrow(cause).when(testListenerMock).onTestFailure(any(TestContext.EmptyTestCase.class), any(CitrusRuntimeException.class));

        invokeHandleErrorOnFixture(cause);

        verify(testListenerMock).onTestStart(any(TestContext.EmptyTestCase.class));
        verify(testListenerMock).onTestFailure(any(TestContext.EmptyTestCase.class), any(CitrusRuntimeException.class));
        verifyNoMoreInteractions(testListenerMock);
    }

    @Test
    public void handleErrorGracefullyHandlesErrorInTestFinishListener() {
        var testListenerMock = attachTestListenerMockToFixture();

        var cause = new CitrusRuntimeException("thrown with a purpose!");
        doThrow(cause).when(testListenerMock).onTestExecutionEnd(any(TestContext.EmptyTestCase.class));

        invokeHandleErrorOnFixture(cause);

        verify(testListenerMock).onTestStart(any(TestContext.EmptyTestCase.class));
        verify(testListenerMock).onTestFailure(any(TestContext.EmptyTestCase.class), any(CitrusRuntimeException.class));
        verify(testListenerMock).onTestExecutionEnd(any(TestContext.EmptyTestCase.class));
    }

    private TestListener attachTestListenerMockToFixture() {
        var testListeners = new TestListeners();

        var testListenerMock = mock(TestListener.class);
        testListeners.addTestListener(testListenerMock);

        fixture.setTestListeners(testListeners);

        return testListenerMock;
    }

    private void invokeHandleErrorOnFixture(CitrusRuntimeException cause) {
        var testName = "test name";
        var packageName = "package name";

        var message = "additional message";

        var citrusRuntimeException = fixture.handleError(testName, packageName, message, cause);
        assertEquals(citrusRuntimeException.getMessage(), message);
        assertEquals(citrusRuntimeException.getCause(), cause);
    }
}
