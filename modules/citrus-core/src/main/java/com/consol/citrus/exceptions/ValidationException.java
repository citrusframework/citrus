package com.consol.citrus.exceptions;

/**
 * If validation fails throw this exception
 */
public class ValidationException extends CitrusRuntimeException {

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
