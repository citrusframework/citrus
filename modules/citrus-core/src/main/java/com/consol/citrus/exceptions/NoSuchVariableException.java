package com.consol.citrus.exceptions;

/**
 * Throw this exception in case a unknown variable is read from test context.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 10.08.2009
 *
 */
public class NoSuchVariableException extends CitrusRuntimeException {
    public NoSuchVariableException() {
        super();
    }

    public NoSuchVariableException(String message) {
        super(message);
    }

    public NoSuchVariableException(Throwable cause) {
        super(cause);
    }

    public NoSuchVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}
