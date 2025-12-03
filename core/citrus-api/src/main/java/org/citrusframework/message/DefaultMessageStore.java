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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default message store implementation saves messages with its unique id as a key.
 * The message store also holds a mapping of registered message names resolving to message ids.
 * Allows users to get messages by its name or id.
 * @since 2.6.2
 */
public class DefaultMessageStore extends ConcurrentHashMap<String, Message> implements MessageStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageStore.class);

    private final Map<String, String> registeredNames = new ConcurrentHashMap<>();

    @Override
    public Message getMessage(String nameOrId) {
        if (registeredNames.containsKey(nameOrId)) {
            return super.get(registeredNames.get(nameOrId));
        }

        return super.get(nameOrId);
    }

    @Override
    public void storeMessage(String name, Message message) {
        if (registeredNames.containsKey(name)) {
            logger.warn("Message with name '{}' already exists in message store - will overwrite the name mapping", name);
        }

        registeredNames.put(name, message.getId());
        super.put(message.getId(), message);
    }

    @Override
    public String constructMessageName(TestAction action, Endpoint endpoint) {
        return action.getName() + "(" + endpoint.getName() + ")";
    }
}
