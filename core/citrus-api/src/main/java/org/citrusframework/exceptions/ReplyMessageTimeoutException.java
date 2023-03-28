package org.citrusframework.exceptions;

/**
 * @author Christoph Deppisch
 */
public class ReplyMessageTimeoutException extends MessageTimeoutException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     */
    public ReplyMessageTimeoutException(long timeout, String endpoint) {
        super(timeout, endpoint);
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     * @param cause
     */
    public ReplyMessageTimeoutException(long timeout, String endpoint, Throwable cause) {
        super(timeout, endpoint, cause);
    }

    @Override
    public String getDetailMessage() {
        if (timeout <=0 && endpoint == null) {
            return "Failed to receive synchronous reply message.";
        }

        return String.format("Failed to receive synchronous reply message on endpoint: '%s'", endpoint);
    }

}
