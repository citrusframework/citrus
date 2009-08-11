package com.consol.citrus.exceptions;

/**
 * Thrown in case an element is not found. Usually used in validation procedures.
 */
public class UnknownElementException extends CitrusRuntimeException {

    public UnknownElementException() {
        super();
    }

    public UnknownElementException(String message) {
        super(message);
    }

    public UnknownElementException(Throwable cause) {
        super(cause);
    }

    public UnknownElementException(String message, Throwable cause) {
        super(message, cause);
    }
}
