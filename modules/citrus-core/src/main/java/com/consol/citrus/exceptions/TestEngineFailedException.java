package com.consol.citrus.exceptions;

/**
 * Custom RuntimeException thrown if test run failed, because some tests were not successful.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 *
 */
public class TestEngineFailedException extends RuntimeException {
    public TestEngineFailedException() {
    }

    public TestEngineFailedException(String message) {
        super(message);
    }

    public TestEngineFailedException(Throwable cause) {
        super(cause);
    }

    public TestEngineFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
