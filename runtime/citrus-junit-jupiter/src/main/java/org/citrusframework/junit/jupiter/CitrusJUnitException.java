package org.citrusframework.junit.jupiter;

/**
 * Special {@link AssertionError} implementation used to fail a JUnit test in Citrus without printing a full stack trace.
 * <p>
 * This is useful in scenarios where the failure reason is already clearly communicated through Citrus test reporting or log output, and the additional stack trace would only add noise.
 * <p>
 * By overriding {@link #fillInStackTrace()}, this exception avoids populating the stack trace entirely, while still causing the test to be marked as failed by the JUnit engine.
 *
 * <p>
 * <b>Note:</b> The JUnit platform (or Citrus test runner) will still display the exception type and message in its failure summary.
 * To avoid even that, a different mechanism such as marking the test as skipped would be required.
 */
public class CitrusJUnitException extends AssertionError {

    public CitrusJUnitException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // don't populate the stack trace
    }
}
