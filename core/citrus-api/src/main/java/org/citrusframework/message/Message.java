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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.log.LogMessageModifier;

/**
 * @since 2.0
 */
public interface Message extends Serializable {

    /**
     * Prints message content to String representation.
     */
    default String print() {
        return print(getPayload(String.class).trim(), getHeaders(), getHeaderData());
    }

    /**
     * Prints given message content (body, headers, headerData) to String representation.
     */
    default String print(String body, Map<String, Object> headers, List<String> headerData) {
        if (headerData == null || headerData.isEmpty()) {
            return getClass().getSimpleName().toUpperCase() + " [id: " + getId() + "]\n[headers: " + Collections.unmodifiableMap(headers) + "]\n[payload: " + MessagePayloadUtils.prettyPrint(body) + "]";
        } else {
            return getClass().getSimpleName().toUpperCase() + " [id: " + getId() + "]\n[headers: " + Collections.unmodifiableMap(headers) + "]\n[header-data: " + Collections.unmodifiableList(headerData) + "]\n[payload: " + MessagePayloadUtils.prettyPrint(body) + "]";
        }
    }

    /**
     * Prints message content and applies logger modifier provided in given test context.
     */
    default String print(TestContext context) {
        if (context == null) {
            return print();
        }

        String payload = getPayload(String.class).trim();
        if (context.getLogModifier() instanceof LogMessageModifier modifier) {
            return print(modifier.mask(payload), modifier.maskHeaders(this), modifier.maskHeaderData(this));
        }

        return print(context.getLogModifier().mask(payload), getHeaders(), getHeaderData());
    }

    /**
     * Gets the unique message id;
     */
    String getId();

    /**
     * Indicates the type of the message content (e.g. Xml, Json, binary)
     */
    String getType();

    /**
     * Sets the message type indicating the content type.
     */
    Message setType(String type);

    /**
     * Gets the message name for internal use;
     */
    String getName();

    /**
     * Sets the message name for internal use.
     */
    Message setName(String name);

    /**
     * Gets the message header value by its header name.
     */
    Object getHeader(String headerName);

    /**
     * Sets new header entry in message header list.
     */
    Message setHeader(String headerName, Object headerValue);

    /**
     * Removes the message header if it not a reserved message header such as unique message id.
     */
    void removeHeader(String headerName);

    /**
     * Adds new header data.
     */
    Message addHeaderData(String headerData);

    /**
     * Gets the list of header data in this message.
     */
    List<String> getHeaderData();

    /**
     * Gets message headers.
     */
    Map<String, Object> getHeaders();

    /**
     * Gets message payload with required type conversion.
     */
    <T> T getPayload(Class<T> type);

    /**
     * Gets the message payload.
     */
    Object getPayload();

    /**
     * Sets the message payload.
     */
    Message setPayload(Object payload);

}
