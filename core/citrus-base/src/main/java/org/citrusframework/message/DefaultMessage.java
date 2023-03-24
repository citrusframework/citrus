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

package org.citrusframework.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.MessageUtils;
import org.citrusframework.util.TypeConversionUtils;

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
    private final List<String> headerData = new ArrayList<>();

    /** Message headers */
    private final Map<String, Object> headers = new LinkedHashMap<>();

    /** The message name for internal use */
    private String name;

    /** Type of the message indicates the content type - also see {@link MessageType) */
    private String type;

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
        this(message.getPayload(), message.getHeaders());

        this.setName(message.getName());
        this.setType(message.getType());
        this.headerData.addAll(message.getHeaderData());
    }

    /**
     * Default constructor using just message payload.
     * @param payload
     */
    public DefaultMessage(Object payload) {
        this(payload, new LinkedHashMap<>());
    }

    /**
     * Default constructor using payload and headers.
     * @param payload
     * @param headers
     */
    public DefaultMessage(Object payload, Map<String, Object> headers) {
        this.payload = payload;
        this.headers.putAll(headers);

        this.headers.putIfAbsent(MessageHeaders.ID, UUID.randomUUID().toString());
        this.headers.putIfAbsent(MessageHeaders.TIMESTAMP, System.currentTimeMillis());
    }

    @Override
    public String getId() {
        return getHeader(MessageHeaders.ID).toString();
    }

    /**
     * Gets the message creation timestamp;
     * @return
     */
    public Long getTimestamp() {
        return (Long) getHeader(MessageHeaders.TIMESTAMP);
    }

    @Override
    public String toString() {
        return print();
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
    public DefaultMessage setPayload(Object payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public Map<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public DefaultMessage setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Specifies the message type.
     * @param type one of the default message types.
     */
    public DefaultMessage setType(MessageType type) {
        this.type = type.name();
        return this;
    }

    @Override
    public DefaultMessage setType(String type) {
        if (type != null) {
            headers.put(MessageHeaders.MESSAGE_TYPE, type);
        }

        this.type = type;
        return this;
    }

    @Override
    public String getType() {
        if (type == null) {
            if (MessageUtils.hasJsonPayload(this)) {
                type = MessageType.JSON.name();
            } else if (MessageUtils.hasXmlPayload(this)) {
                type = MessageType.XML.name();
            } else if (getPayload() instanceof String) {
                type = MessageType.PLAINTEXT.name();
            } else if (!CitrusSettings.DEFAULT_MESSAGE_TYPE.equals(MessageType.XML.name())
                    && !CitrusSettings.DEFAULT_MESSAGE_TYPE.equals(MessageType.JSON.name())) {
                type = CitrusSettings.DEFAULT_MESSAGE_TYPE;
            } else {
                type = MessageType.UNSPECIFIED.name();
            }
        }

        return type;
    }
}
