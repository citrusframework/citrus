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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.TypeConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Default message implementation holds message payload and message headers. Also provides access methods for special
 * header elements such as unique message id and creation timestamp.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class DefaultMessage implements Message {

    /** Serial */
    private static final long serialVersionUID = 1904510540660648952L;

    /** Message payload object */
    private Object payload;

    /** Optional list of header data */
    private List<String> headerData = new ArrayList<String>();

    /** Message headers */
    private final Map<String, Object> headers;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultMessage.class);

    /**
     * Empty constructor initializing with empty message payload.
     */
    public DefaultMessage() {
        this("");
    }

    /**
     * Constructs copy of given message.
     * @param message
     */
    public DefaultMessage(Message message) {
        this(message.getPayload());

        this.headers.putAll(message.getHeaders());

        for (String data : message.getHeaderData()) {
            addHeaderData(data);
        }
    }

    /**
     * Default constructor using just message payload.
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

        this.headers.put(MessageHeaders.ID, UUID.randomUUID().toString());
        this.headers.put(MessageHeaders.TIMESTAMP, System.currentTimeMillis());
    }

    @Override
    public String getId() {
        return headers.get(MessageHeaders.ID).toString();
    }

    /**
     * Gets the message creation timestamp;
     * @return
     */
    public Long getTimestamp() {
        return (Long) headers.get(MessageHeaders.TIMESTAMP);
    }

    @Override
    public String toString() {
        if (CollectionUtils.isEmpty(headerData)) {
            return String.format("%s [payload: %s][headers: %s]", getClass().getSimpleName().toUpperCase(), getPayload(String.class).trim(), headers);
        } else {
            return String.format("%s [payload: %s][headers: %s][header-data: %s]", getClass().getSimpleName().toUpperCase(), getPayload(String.class).trim(), headers, headerData);
        }
    }

    @Override
    public DefaultMessage setHeader(String headerName, Object headerValue) {
        if (headerName.equals(MessageHeaders.ID)) {
            throw new CitrusRuntimeException("Not allowed to set reserved message header: " + MessageHeaders.ID);
        }

        headers.put(headerName, headerValue);
        return this;
    }

    @Override
    public Object getHeader(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public void removeHeader(String headerName) {
        if (headerName.equals(MessageHeaders.ID)) {
            throw new CitrusRuntimeException("Not allowed to remove reserved message header from message: " + MessageHeaders.ID);
        }

        headers.remove(headerName);
    }

    @Override
    public DefaultMessage addHeaderData(String headerData) {
        this.headerData.add(headerData);
        return this;
    }

    @Override
    public List<String> getHeaderData() {
        return headerData;
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        return TypeConversionUtils.convertIfNecessary(getPayload(), type);
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public Map<String, Object> getHeaders() {
        return headers;
    }
}
