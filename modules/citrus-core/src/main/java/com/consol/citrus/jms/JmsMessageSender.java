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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSender;

public class JmsMessageSender extends AbstractJmsTemplateBasedAdapter implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsMessageSender.class);
    
    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDestinationName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        getJmsTemplate().convertAndSend(message);
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
