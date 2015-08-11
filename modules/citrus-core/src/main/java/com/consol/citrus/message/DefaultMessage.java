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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
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
        this(message.getPayload(), message.copyHeaders());

        this.headers.put(MessageHeaders.ID, message.getId());
        this.headers.put(MessageHeaders.TIMESTAMP, message.getHeader(MessageHeaders.TIMESTAMP));

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

        this.headers.put(MessageHeaders.ID, UUID.randomUUID());
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
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }

        if (type.isAssignableFrom(Source.class)) {
            if (getPayload().getClass().isAssignableFrom(String.class)) {
                return (T) new StringSource(getPayload(String.class));
            } else if (getPayload().getClass().isAssignableFrom(Node.class)) {
                return (T) new DOMSource((Node) getPayload());
            } else if (getPayload().getClass().isAssignableFrom(InputStreamSource.class)) {
                try {
                    return (T) new StreamSource(((InputStreamSource)getPayload()).getInputStream());
                } catch (IOException e) {
                    log.warn("Failed to create stream source from message payload", e);
                }
            }
        }

        try {
            return new SimpleTypeConverter().convertIfNecessary(payload, type);
        } catch (ConversionNotSupportedException e) {
            if (String.class.equals(type)) {
                log.warn(String.format("Using payload object toString representation: %s", e.getMessage()));
                return (T) payload.toString();
            }

            throw e;
        }
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
    public Map<String, Object> copyHeaders() {
        LinkedHashMap copy = new LinkedHashMap(headers.size());
        copy.putAll(headers);
        return copy;
    }
}
