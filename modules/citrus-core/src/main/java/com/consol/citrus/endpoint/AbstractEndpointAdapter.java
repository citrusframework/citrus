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

package com.consol.citrus.endpoint;

import com.consol.citrus.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;

/**
 * Abstract endpoint adapter adds fallback message handler in case no response was provided.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpointAdapter implements EndpointAdapter, BeanNameAware {

    /** Fallback message handler */
    private MessageHandler fallbackMessageHandler = null;

    /** Endpoint adapter name */
    private String name = getClass().getSimpleName();

    /** Logger */
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final Message<?> handleMessage(Message<?> request) {
        Message<?> replyMessage = handleMessageInternal(request);

        if ((replyMessage == null || replyMessage.getPayload() == null)) {
            if (fallbackMessageHandler != null) {
                log.info("Did not receive reply message - "
                        + "delegating to fallback message handler for response generation");

                replyMessage = fallbackMessageHandler.handleMessage(request);
            } else {
                log.info("Did not receive reply message - no response is simulated");
            }
        }

        return replyMessage;
    }

    /**
     * Subclasses must implement this method in order to handle incoming request message. If
     * this method does not return any response message fallback message handler is invoked for processing.
     * @param message
     * @return
     */
    protected abstract Message<?> handleMessageInternal(Message<?> message);

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Gets this endpoint adapter's name - usually injected as Spring bean name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the fallback message handler.
     * @return
     */
    public MessageHandler getFallbackMessageHandler() {
        return fallbackMessageHandler;
    }

    /**
     * Sets the fallback message handler.
     * @param fallbackMessageHandler
     */
    public void setFallbackMessageHandler(MessageHandler fallbackMessageHandler) {
        this.fallbackMessageHandler = fallbackMessageHandler;
    }
}
