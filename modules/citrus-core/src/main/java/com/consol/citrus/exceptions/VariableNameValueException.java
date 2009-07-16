package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class VariableNameValueException extends TestSuiteException {

    private static final long serialVersionUID = 2568473310165193726L;

    public VariableNameValueException() {
        super();
    }

    public VariableNameValueException(String message) {
        super(message);
    }

    public VariableNameValueException(Throwable cause) {
        super(cause);
    }

    public VariableNameValueException(String message, Throwable cause) {
        super(message, cause);
    }

}
