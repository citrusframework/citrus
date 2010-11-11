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

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.context.TestContext;

/**
 * Abstract control message builder is aware of message headers and delegates message payload
 * generation to subclass.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractHeaderAwareControlMessageBuilder<T> implements ControlMessageBuilder<T> {

    /** The control headers expected for this message */
    private Map<String, Object> controlMessageHeaders = new HashMap<String, Object>();
    
    /**
     * Constructs the control message with headers and payload coming from 
     * subclass implementation.
     */
    public Message<T> buildControlMessage(TestContext context) {
        return MessageBuilder.withPayload(buildMessagePayload(context)).copyHeaders(controlMessageHeaders).build();
    }
    
    protected abstract T buildMessagePayload(TestContext context);

    /**
     * Sets the message headers for this control message.
     * @param controlMessageHeaders the controlMessageHeaders to set
     */
    public void setControlMessageHeaders(Map<String, Object> controlMessageHeaders) {
        this.controlMessageHeaders = controlMessageHeaders;
    }
}
