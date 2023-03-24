package org.citrusframework.exceptions;

/**
 * @author Christoph Deppisch
 */
public class MessageTimeoutException extends ActionTimeoutException {

    protected final String endpoint;

    /**
     * Default constructor
     */
    public MessageTimeoutException() {
        this(0L, "");
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     */
    public MessageTimeoutException(long timeout, String endpoint) {
        super(timeout);
        this.endpoint = endpoint;
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     * @param cause
     */
    public MessageTimeoutException(long timeout, String endpoint, Throwable cause) {
        super(timeout, cause);
        this.endpoint = endpoint;
    }

    @Override
    public String getDetailMessage() {
        if (timeout <=0 && endpoint == null) {
            return "Failed to receive message.";
        }

        return String.format("Failed to receive message on endpoint: '%s'", endpoint);
    }
}
