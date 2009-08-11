package com.consol.citrus.exceptions;

/**
 * This exception is thrown when a message receiving action does not specify any content that is expected.
 */
public class MissingExpectedMessageException extends CitrusRuntimeException {

    public MissingExpectedMessageException() {
    }

    public MissingExpectedMessageException(String message) {
        super(message);
    }

    public MissingExpectedMessageException(Throwable cause) {
        super(cause);
    }

    public MissingExpectedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

}
