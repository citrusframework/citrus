package com.consol.citrus.service;

import java.util.Enumeration;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.consol.citrus.exceptions.JmsTimeoutException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.XMLMessage;
import com.consol.citrus.util.XMLUtils;

/**
 * JmsService to send and receive xml messages using JMS
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 *
 */
public class JmsService implements Service, InitializingBean {
    /**
     * Spring JMS template
     */
    private JmsTemplate jmsTemplate;

    /**
     * Queue name as service destination
     */
    private String serviceDestination;

    /**
     * Optional replyToQueue to be set by service beans
     * When sending messages this queue will be placed as JMSReplyTo queue into the message
     */
    private String replyToQueue;

    /**
     * Select messages to receive
     */
    private String messageSelector;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsService.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#sendMessage(java.lang.String)
     */
    public void sendMessage(final Message message) {
        log.info("Sending message to: " + getServiceDestination());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(XMLUtils.prettyPrint(message.getMessagePayload()));
        }

        /* use jms template to send message */
        jmsTemplate.send(new MessageCreator() {
            public javax.jms.Message createMessage(Session session) throws JMSException {
                javax.jms.TextMessage msg = session.createTextMessage(message.getMessagePayload());
                /* before sending set special header values */
                Iterator it = message.getHeader().keySet().iterator();
                while (it.hasNext())
                {
                    Object o = it.next();
                    final String key = (String) o;
                    final String value = (String) message.getHeader().get(key);

                    if (log.isDebugEnabled()) {
                        log.debug("Setting JMS message property: " + key + " to: " + value);
                    }

                    if (o.equals("JMSCorrelationID")) {
                        msg.setJMSCorrelationID(value);
                    } else if (o.equals("JMSDeliveryMode")) {
                        msg.setJMSDeliveryMode(Integer.valueOf(value).intValue());
                    } else if (o.equals("JMSDestination")) {
                        msg.setJMSDestination(jmsTemplate.getDestinationResolver().resolveDestinationName(session, value, true));
                    } else if (o.equals("JMSExpiration")) {
                        msg.setJMSExpiration(Long.valueOf(value).longValue());
                    } else if (o.equals("JMSMessageID")) {
                        msg.setJMSMessageID(value);
                    } else if (o.equals("JMSPriority")) {
                        msg.setJMSPriority(Integer.valueOf(value).intValue());
                    } else if (o.equals("JMSRedelivered")) {
                        msg.setJMSRedelivered(Boolean.valueOf(value).booleanValue());
                    } else if (o.equals("JMSReplyTo")) {
                        msg.setJMSReplyTo(jmsTemplate.getDestinationResolver().resolveDestinationName(session, value, true));
                    } else if (o.equals("JMSTimestamp")) {
                        msg.setJMSTimestamp(Long.valueOf(value).longValue());
                    } else if (o.equals("JMSType")) {
                        msg.setJMSType(value);
                    } else {
                        msg.setStringProperty(key, value);
                    }
                }

                /* if replyToQueue is present place it into the jms message */
                if (replyToQueue != null) {
                    log.info("Set replyToQueue in JMS message: " + replyToQueue);
                    msg.setJMSReplyTo(jmsTemplate.getDestinationResolver().resolveDestinationName(session, replyToQueue, false));
                }

                return msg;
            }
        });
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#receiveMessage()
     */
    public Message receiveMessage() throws JmsTimeoutException, TestSuiteException {
        try {
            log.info("Receiving message from: " + getServiceDestination());

            /* use jms template to receive message */
            final javax.jms.TextMessage jmsMessage;

            if (messageSelector != null && messageSelector.length() > 0) {
                jmsMessage = (javax.jms.TextMessage)jmsTemplate.receiveSelected(messageSelector);
            } else {
                jmsMessage = (javax.jms.TextMessage)jmsTemplate.receive();
            }

            if (jmsMessage == null) throw new JmsTimeoutException("Action timed out while receiving message on " + getServiceDestination());

            Message message = new XMLMessage();
            message.setMessagePayload(jmsMessage.getText());

            /* get header values from message and store them into the context */
            Enumeration headerValues = jmsMessage.getPropertyNames();
            while (headerValues.hasMoreElements())
            {
                String key = (String)headerValues.nextElement();
                String value = jmsMessage.getStringProperty(key);

                message.getHeader().put(key, value);
            }

            message.getHeader().put("JMSCorrelationID", jmsMessage.getJMSCorrelationID());
            message.getHeader().put("JMSDeliveryMode", Integer.valueOf(jmsMessage.getJMSDeliveryMode()).toString());

            if (jmsMessage.getJMSDestination() != null)
                message.getHeader().put("JMSDestination", ((Queue)jmsMessage.getJMSDestination()).getQueueName());

            message.getHeader().put("JMSExpiration", Long.valueOf(jmsMessage.getJMSExpiration()).toString());
            message.getHeader().put("JMSMessageID", jmsMessage.getJMSMessageID());
            message.getHeader().put("JMSPriority", Integer.valueOf(jmsMessage.getJMSPriority()).toString());
            message.getHeader().put("JMSRedelivered", Boolean.valueOf(jmsMessage.getJMSRedelivered()).toString());

            if (jmsMessage.getJMSReplyTo() != null)
                message.getHeader().put("JMSReplyTo", ((Queue)jmsMessage.getJMSReplyTo()).getQueueName());

            message.getHeader().put("JMSTimestamp", Long.valueOf(jmsMessage.getJMSTimestamp()).toString());
            message.getHeader().put("JMSType", jmsMessage.getJMSType());

            if(log.isDebugEnabled()) {
                log.debug("Message received:");
                log.debug(XMLUtils.prettyPrint(jmsMessage.getText()));
            }

            return message;
        } catch (JmsException e) {
            throw new TestSuiteException(e);
        } catch (JMSException e) {
            throw new TestSuiteException(e);
        }
    }

    /**
     * Setter for JmsTemplate
     * @param jmsTemplate
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#getServiceDestination()
     */
    public String getServiceDestination() {
        return jmsTemplate.getDefaultDestinationName();
    }

    /**
     * Getter for JmSTemplate
     * @return the JmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    /**
     * Setter for service destination
     * @param destination
     */
    public void setServiceDestination(String destination) {
        this.serviceDestination = destination;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        jmsTemplate.setDefaultDestinationName(serviceDestination);
    }

    /**
     * Setter for replyToQueue
     * @param replyToQueue
     */
    public void setReplyToQueue(String replyToQueue) {
        this.replyToQueue = replyToQueue;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#changeServiceDestination(java.lang.String)
     */
    public void changeServiceDestination(String destination) throws TestSuiteException {
        setServiceDestination(destination);
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            throw new TestSuiteException(e);
        }
    }

    /**
     * Getter for replyToQueue
     * @return String
     */
    public String getReplyToQueue() {
        return replyToQueue;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }
}
