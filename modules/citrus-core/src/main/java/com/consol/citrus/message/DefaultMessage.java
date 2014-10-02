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

package com.consol.citrus.message;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class DefaultMessage implements Message {

    /** Message payload object */
    private Object payload;

    /** Message headers */
    private final Map<String, Object> headers;

    /**
     * Default constructor usiong just message payload.
     * @param payload
     */
    public DefaultMessage(Object payload) {
        this(payload, new LinkedHashMap<String, Object>());
    }

    /**
     * Default constructor using payload and headers.
     * @param payload
     * @param headers
     */
    public DefaultMessage(Object payload, Map<String, Object> headers) {
        this.payload = payload;
        this.headers = headers;

        this.headers.put(MessageHeaders.ID, UUID.randomUUID());
        this.headers.put(MessageHeaders.TIMESTAMP, System.currentTimeMillis());
    }

    /**
     * Sets new header entry in message header list.
     * @param headerName
     * @param headerValue
     * @return
     */
    public DefaultMessage setHeader(String headerName, Object headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Message[payload: %s][headers: %s]", getPayload(), getHeaders());
    }

    @Override
    public Map<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
