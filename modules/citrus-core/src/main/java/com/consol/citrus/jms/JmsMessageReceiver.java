package com.consol.citrus.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.AbstractJmsTemplateBasedAdapter;
import org.springframework.integration.jms.HeaderMappingMessageConverter;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.message.GenericMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.MessageReceiver;

public class JmsMessageReceiver extends AbstractJmsTemplateBasedAdapter implements MessageReceiver {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsMessageReceiver.class);
    
    public Message<?> receive(long timeout) {
        log.info("Receiving message from: " + getDestinationName());
        
        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = getJmsTemplate().receiveAndConvert();
        
        if(receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving message on " + getDestinationName());
        }
        
        Message receivedMessage;
        if (receivedObject instanceof Message) {
            receivedMessage = (Message)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    public Message<?> receiveSelected(String selector, long timeout) {
        log.info("Receiving message from: " + getDestinationName() + "(" + selector + ")");
        
        Object receivedObject = getJmsTemplate().receiveSelectedAndConvert(selector);
        
        if(receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving message on " + getDestinationName());
        }
        
        Message receivedMessage;
        if (receivedObject instanceof Message) {
            receivedMessage = (Message)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    /**
     * @return the destinationName
     */
    protected String getDestinationName() {
        try {
            if(getJmsTemplate().getDefaultDestination() != null) {
                if(getJmsTemplate().getDefaultDestination() instanceof Queue) {
                    return ((Queue)getJmsTemplate().getDefaultDestination()).getQueueName();
                } else if(getJmsTemplate().getDefaultDestination() instanceof Topic) {
                    return ((Topic)getJmsTemplate().getDefaultDestination()).getTopicName();
                } else {
                    return getJmsTemplate().getDefaultDestination().toString();
                }
            } else {
                return getJmsTemplate().getDefaultDestinationName();
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }
    
    public Message<?> receive() {
        return receive(-1);
    }

    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, -1);
    }

    @Override
    protected void configureMessageConverter(JmsTemplate jmsTemplate, JmsHeaderMapper headerMapper) {
        MessageConverter converter = jmsTemplate.getMessageConverter();
        if (converter == null || !(converter instanceof HeaderMappingMessageConverter)) {
            HeaderMappingMessageConverter hmmc = new HeaderMappingMessageConverter(converter, headerMapper);
            hmmc.setExtractIntegrationMessagePayload(true);
            jmsTemplate.setMessageConverter(hmmc);
        }
    }
}
