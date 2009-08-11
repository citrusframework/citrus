package com.consol.citrus.exceptions;

/**
 * Throw this exception in case you did not receive a message on a destination in time.
 * Used in message receivers to state that message did not arrive in time.
 */
public class ActionTimeoutException extends CitrusRuntimeException {

    private static final long serialVersionUID = -8652778602073652873L;

    public ActionTimeoutException() {
        super();
    }

    public ActionTimeoutException(String message) {
        super(message);
    }

    public ActionTimeoutException(Throwable cause) {
        super(cause);
    }

    public ActionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
