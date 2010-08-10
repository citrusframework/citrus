/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Action expecting a timeout on a message destination, this means that no message 
 * should arrive on the destination.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class ReceiveTimeoutAction extends AbstractTestAction {
    /** Time to wait until timeout */
    private long timeout = 1000L;

    /** MessageReceiver instance */
    private MessageReceiver messageReceiver;

    /** Message selector string */
    private String messageSelector;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReceiveTimeoutAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            Message<?> receivedMessage;
            
            if (StringUtils.hasText(messageSelector)) {
                receivedMessage = messageReceiver.receiveSelected(messageSelector, timeout);
            } else {
                receivedMessage = messageReceiver.receive(timeout); 
            }

            if(receivedMessage != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Received message: " + receivedMessage.getPayload());
                }
                
                throw new CitrusRuntimeException("Message timeout validation failed! Received message while waiting for timeout on destination");
            }
        } catch (ActionTimeoutException e) {
            log.info("No messages received on destination. Message timeout validation OK!");
        }
    }

    /**
     * Setter for receive timeout.
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Set message selector string.
     * @param messageSelector
     */
    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }
    
    /**
     * Set the message receiver instance.
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}
