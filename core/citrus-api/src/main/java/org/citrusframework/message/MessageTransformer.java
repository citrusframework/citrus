package org.citrusframework.message;

import org.citrusframework.context.TestContext;

/**
 * Transformer is able to completely change a given message.
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface MessageTransformer {

    /**
     * Transform message with given test context and return new message.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    Message transform(Message message, TestContext context);

    /**
     * Fluent builder
     * @param <T> transformer type
     * @param <B> builder reference to self
     */
    interface Builder<T extends MessageTransformer, B extends Builder<T, B>> {

        /**
         * Builds new message processor instance.
         * @return the built processor.
         */
        T build();
    }
}
