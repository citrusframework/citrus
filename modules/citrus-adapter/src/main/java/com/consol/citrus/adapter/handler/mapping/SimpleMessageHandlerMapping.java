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

package com.consol.citrus.adapter.handler.mapping;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Message handler mapping simply holds map of mapping names and message handlers. Searches for available mapping name
 * in mapping keys and returns respective message handler implementation.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 * @deprecated since Citrus 1.4, in favor of {@link com.consol.citrus.endpoint.adapter.mapping.SimpleMappingStrategy}
 */
@Deprecated
public class SimpleMessageHandlerMapping implements MessageHandlerMapping {

    /** Simple map holds mapping names and message handlers */
    private Map<String, MessageHandler> handlerMappings = new HashMap<String, MessageHandler>();

    /**
     * Finds message handler by mapping name.
     *
     * @param mappingName
     * @return
     */
    @Override
    public MessageHandler getMessageHandler(String mappingName) {
        if (handlerMappings.containsKey(mappingName)) {
            return handlerMappings.get(mappingName);
        } else {
            throw new CitrusRuntimeException("Unable to find matching message handler with mapping name '" +
                    mappingName + "' in list of available message handlers");

        }
    }

    /**
     * Sets the handler mappings.
     * @param handlerMappings
     */
    public void setHandlerMappings(Map<String, MessageHandler> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }
}
