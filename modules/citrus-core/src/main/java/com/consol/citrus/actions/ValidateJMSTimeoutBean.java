package com.consol.citrus.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;

/**
 * Bean to ecpect a JMS timeout on a queue
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class ValidateJMSTimeoutBean extends AbstractTestAction {
    /** Time to wait until timeout */
    private long timeout = 1000L;

    private MessageReceiver messageReceiver;

    /**
     * Select messages to receive
     */
    private String messageSelector;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ValidateJMSTimeoutBean.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            Message receivedMessage;
            
            if (StringUtils.hasText(messageSelector)) {
                receivedMessage = messageReceiver.receiveSelected(messageSelector, timeout);
            } else {
                receivedMessage = messageReceiver.receive(timeout); 
            }

            if(receivedMessage != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Received message: " + receivedMessage.getPayload());
                }
                
                throw new CitrusRuntimeException("JMS timeout validation failed, because test suite received message on destination");
            }
        } catch (ActionTimeoutException e) {
            log.info("Received timeout as expected. JMS timeout validation OK!");
        }
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
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}
