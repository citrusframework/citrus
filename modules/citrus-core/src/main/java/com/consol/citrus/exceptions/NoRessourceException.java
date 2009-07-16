package com.consol.citrus.exceptions;

/**
 * Custom exception
 */
public class NoRessourceException extends TestSuiteException {

    private static final long serialVersionUID = 462472789642855456L;

    public NoRessourceException() {
    }

    public NoRessourceException(String message) {
        super(message);
    }

    public NoRessourceException(Throwable cause) {
        super(cause);
    }

    public NoRessourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
