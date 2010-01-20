/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
 * Action expecting a timeout on a message destination
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class ReceiveTimeoutAction extends AbstractTestAction {
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
