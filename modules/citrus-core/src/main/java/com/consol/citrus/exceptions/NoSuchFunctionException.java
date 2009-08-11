package com.consol.citrus.exceptions;

/**
 * Unknown functions cause this exception.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 *
 */
public class NoSuchFunctionException extends CitrusRuntimeException {

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
