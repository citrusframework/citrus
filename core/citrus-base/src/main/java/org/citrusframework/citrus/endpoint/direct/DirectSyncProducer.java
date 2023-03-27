package org.citrusframework.citrus.endpoint.direct;

import java.util.UUID;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.MessageTimeoutException;
import org.citrusframework.citrus.exceptions.ReplyMessageTimeoutException;
import org.citrusframework.citrus.message.DefaultMessageQueue;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageQueue;
import org.citrusframework.citrus.message.correlation.CorrelationManager;
import org.citrusframework.citrus.message.correlation.PollingCorrelationManager;
import org.citrusframework.citrus.messaging.ReplyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncProducer extends DirectProducer implements ReplyConsumer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DirectSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Endpoint configuration */
    private final DirectSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     *
     * @param name
     * @param endpointConfiguration
     */
    public DirectSyncProducer(String name, DirectSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String destinationQueueName = getDestinationQueueName();

        if (log.isDebugEnabled()) {
            log.debug("Sending message to queue: '" + destinationQueueName + "'");
            log.debug("Message to send is:\n" + message.toString());
        }

        log.info("Message was sent to queue: '" + destinationQueueName + "'");

        MessageQueue replyQueue = getReplyQueue(message, context);
        getDestinationQueue(context).send(message);
        Message replyMessage = replyQueue.receive(endpointConfiguration.getTimeout());

        if (replyMessage == null) {
            throw new ReplyMessageTimeoutException(endpointConfiguration.getTimeout(), destinationQueueName);
        } else {
            log.info("Received synchronous response from reply queue");
        }

        correlationManager.store(correlationKey, replyMessage);
    }

    /**
     * Reads reply queue from message header or creates a new temporary queue.
     * @param message
     * @param context
     * @return
     */
    private MessageQueue getReplyQueue(Message message, TestContext context) {
        if (message.getHeader(DirectMessageHeaders.REPLY_QUEUE) == null) {
            MessageQueue temporaryQueue = new DefaultMessageQueue(getName() + "." + UUID.randomUUID().toString());
            message.setHeader(DirectMessageHeaders.REPLY_QUEUE, temporaryQueue);
            return temporaryQueue;
        }

        if (message.getHeader(DirectMessageHeaders.REPLY_QUEUE) instanceof MessageQueue) {
            return (MessageQueue)message.getHeader(DirectMessageHeaders.REPLY_QUEUE);
        } else {
            return resolveQueueName(message.getHeader(DirectMessageHeaders.REPLY_QUEUE).toString(), context);
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, getDestinationQueueName());
        }

        return message;
    }

    /**
     * Gets the correlation manager.
     * @return
     */
    public CorrelationManager<Message> getCorrelationManager() {
        return correlationManager;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }
}
