package com.consol.citrus.exceptions;

/**
 * Basic custom runtime exception for all errors in Citrus
 */
public class CitrusRuntimeException extends RuntimeException {

    public CitrusRuntimeException() {
    }

    public CitrusRuntimeException(String message) {
        super(message);
    }

    public CitrusRuntimeException(Throwable cause) {
        super(cause);
    }

    public CitrusRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
