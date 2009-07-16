package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class UnknownElementException extends TestSuiteException {

    /**
     *
     */
    private static final long serialVersionUID = -8652778602073652872L;

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
