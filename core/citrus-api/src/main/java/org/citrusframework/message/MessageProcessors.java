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

package org.citrusframework.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.Scoped;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * List of global message construction processors that modify message payload and message headers. User just has to add
 * processor implementation as bean to the Spring application context.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageProcessors {

    private List<MessageProcessor> messageProcessors = new ArrayList<>();

    /**
     * Sets the messageProcessors property.
     *
     * @param messageProcessors
     */
    public void setMessageProcessors(List<MessageProcessor> messageProcessors) {
        this.messageProcessors = messageProcessors;
    }

    /**
     * Gets the message processors.
     * @return
     */
    public List<MessageProcessor> getMessageProcessors() {
        return Collections.unmodifiableList(messageProcessors);
    }

    /**
     * Adds a new message processor.
     * @param processor
     */
    public void addMessageProcessor(MessageProcessor processor) {
        if (processor instanceof Scoped && !((Scoped)processor).isGlobalScope()) {
            throw new CitrusRuntimeException("Unable to add non global scoped processor to global message processors - " +
                    "either declare processor as global scope or explicitly add it to test actions instead");
        }
        this.messageProcessors.add(processor);
    }
}
