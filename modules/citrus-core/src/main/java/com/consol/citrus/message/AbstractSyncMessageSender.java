package com.consol.citrus.message;

import com.consol.citrus.TestActor;
import com.consol.citrus.endpoint.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract base class for message sender with functionality common for all
 * synchronous message senders. Synchronous communication requires a {@link ReplyMessageHandler} to be
 * informed about receipt of synchronous response messages for further processing.
 * 
 * In parallel testing reply messages need message correlation via {@link ReplyMessageCorrelator} implementation.
 *
 * @author Christoph Deppisch, roland
 * @since 06.09.12
 */
abstract public class AbstractSyncMessageSender implements MessageSender, BeanNameAware {

    /** Logger */
    protected  Logger log = LoggerFactory.getLogger(getClass());

    /** The reply message handler */
    protected ReplyMessageHandler replyMessageHandler;

    /** Message endpoint */
    private final Endpoint endpoint;

    protected AbstractSyncMessageSender(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
    
    /**
     * Gets the replyMessageHandler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return endpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        endpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        endpoint.setName(name);
    }

    @Override
    public String getName() {
        return endpoint.getName();
    }

    @Override
    public void setName(String name) {
        endpoint.setName(name);
    }
}
