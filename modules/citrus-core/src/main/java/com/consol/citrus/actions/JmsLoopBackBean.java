package com.consol.citrus.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.server.Server;
import com.consol.citrus.variable.GlobalVariables;

/**
 * Special loop back bean for jms.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 01.03.2007
 */
public class JmsLoopBackBean implements InitializingBean, Server {
    /**
     * A map containing the path names of XML elements as keys and values or
     * variable values to be set within the XML message.
     */
    protected HashMap setMessageValues = new HashMap();
    /**
     * A map containing string properties as keys and values or variable values
     * to be set within service message header.
     */
    protected HashMap setHeaderValues = new HashMap();
    /**
     * A map containing the path names of XML elements as keys and variable
     * names as values to be filled with values from the XML message.
     */
    protected HashMap getMessageValues = new HashMap();
    /**
     * A map containing string properties as keys and variable names as values
     * to be filled with values from the service message header.
     */
    protected HashMap getHeaderValues = new HashMap();

    /**
     * The XML ressource as file resource
     */
    protected Resource xmlResource;

    /**
     * The XML ressource as inline definition as CDATA
     */
    protected String xmlData;

    /**
     * Jms server
     */
    private String serverUrl;
    /**
     * Jms user
     */
    private String userName;
    /**
     * Jms password
     */
    private String userPassword;

    /**
     * Delay of loopback response in milisecs
     */
    private long responseDelayMilisec;

    /**
     * Queue destination names
     */
    private String receiveDestination;
    private String sendDestination;

    /**
     * Select messages to receive
     */
    private String messageSelector;

    /**
     * Alive for thread
     */
    private boolean running = true;

    /**
     * Queue connection factory jms implementation
     */
    private ActiveMQConnectionFactory queueFactory;

    /**
     * Time to wait for response
     */
    private long timeout = 5000L;

    /**
     * Name of server
     */
    private String name;

    /**
     * Queue connection
     */
    QueueConnection qcon = null;

    /**
     * Number of server instances to invoke
     */
    private int countInstances = 1;

    /**
     * Reporting quantities
     */
    private int throughput = 0;
    private long throughputTime = 0L;
    
    @Autowired
    FunctionRegistry functionRegistry;
    
