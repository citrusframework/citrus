package org.citrusframework;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.citrusframework.TestResult.RESULT.FAILURE;
import static org.citrusframework.TestResult.RESULT.SKIP;
import static org.citrusframework.TestResult.RESULT.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals("TestResult[testName=minimalToString, className=TestResultTest, result=SUCCESS]", fixture.toString());
    }

    @Test
    void toStringWithParameters() {
        var fixture = new TestResult(FAILURE, "toStringWithParameters", getClass().getSimpleName());
        fixture.getParameters().put("parameter 1", "value 1");
        fixture.getParameters().put("parameter 2", "value 2");

        assertEquals("TestResult[testName=toStringWithParameters, className=TestResultTest, parameters=[parameter 1=value 1, parameter 2=value 2], result=FAILURE]", fixture.toString());
    }

    @Test
    void toStringWithDuration() {
        var fixture = new TestResult(SKIP, "toStringWithDuration", getClass().getSimpleName());
        fixture.withDuration(Duration.ofMillis(1234L));

        assertEquals("TestResult[testName=toStringWithDuration, className=TestResultTest, result=SKIP, duration=1234ms]", fixture.toString());
    }
}
