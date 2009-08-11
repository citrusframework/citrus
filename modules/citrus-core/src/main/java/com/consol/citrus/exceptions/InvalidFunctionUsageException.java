package com.consol.citrus.exceptions;

/**
 * Use this exception in case a function is called with invalid parameters.
 */
public class InvalidFunctionUsageException extends CitrusRuntimeException {

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
