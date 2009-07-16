package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class InvalidFunctionUsageException extends TestSuiteException {

    public InvalidFunctionUsageException() {
        super();
    }

    public InvalidFunctionUsageException(String message) {
        super(message);
    }

    public InvalidFunctionUsageException(Throwable cause) {
        super(cause);
    }

    public InvalidFunctionUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
