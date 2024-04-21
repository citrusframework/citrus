package org.citrusframework;

import org.testng.annotations.Test;

import java.time.Duration;

import static org.citrusframework.TestResult.RESULT.FAILURE;
import static org.citrusframework.TestResult.RESULT.SKIP;
import static org.citrusframework.TestResult.RESULT.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.Assert.assertEquals;

public class TestResultTest {

    @Test
    public void isSuccess() {
        var fixture = new TestResult(SUCCESS, "isSuccess", getClass().getSimpleName());

        assertTrue(fixture.isSuccess());
        assertFalse(fixture.isFailed());
        assertFalse(fixture.isSkipped());
    }

    @Test
    public void isFailed() {
        var fixture = new TestResult(FAILURE, "isFailed", getClass().getSimpleName());

        assertFalse(fixture.isSuccess());
        assertTrue(fixture.isFailed());
        assertFalse(fixture.isSkipped());
    }

    @Test
    public void isSkipped() {
        var fixture = new TestResult(SKIP, "isSkipped", getClass().getSimpleName());

        assertFalse(fixture.isSuccess());
        assertFalse(fixture.isFailed());
        assertTrue(fixture.isSkipped());
    }

    @Test
    public void nullTestResult() {
        var fixture = new TestResult(null, "nullTestResult", getClass().getSimpleName());

        assertFalse(fixture.isSuccess());
        assertFalse(fixture.isFailed());
        assertFalse(fixture.isSkipped());
    }

    @Test
    void minimalToString() {
        var fixture = new TestResult(SUCCESS, "minimalToString", getClass().getSimpleName());

        assertEquals(fixture.toString(), "TestResult[testName=minimalToString, result=SUCCESS]");
    }

    @Test
    void toStringWithParameters() {
        var fixture = new TestResult(FAILURE, "toStringWithParameters", getClass().getSimpleName());
        fixture.getParameters().put("parameter 1", "value 1");
        fixture.getParameters().put("parameter 2", "value 2");

        assertEquals(fixture.toString(), "TestResult[testName=toStringWithParameters, parameters=[parameter 1=value 1, parameter 2=value 2], result=FAILURE]");
    }

    @Test
    void toStringWithDuration() {
        var fixture = new TestResult(SKIP, "toStringWithDuration", getClass().getSimpleName());
        fixture.withDuration(Duration.ofMillis(1234L));

        assertEquals(fixture.toString(), "TestResult[testName=toStringWithDuration, result=SKIP, durationMs=1234]");
    }
}
