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

package com.consol.citrus.validation;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;

/**
 * Validation context used during message validation. Contexts holds validation specific 
 * information.
 * 
 * Context is not aware of ignored message elements or XML specific XPath validation.
 * 
 * @author Christoph Deppisch
 */
public class ValidationContext {
    /** Control message */
    private Message<?> expectedMessage;
    
    /** Control message headers */
    private MessageHeaders expectedMessageHeaders;
    
    /**
     * @param expectedMessage the expectedMessage to set
     */
    public void setExpectedMessage(Message<?> expectedMessage) {
        this.expectedMessage = expectedMessage;
    }

    /**
     * Get the control message.
     * @return the expectedMessage
     */
    public Message<?> getExpectedMessage() {
        return expectedMessage;
    }

    /**
     * Get control message headers.
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
     * Set control message headers.
     * @param expectedMessageHeaders the expectedMessageHeaders to set
     */
    public void setExpectedMessageHeaders(MessageHeaders expectedMessageHeaders) {
        this.expectedMessageHeaders = expectedMessageHeaders;
    }
}