    @Autowired
    GlobalVariables globalVariables;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsLoopBackBean.class);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        TestContext context = new TestContext();
        context.setFunctionRegistry(functionRegistry);
        context.setGlobalVariables(globalVariables);
        
        Map headerValues = new HashMap();
        QueueReceiver qreceiver;
        QueueSession qsession;
        Queue queue;

        String operationInfo = "";
        if (messageSelector != null) {
            operationInfo = "(" + messageSelector + ")";
        }

        try {
            if(log.isDebugEnabled()) {
                log.debug("[JMSLoopBack] establishing connection to " + serverUrl);
            }

            if (qcon == null) {
                qcon = queueFactory.createQueueConnection(userName, userPassword);
                qcon.start();
            }

            qsession = qcon.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            queue = qsession.createQueue(receiveDestination);
            qreceiver = qsession.createReceiver(queue, messageSelector);

            if(log.isDebugEnabled()) {
                log.debug("[JMSLoopBack] established connection: " + qcon + " session: " + qsession);
            }
            log.info("[JMSLoopBack] Listening on destination " + receiveDestination + operationInfo);
        } catch (Exception ex) {
            log.error("Unexpected error setting up new queue session", ex);
            throw new RuntimeException(ex);
        }

        while (isRunning()) {
            TextMessage message = null;
            try {
                message = (TextMessage) qreceiver.receive(timeout);
            } catch (JMSException e) {
                log.error("Error while receiving JMS message", e);
                throw new RuntimeException(e);
            }

            try {
                if (message != null) {
                    long time = System.currentTimeMillis();

                    log.info("[JMSLoopBack] received message on destination " + receiveDestination + operationInfo);
                    if(log.isDebugEnabled()) {
                        log.debug("Message is: " + message.getText());
                    }

                    Iterator it = getHeaderValues.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        final String value = message.getStringProperty(key);
                        if(log.isDebugEnabled()) {
                            log.debug("[JMSLoopBack] reading header property: "
                                + key + " value: "
                                + (value == null ? "null" : value));
                        }
                        
                        if (value == null) {
                            throw new UnknownElementException(
                                    "[JMSLoopBack] String property: "
                                    + key
                                    + " is not available for JMS service: "
                                    + receiveDestination);
                        }
                        headerValues.put(key, value);
                    }

                    context.createVariablesFromHeaderValues(getHeaderValues, headerValues);

                    Message receivedMessage = MessageBuilder.withPayload(message.getText()).build();
                    context.createVariablesFromMessageValues(getMessageValues, receivedMessage);

                    String messagePayload = null;
                    
                    if (xmlResource != null) {
                        BufferedInputStream reader = new BufferedInputStream(xmlResource.getInputStream());
                        StringBuffer contentBuffer = new StringBuffer();
                        
                        byte[] contents = new byte[1024];
                        int bytesRead=0;
                        while( (bytesRead = reader.read(contents)) != -1){
                            contentBuffer.append(new String(contents, 0, bytesRead));
                        }
                        
                        messagePayload = contentBuffer.toString();
                    } else if (xmlData != null) {
                        messagePayload = context.replaceDynamicContentInString(xmlData);
                    } else {
                        throw new MissingExpectedMessageException(
                                "[JMSLoopBack] The <property name=\"xmlRessource\"/> or the <property name=\"xmlData\"/> is missing!"
                                + "\n\rPlease specify either a xmlRessource or a xmlData property!");
                    }

                    if (StringUtils.hasText(messagePayload)) {
                        messagePayload = context.replaceMessageValues(setMessageValues, messagePayload);
                    }
                    

                    TextMessage msg = qsession.createTextMessage(messagePayload);

                    headerValues.clear();
                    headerValues = context.replaceVariablesInMap(setHeaderValues);
                    
                    Iterator iter = headerValues.keySet().iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();
                        final String key = (String) o;
                        final String value = (String) headerValues.get(key);

                        if(log.isDebugEnabled()) {
                            log.debug("[JMSLoopBack] setting header property: " + key + " to value: " + value);
                        }

                        /* header value is set */
                        msg.setStringProperty(key, value);
                    }

                    if (message.getJMSReplyTo() != null) {
                        log.info("[JMSLoopBack] sending message to reply-to-queue: "
                                + ((Queue) message.getJMSReplyTo()).getQueueName());
                        if(log.isDebugEnabled()) {
                            log.debug("Message is: " + msg.getText());
                        }

                        // Delay response of the loopbackdummy
                        Thread.sleep(responseDelayMilisec);

                        QueueSender qsender = qsession.createSender((Queue) message.getJMSReplyTo());
                        qsender.send(msg);
                    }

                    if (sendDestination != null) {
                        log.info("[JMSLoopBack] sending message to "+ sendDestination);
                        if(log.isDebugEnabled()) {
                            log.debug("Message is: " + msg.getText());
                        }

                        // Delay response of the loopbackdummy
                        Thread.sleep(responseDelayMilisec);

                        QueueSender qsender = qsession.createSender((Queue) qsession.createQueue(sendDestination));
                        qsender.send(msg);
                    }

                    time = System.currentTimeMillis() - time;
                    updateThroughPut(time);
                }
            } catch (CitrusRuntimeException e) {
                log.error("[JMSLoopBack] Error in JMSLoopBackDummy ", e);
                continue;
            } catch (JMSException e) {
                log.error("[JMSLoopBack] Error in JMSLoopBackDummy ", e);
                continue;
            } catch (IOException e) {
                log.error("[JMSLoopBack] Error in JMSLoopBackDummy ", e);
                continue;
            } catch (InterruptedException e) {
                log.error("[JMSLoopBack] Error in JMSLoopBackDummy ", e);
                continue;
            } catch (ParseException e) {
                log.error("[JMSLoopBack] Error in JMSLoopBackDummy ", e);
                continue;
            }
        }

    }

    private void reportThroughpput() {
        if (throughput > 0) {
            log.info("[JMSLoopBack] throughput for destination: " + receiveDestination);
            log.info("[JMSLoopBack] messageCount: " + throughput);
            log.info("[JMSLoopBack] avgTime: " + getThroughputTime() / throughput + " ms");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Startable#start()
     */
    public void start() {
        for (int i = 1; i <= countInstances; i++) {
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Stoppable#stop()
     */
    public void stop() {
        String operationInfo = "";
        if (messageSelector != null) {
            operationInfo = "(operation=" + messageSelector + ")";
        }

        log.info("[JMSLoopBack] Stopping on destination " + receiveDestination+ operationInfo);

        reportThroughpput();

        synchronized (this) {
            running = false;
            notifyAll();
        }
    }

    /**
     * Method updating throughput and average time.
     *
     * @param time
     *            to add to total time.
     */
    private synchronized void updateThroughPut(long time) {
        throughput++;
        throughputTime += time;
    }

    /**
     * @param receiveDestination
     *            the receiveDestination to set
     */
    public void setReceiveDestination(String receiveDestination) {
        this.receiveDestination = receiveDestination;
    }

    /**
     * @param sendDestination
     *            the sendDestination to set
     */
    public void setSendDestination(String sendDestination) {
        this.sendDestination = sendDestination;
    }

    /**
     * @param serverUrl
     *            the serverUrl to set
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param userPassword
     *            the userPassword to set
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * @param responseDelay
     *            the responseDelay to set
     */
    public void setResponseDelayMilisec(long responseDelayMilisec) {
        this.responseDelayMilisec = responseDelayMilisec;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        queueFactory = new ActiveMQConnectionFactory();
        queueFactory.setBrokerURL(serverUrl);
    }

    /**
     * @param messageSelector
     *            the messageSelector to set
     */
    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#isRunning()
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang
     * .String)
     */
    public void setBeanName(String beanName) {
        name = beanName;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param countInstances
     *            the countInstances to set
     */
    public void setCountInstances(int countInstances) {
        this.countInstances = countInstances;
    }

    /**
     * @param getHeaderValues
     *            the getHeaderValues to set
     */
    public void setGetHeaderValues(HashMap getHeaderValues) {
        this.getHeaderValues = getHeaderValues;
    }

    /**
     * @param getMessageValues
     *            the getMessageValues to set
     */
    public void setGetMessageValues(HashMap getMessageValues) {
        this.getMessageValues = getMessageValues;
    }

    /**
     * @param setHeaderValues
     *            the setHeaderValues to set
     */
    public void setSetHeaderValues(HashMap setHeaderValues) {
        this.setHeaderValues = setHeaderValues;
    }

    /**
     * @param setMessageValues
     *            the setMessageValues to set
     */
    public void setSetMessageValues(HashMap setMessageValues) {
        this.setMessageValues = setMessageValues;
    }

    /**
     * @param xmlData
     *            the xmlData to set
     */
    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    /**
     * @param xmlResource
     *            the xmlResource to set
     */
    public void setXmlResource(Resource xmlResource) {
        this.xmlResource = xmlResource;
    }

    /**
     * @return the throughputTime
     */
    public synchronized long getThroughputTime() {
        return throughputTime;
    }
}
