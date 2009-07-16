package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class ValidationException extends TestSuiteException {

    private static final long serialVersionUID = 5568293757540239407L;

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
