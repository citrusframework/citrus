/*
 * Copyright the original author or authors.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.CachingInputStream;
import org.citrusframework.util.MessageUtils;
import org.citrusframework.util.TypeConversionUtils;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;

/**
 * Default message implementation holds message payload and message headers. Also provides access methods for special
 * header elements such as unique message id and creation timestamp.
 *
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
     * Constructs a new DefaultMessage based on the provided Message object, with an option to force Citrus header update.
     * If forceCitrusHeaderUpdate is true, it overwrites existing values for ID and TIMESTAMP headers with new values.
     * If forceCitrusHeaderUpdate is false, it updates the ID and TIMESTAMP headers only if they are absent in the provided headers.
     *
     * @param message the Message object to copy
     * @param forceCitrusHeaderUpdate flag indicating whether to force Citrus header update
     */
    public DefaultMessage(Message message, boolean forceCitrusHeaderUpdate) {
        this(message.getPayload(), message.getHeaders(), forceCitrusHeaderUpdate);

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
        this(payload, headers, false);
    }

    /**
     * Constructs a new DefaultMessage with the given payload, headers, and an option to force Citrus header update.
     * If forceCitrusHeaderUpdate is true, it updates the ID and TIMESTAMP headers with new values.
     * If forceCitrusHeaderUpdate is false, it updates the ID and TIMESTAMP headers only if they are absent in the provided headers.
     *
     * @param payload the message payload
     * @param headers the message headers
     * @param forceCitrusHeaderUpdate flag indicating whether to force Citrus header update
     */
    private DefaultMessage(Object payload, Map<String, Object> headers, boolean forceCitrusHeaderUpdate) {
        setPayload(payload);
        this.headers.putAll(headers);

        if (forceCitrusHeaderUpdate) {
            this.headers.put(MessageHeaders.ID, randomUUID().toString());
            this.headers.put(MessageHeaders.TIMESTAMP, currentTimeMillis());
        } else {
            this.headers.putIfAbsent(MessageHeaders.ID, randomUUID().toString());
            this.headers.putIfAbsent(MessageHeaders.TIMESTAMP, currentTimeMillis());
        }
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
        if (payload instanceof CachingInputStream cachingInputStream) {
            return cachingInputStream.get();
        }

        return payload;
    }

    @Override
    public DefaultMessage setPayload(Object payload) {
        if (payload instanceof InputStream inputStream && CitrusSettings.isCacheInputStream()) {
            this.payload = new CachingInputStream(inputStream);
        } else {
            this.payload = payload;
        }

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
            } else if (MessageUtils.hasYamlPayload(this)) {
                type = MessageType.YAML.name();
            } else if (getPayload() instanceof String) {
                type = MessageType.PLAINTEXT.name();
            } else if (!CitrusSettings.DEFAULT_MESSAGE_TYPE.equals(MessageType.XML.name())
                    && !CitrusSettings.DEFAULT_MESSAGE_TYPE.equals(MessageType.JSON.name())
                    && !CitrusSettings.DEFAULT_MESSAGE_TYPE.equals(MessageType.YAML.name())) {
                type = CitrusSettings.DEFAULT_MESSAGE_TYPE;
            } else {
                type = MessageType.UNSPECIFIED.name();
            }
        }

        return type;
    }
}
