/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.endpoint.adapter;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint adapter always returns a static response message.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class StaticResponseEndpointAdapter extends StaticEndpointAdapter {

    /** Response message payload */
    private String messagePayload = "";

    /** Response message header */
    private Map<String, Object> messageHeader = new HashMap<String, Object>();

    @Override
    public Message handleMessageInternal(Message message) {
        return new DefaultMessage(messagePayload, messageHeader);
    }

    /**
     * Gets the message payload.
     * @return
     */
    public String getMessagePayload() {
        return messagePayload;
    }

    /**
     * Set the response message payload.
     * @param messagePayload the messagePayload to set
     */
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * Gets the message header.
     * @return
     */
    public Map<String, Object> getMessageHeader() {
        return messageHeader;
    }

    /**
     * Set the response message header.
     * @param messageHeader the messageHeader to set
     */
    public void setMessageHeader(Map<String, Object> messageHeader) {
        this.messageHeader = messageHeader;
    }
}
