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

package com.consol.citrus.validation;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;

/**
 * Very basic context holding validation specific objects like expected message payload 
 * and expected message headers.
 * 
 * Context is not aware of ignored message elements or XML specific XPath validation.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class ValidationContext {
    private Message<?> expectedMessage;
    
    private MessageHeaders expectedMessageHeaders;
    
    /**
     * @param expectedMessage the expectedMessage to set
     */
    public void setExpectedMessage(Message<?> expectedMessage) {
        this.expectedMessage = expectedMessage;
    }

    /**
     * @return the expectedMessage
     */
    public Message<?> getExpectedMessage() {
        return expectedMessage;
    }

    /**
     * @return the expectedMessageHeaders
     */
    public MessageHeaders getExpectedMessageHeaders() {
        if(expectedMessageHeaders != null) {
            return expectedMessageHeaders;
        } else if(expectedMessage != null){
            return expectedMessage.getHeaders();
        } else {
            return null;
        }
    }

    /**
     * @param expectedMessageHeaders the expectedMessageHeaders to set
     */
    public void setExpectedMessageHeaders(MessageHeaders expectedMessageHeaders) {
        this.expectedMessageHeaders = expectedMessageHeaders;
    }

}
