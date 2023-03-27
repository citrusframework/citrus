package org.citrusframework.citrus.endpoint.direct;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageQueue;
import org.citrusframework.citrus.message.correlation.CorrelationManager;
import org.citrusframework.citrus.message.correlation.PollingCorrelationManager;
import org.citrusframework.citrus.messaging.ReplyProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncConsumer extends DirectConsumer implements ReplyProducer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DirectSyncConsumer.class);

    /** Reply channel store */
    private CorrelationManager<MessageQueue> correlationManager;

    /** Endpoint configuration */
    private final DirectSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using emdpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public DirectSyncConsumer(String name, DirectSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply channel not set up yet");
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message receivedMessage = super.receive(selector, context, timeout);
        saveReplyMessageQueue(receivedMessage, context);

        return receivedMessage;
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Can not send empty message");

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = correlationManager.getCorrelationKey(correlationKeyName, context);
        MessageQueue replyQueue = correlationManager.find(correlationKey, endpointConfiguration.getTimeout());
        Assert.notNull(replyQueue, "Failed to find reply channel for message correlation key: " + correlationKey);

        if (log.isDebugEnabled()) {
            log.debug("Sending message to reply channel: '" + replyQueue + "'");
            log.debug("Message to send is:\n" + message.toString());
        }

        replyQueue.send(message);
        log.info("Message was sent to reply channel: '" + replyQueue + "'");
    }

    /**
     * Store reply message channel.
     * @param receivedMessage
     * @param context
     */
    public void saveReplyMessageQueue(Message receivedMessage, TestContext context) {
        MessageQueue replyQueue = null;
        if (receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE) instanceof MessageQueue) {
            replyQueue = (MessageQueue)receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE);
        } else if (StringUtils.hasText((String) receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE))) {
            replyQueue = resolveQueueName(receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE).toString(), context);
        }

        if (replyQueue != null) {
            String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage);
            correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
            correlationManager.store(correlationKey, replyQueue);
        } else {
            log.warn("Unable to retrieve reply message channel for message \n" +
                    receivedMessage + "\n - no reply channel found in message headers!");
        }
    }

    /**
     * Gets the correlation manager.
     * @return
     */
    public CorrelationManager<MessageQueue> getCorrelationManager() {
        return correlationManager;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<MessageQueue> correlationManager) {
        this.correlationManager = correlationManager;
    }
}
