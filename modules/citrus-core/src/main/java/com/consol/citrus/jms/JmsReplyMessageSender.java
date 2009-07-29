package com.consol.citrus.jms;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.HeaderMappingMessageConverter;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSender;


public class JmsReplyMessageSender implements MessageSender, InitializingBean {
    private JmsReplyDestinationHolder replyDestinationHolder;

    private ConnectionFactory connectionFactory;

    private JmsTemplate jmsTemplate;

    private JmsHeaderMapper headerMapper;

    private boolean initialized;

    private final Object initializationMonitor = new Object();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsReplyMessageSender.class);

    public JmsReplyMessageSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public JmsReplyMessageSender() {
    }

    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        Assert.notNull(replyDestinationHolder.getReplyDestination(), "No reply destination found");
        
        log.info("Sending message to: " + getDestinationName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        getJmsTemplate().convertAndSend(replyDestinationHolder.getReplyDestination(), message);
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }
    
    /**
     * @param replyDestinationHolder the replyDestinationHolder to set
     */
    public void setReplyDestinationHolder(
            JmsReplyDestinationHolder replyDestinationHolder) {
        this.replyDestinationHolder = replyDestinationHolder;
    }

    protected JmsTemplate getJmsTemplate() {
        if (this.jmsTemplate == null) {
            this.afterPropertiesSet();
        }
        return this.jmsTemplate;
    }
    
    /**
     * @return the destinationName
     */
    protected String getDestinationName() {
        try {
            if(replyDestinationHolder.getReplyDestination() != null) {
                if(replyDestinationHolder.getReplyDestination() instanceof Queue) {
                    return ((Queue)replyDestinationHolder.getReplyDestination()).getQueueName();
                } else if(replyDestinationHolder.getReplyDestination() instanceof Topic) {
                    return ((Topic)replyDestinationHolder.getReplyDestination()).getTopicName();
                } else {
                    return replyDestinationHolder.getReplyDestination().toString();
                }
            } else {
                return null;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }

    public void afterPropertiesSet() {
        synchronized (this.initializationMonitor) {
            if (this.initialized) {
                return;
            }
            
            Assert.notNull(replyDestinationHolder, "'replyDestinationHolder' is required");
            
            if (this.jmsTemplate == null) {
                Assert.isTrue(this.connectionFactory != null, "'connectionFactory'is required.");
                this.jmsTemplate = new JmsTemplate();
                jmsTemplate.setConnectionFactory(this.connectionFactory);
            }
            
            this.configureMessageConverter(this.jmsTemplate, this.headerMapper);
            this.initialized = true;
        }
    }
    
    protected void configureMessageConverter(JmsTemplate jmsTemplate, JmsHeaderMapper headerMapper) {
        MessageConverter converter = jmsTemplate.getMessageConverter();
        if (converter == null || !(converter instanceof HeaderMappingMessageConverter)) {
            HeaderMappingMessageConverter hmmc = new HeaderMappingMessageConverter(converter, headerMapper);
            hmmc.setExtractIntegrationMessagePayload(true);
            jmsTemplate.setMessageConverter(hmmc);
        }
    }
}
