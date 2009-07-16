package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class JmsTimeoutException extends TestSuiteException {

    private static final long serialVersionUID = -8652778602073652873L;

    public JmsTimeoutException() {
        super();
    }

    public JmsTimeoutException(String message) {
        super(message);
    }

    public JmsTimeoutException(Throwable cause) {
        super(cause);
    }

    public JmsTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
