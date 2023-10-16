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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.log.LogMessageModifier;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface Message extends Serializable {

    /**
     * Prints message content to String representation.
     * @return
     */
    default String print() {
        return print(getPayload(String.class).trim(), getHeaders(), getHeaderData());
    }

    /**
     * Prints given message content (body, headers, headerData) to String representation.
     * @param body
     * @param headers
     * @param headerData
     * @return
     */
    default String print(String body, Map<String, Object> headers, List<String> headerData) {
        if (headerData == null || headerData.isEmpty()) {
            return getClass().getSimpleName().toUpperCase() + " [id: " + getId() + ", payload: " + MessagePayloadUtils.prettyPrint(body) + "][headers: " + Collections.unmodifiableMap(headers) + "]";
        } else {
            return getClass().getSimpleName().toUpperCase() + " [id: " + getId() + ", payload: " + MessagePayloadUtils.prettyPrint(body) + "][headers: " + Collections.unmodifiableMap(headers) + "][header-data: " + Collections.unmodifiableList(headerData) + "]";
        }
    }

    /**
     * Prints message content and applies logger modifier provided in given test context.
     * @return
     */
    default String print(TestContext context) {
        if (context == null) {
            return print();
        }

        if (context.getLogModifier() instanceof LogMessageModifier) {
            LogMessageModifier modifier = ((LogMessageModifier) context.getLogModifier());
            return print(modifier.maskBody(this), modifier.maskHeaders(this), modifier.maskHeaderData(this));
        }

        return print(context.getLogModifier().mask(getPayload(String.class).trim()), getHeaders(), getHeaderData());
    }

    /**
     * Gets the unique message id;
     * @return
     */
    String getId();

    /**
     * Indicates the type of the message content (e.g. Xml, Json, binary)
     * @return
     */
    String getType();

    /**
     * Sets the message type indicating the content type.
     * @param type
     */
    Message setType(String type);

    /**
     * Gets the message name for internal use;
     * @return
     */
    String getName();

    /**
     * Sets the message name for internal use.
     * @param name
     */
    Message setName(String name);

    /**
     * Gets the message header value by its header name.
     * @param headerName
     * @return
     */
    Object getHeader(String headerName);

    /**
     * Sets new header entry in message header list.
     * @param headerName
     * @param headerValue
     * @return
     */
    Message setHeader(String headerName, Object headerValue);

    /**
     * Removes the message header if it not a reserved message header such as unique message id.
     * @param headerName
     * @return
     */
    void removeHeader(String headerName);

    /**
     * Adds new header data.
     * @param headerData
     * @return
     */
    Message addHeaderData(String headerData);

    /**
     * Gets the list of header data in this message.
     * @return
     */
    List<String> getHeaderData();

    /**
     * Gets message headers.
     * @return
     */
    Map<String, Object> getHeaders();

    /**
     * Gets message payload with required type conversion.
     * @param type
     * @param <T>
     * @return
     */
    <T> T getPayload(Class<T> type);

    /**
     * Gets the message payload.
     * @return
     */
    Object getPayload();

    /**
     * Sets the message payload.
     * @param payload
     */
    Message setPayload(Object payload);

}
