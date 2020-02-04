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
     * @return
     */
    boolean accept(Message message);
}
