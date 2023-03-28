package org.citrusframework.message;

/**
 * @author Christoph Deppisch
 */
public interface MessageQueue {

    /**
     * Send new message to queue.
     * @param message
     */
    void send(Message message);

    /**
     * Receive any message on the queue. If no message is present return null.
     * @return the first message on the queue or null if no message available.
     */
    default Message receive() {
        return receive((message) -> true);
    }

    /**
     * Receive any message on the queue. Operation blocks until a message is present on the queue or
     * the given timeout is reached.
     * @param timeout time to wait for a message.
     * @return message or null if no matching message is available.
     */
    default Message receive(long timeout) {
        return receive((message) -> true, timeout);
    }

    /**
     * Supports selective consumption of messages on the queue. The first message
     * to be accepted by given message selector is returned as result. Operation is not blocking. In case no
     * matching message is present in the queue null is returned.
     *
     * @param selector must accept the message to consume.
     * @return message or null if no matching message is available.
     */
    Message receive(MessageSelector selector);

    /**
     * Consume messages on the queue via message selector. Operation blocks until a matching message is present on the queue or
     * the given timeout is reached.
     *
     * @param selector must accept message to consume.
     * @param timeout time to wait for a matching message.
     * @return message or null if no matching message is available.
     */
    Message receive(MessageSelector selector, long timeout);

    /**
     * Purge messages selected by given selector.
     * @param selector
     */
    void purge(MessageSelector selector);
}
