/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.endpoint;

import com.consol.citrus.report.MessageListeners;

/**
 * Abstract endpoint configuration provides basic properties such as message listeners.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpointConfiguration implements EndpointConfiguration {
    /** The endpoint name usually the Spring bean name */
    private String endpointName;

    /** Send/receive timeout setting */
    private long timeout = 5000L;

    /** Message listeners */
    private MessageListeners messageListener;

    /**
     * Gets the message listeners.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }

    /**
     * Sets the message listeners.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Gets the timeout for sending and receiving messages.
     * @return
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for sending and receiving messages..
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the endpoint name.
     * @return
     */
    public String getEndpointName() {
        return endpointName;
    }

    /**
     * Sets the endpoint name.
     * @param endpointName
     */
    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
}
