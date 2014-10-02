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

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface Message {

    /**
     * Gets the unique message id;
     * @return
     */
    public String getId();

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
    public DefaultMessage setHeader(String headerName, Object headerValue);

    /**
     * Removes the message header if it not a reserved message header such as unique message id.
     * @param headerName
     * @return
     */
    void removeHeader(String headerName);

    /**
     * Gets exact copy of actual message headers.
     * @return
     */
    public Map<String, Object> copyHeaders();

    /**
     * Gets message payload with required type conversion.
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getPayload(Class<T> type);

    /**
     * Gets the message payload.
     * @return
     */
    Object getPayload();

    /**
     * Sets the message payload.
     * @param payload
     */
    void setPayload(Object payload);

}
