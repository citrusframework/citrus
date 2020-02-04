package com.consol.citrus.message;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface MessageSelector {

    /**
     * Checks weather this selector should accept given message or not. When accepting the message the
     * selective consumer is provided with the message otherwise the message is skipped for this consumer.
     * @param message
     * @return true to accept the message, false to decline.
     */
    boolean accept(Message message);

    /**
     * Special message selector accepts all messages on queue.
     */
    final class AllAcceptingMessageSelector implements MessageSelector {
        public boolean accept(Message message) {
            return true;
        }
    }
}
