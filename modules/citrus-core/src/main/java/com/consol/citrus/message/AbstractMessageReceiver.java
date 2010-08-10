/*
 * Copyright 2006-2010 the original author or authors.
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

import org.springframework.integration.core.Message;

/**
 * Abstract base class for message receiver implementations.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageReceiver implements MessageReceiver {

    /** Receive timeout in ms */
    private long receiveTimeout = 5000L;
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public abstract Message<?> receive(long timeout);

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public abstract Message<?> receiveSelected(String selector, long timeout);

    /**
     * Setter for receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
}
