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
public class DefaultMessageContainer<T extends Message> implements MessageContainer<T> {

    /** Serial */
    private static final long serialVersionUID = 6544787865701660114L;

    /** Nested message object */
    private final T message;

    /**
     * Default constructor using nested message.
     * @param message
     */
    public DefaultMessageContainer(T message) {
        this.message = message;
    }

    @Override
    public String getId() {
        return message.getId();
    }

    @Override
    public Object getHeader(String headerName) {
        return message.getHeader(headerName);
    }

    @Override
    public DefaultMessage setHeader(String headerName, Object headerValue) {
        return message.setHeader(headerName, headerValue);
    }

    @Override
    public void removeHeader(String headerName) {
        message.removeHeader(headerName);
    }

    @Override
    public Map<String, Object> copyHeaders() {
        return message.copyHeaders();
    }

    @Override
    public <P> P getPayload(Class<P> type) {
        return message.getPayload(type);
    }

    @Override
    public Object getPayload() {
        return message;
    }

    @Override
    public void setPayload(Object payload) {
        message.setPayload(payload);
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
