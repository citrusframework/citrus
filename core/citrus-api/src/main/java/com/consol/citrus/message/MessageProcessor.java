package com.consol.citrus.message;

import com.consol.citrus.context.TestContext;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface MessageProcessor {

    /**
     * Process message with given test context and return processed message.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    Message process(Message message, TestContext context);
}
