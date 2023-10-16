package org.citrusframework.endpoint.direct;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectProducer implements Producer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DirectProducer.class);

    /** The producer name */
    private final String name;

    /** Endpoint configuration*/
    private final DirectEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public DirectProducer(String name, DirectEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, TestContext context) {
        String destinationQueueName = getDestinationQueueName();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Sending message to queue: '%s'", destinationQueueName));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Message to send is:" + System.getProperty("line.separator") + message.toString());
        }

        try {
            getDestinationQueue(context).send(message);
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to send message to queue: '%s'", destinationQueueName), e);
        }

        logger.info(String.format("Message was sent to queue: '%s'", destinationQueueName));
    }

    /**
     * Get the destination queue depending on settings in this message sender.
     * Either a direct queue object is set or a queue name which will be resolved
     * to a queue.
     *
     * @return the destination queue object.
     * @param context
     */
    protected MessageQueue getDestinationQueue(TestContext context) {
        if (endpointConfiguration.getQueue() != null) {
            return endpointConfiguration.getQueue();
        } else if (StringUtils.hasText(endpointConfiguration.getQueueName())) {
            return resolveQueueName(endpointConfiguration.getQueueName(), context);
        } else {
            throw new CitrusRuntimeException("Neither queue name nor queue object is set - " +
                    "please specify destination queue");
        }
    }

    /**
     * Gets the queue name depending on what is set in this message sender.
     * Either queue name is set directly or queue object is consulted for queue name.
     *
     * @return the queue name.
     */
    protected String getDestinationQueueName() {
        if (endpointConfiguration.getQueue() != null) {
            return endpointConfiguration.getQueue().toString();
        } else if (StringUtils.hasText(endpointConfiguration.getQueueName())) {
            return endpointConfiguration.getQueueName();
        } else {
            throw new CitrusRuntimeException("Neither queue name nor queue object is set - " +
                    "please specify destination queue");
        }
    }

    /**
     * Resolve the queue by name.
     * @param queueName the name to resolve
     * @param context the test context
     * @return the MessageQueue object
     */
    protected MessageQueue resolveQueueName(String queueName, TestContext context) {
        if (context.getReferenceResolver() != null) {
            return context.getReferenceResolver().resolve(queueName, MessageQueue.class);
        }

        throw new CitrusRuntimeException("Unable to resolve message queue - missing proper reference resolver in context");
    }

    @Override
    public String getName() {
        return name;
    }
}
