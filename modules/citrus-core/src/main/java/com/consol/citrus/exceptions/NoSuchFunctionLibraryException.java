package com.consol.citrus.exceptions;

/**
 * In case no function library exists for a given prefix this exception is thrown.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class NoSuchFunctionLibraryException extends CitrusRuntimeException {

    public NoSuchFunctionLibraryException() {
        super();
    }

    public NoSuchFunctionLibraryException(String message) {
        super(message);
    }

    public NoSuchFunctionLibraryException(Throwable cause) {
        super(cause);
    }

    public NoSuchFunctionLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
