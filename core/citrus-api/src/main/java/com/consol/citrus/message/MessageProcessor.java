package com.consol.citrus.message;

import com.consol.citrus.context.TestContext;

/**
 * Processor performs operations on the given message. The processor is able to change message content such as payload and headers.
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface MessageProcessor extends MessageTransformer {

    /**
     * Process message with given test context. Processors can change the message payload and headers.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    void process(Message message, TestContext context);

    /**
     * Adapt to message transformer API.
     * @param message the message to process.
     * @param context the current test context.
     * @return
     */
    default Message transform(Message message, TestContext context) {
        process(message, context);
        return message;
    }

    /**
     * Fluent builder
     * @param <T> processor type
     * @param <B> builder reference to self
     */
    interface Builder<T extends MessageProcessor, B extends Builder<T, B>> {

        /**
         * Builds new message processor instance.
         * @return the built processor.
         */
        T build();
    }
}
