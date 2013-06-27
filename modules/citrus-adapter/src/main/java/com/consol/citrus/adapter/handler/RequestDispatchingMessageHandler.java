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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.adapter.handler.mapping.MappingKeyExtractor;
import com.consol.citrus.adapter.handler.mapping.MessageHandlerMapping;
import com.consol.citrus.message.MessageHandler;
import org.springframework.integration.Message;

/**
 * Base message handler implementation that dispatches incoming messages according to some extracted message value and
 * a message handler mapping. Once handler mapping identified proper message handler implementation incoming request is forwarded
 * to this message handler for processing.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class RequestDispatchingMessageHandler implements MessageHandler {

    /** Extracts message value predicate for dispatching */
    private MappingKeyExtractor mappingKeyExtractor;

    /** Message handler mapping */
    private MessageHandlerMapping messageHandlerMapping;

    @Override
    public Message<?> handleMessage(Message<?> message) {
        return dispatchMessage(message, mappingKeyExtractor.extractMappingKey(message));
    }

    /**
     * Subclasses must implement this method in order to dispatch incoming request according to mapping name that was
     * extracted before from message content.
     * @param request
     * @param mappingName
     * @return
     */
    public Message<?> dispatchMessage(Message<?> request, String mappingName) {
        return messageHandlerMapping.getMessageHandler(mappingName).handleMessage(request);
    }

    /**
     * Gets the message handler mapping.
     * @return
     */
    public MessageHandlerMapping getMessageHandlerMapping() {
        return messageHandlerMapping;
    }

    /**
     * Gets the mapping name extractor.
     * @return
     */
    public MappingKeyExtractor getMappingKeyExtractor() {
        return mappingKeyExtractor;
    }

    /**
     * Sets the mapping name extractor implementation.
     * @param mappingKeyExtractor
     */
    public void setMappingKeyExtractor(MappingKeyExtractor mappingKeyExtractor) {
        this.mappingKeyExtractor = mappingKeyExtractor;
    }

    /**
     * Sets the handler mapping implementation.
     * @param messageHandlerMapping
     */
    public void setMessageHandlerMapping(MessageHandlerMapping messageHandlerMapping) {
        this.messageHandlerMapping = messageHandlerMapping;
    }
}
