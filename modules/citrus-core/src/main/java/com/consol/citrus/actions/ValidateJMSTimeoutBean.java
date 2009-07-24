package com.consol.citrus.actions;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.jms.core.JmsTemplate;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.JmsTimeoutException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.service.JmsService;
import com.consol.citrus.variable.VariableUtils;

/**
 * Bean to ecpect a JMS timeout on a queue
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class ValidateJMSTimeoutBean extends AbstractTestAction {
    /** Queue destination */
    private String destination;

    /** Time to wait until timeout */
    private long timeout = 0;

    /** JmsTemplate */
    private JmsTemplate jmsTemplate;

    /**
     * Select messages to receive
     */
    private String messageSelector;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ValidateJMSTimeoutBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        try {
            if (timeout != 0) {
                jmsTemplate.setReceiveTimeout(timeout);
            }

            JmsService service = new JmsService();
            service.setJmsTemplate(jmsTemplate);
            
            /*
             * if custom destination is present,
             * set service destination before receiving message
             */
            if (destination != null) {
                String newDestination = null;

                if (VariableUtils.isVariableName(destination)) {
                    newDestination = context.getVariable(destination);
                } else if(context.getFunctionRegistry().isFunction(destination)) {
                    newDestination = FunctionUtils.resolveFunction(destination, context);
                } else {
                    newDestination = destination;
                }

                if (newDestination != null) {
                    if(log.isDebugEnabled()) {
                        log.debug("Setting service destination to custom value " + newDestination);
                    }
                    jmsTemplate.setDefaultDestinationName(newDestination);
                } else if(log.isDebugEnabled()) {
                    log.debug("Setting service destination to custom value failed. Maybe variable is not set properly: " + destination);
                }
            }

            if (messageSelector != null && messageSelector.length() > 0) {
                service.setMessageSelector(context.replaceDynamicContentInString(messageSelector));
            }

            Message receivedMessage = service.receiveMessage();

            if(log.isDebugEnabled()) {
                log.debug("Received message: " + receivedMessage.getPayload());
            }
            throw new TestSuiteException("JMS timeout validation failed, because test suite received message on destination " +  service.getServiceDestination());
        } catch (JmsTimeoutException e) {
            log.info("Received timeout as expected. JMS timeout validation OK!");
        } catch (ParseException e) {
            throw new TestSuiteException(e);
        }
    }

    /**
     * Setter for destination
     * @param destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Setter for timeout
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
}
