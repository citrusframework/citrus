package com.consol.citrus.exceptions;

/**
 * Basic sustom exception for all errors in test suite
 */
public class TestSuiteException extends RuntimeException {

    private static final long serialVersionUID = 4412595135371710073L;

    public TestSuiteException() {
    }

    public TestSuiteException(String message) {
        super(message);
    }

    public TestSuiteException(Throwable cause) {
        super(cause);
    }

    public TestSuiteException(String message, Throwable cause) {
        super(message, cause);
    }

}
