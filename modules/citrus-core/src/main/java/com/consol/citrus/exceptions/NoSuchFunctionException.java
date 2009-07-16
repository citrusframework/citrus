package com.consol.citrus.exceptions;

public class NoSuchFunctionException extends TestSuiteException {

    public NoSuchFunctionException() {
        super();
    }

    public NoSuchFunctionException(String message) {
        super(message);
    }

    public NoSuchFunctionException(Throwable cause) {
        super(cause);
    }

    public NoSuchFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
