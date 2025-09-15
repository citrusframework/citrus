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

package org.citrusframework.cucumber.steps.standard.message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.util.ReflectionHelper;

public class MessageCreators {

    /** Available message creators */
    private final Map<String, MessageCreator> messageCreators = new HashMap<>();
    /** Available message creator POJO objects */
    private final List<Object> pojoCreators = new ArrayList<>();

    /**
     * Create message by delegating message creation to known message creators that
     * are able to create message with given name.
     * @param messageName
     * @return
     */
    public Message createMessage(final String messageName) {
        if (messageCreators.containsKey(messageName)) {
            return messageCreators.get(messageName).create();
        }

        final Message[] message = { null };
        for (final Object messageCreator : pojoCreators) {
            ReflectionHelper.doWithMethods(messageCreator.getClass(), method -> {
                if (method.getAnnotationsByType(CreatesMessage.class).length == 0) {
                    return;
                }

                if (method.getAnnotation(CreatesMessage.class).value().equals(messageName)) {
                    try {
                        message[0] = (Message) method.invoke(messageCreator);
                    } catch (InvocationTargetException e) {
                        throw new CitrusRuntimeException("Unsupported message creator method: " + method.getName(), e);
                    }
                }
            });
        }

        if (message[0] == null) {
            throw new CitrusRuntimeException("Unable to find message creator for message: " + messageName);
        }

        return message[0];
    }

    /**
     * Adds new message creator with given name.
     * @param name
     * @param creator
     */
    public void add(String name, MessageCreator creator) {
        this.messageCreators.put(name, creator);
    }

    /**
     * Adds new message creator POJO instance from type.
     * @param type
     */
    public void addType(String type) {
        try {
            Object messageCreator = Class.forName(type).newInstance();

            if (messageCreator instanceof MessageCreator) {
                messageCreators.put(messageCreator.getClass().getSimpleName(), (MessageCreator) messageCreator);
            }

            pojoCreators.add(messageCreator);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Unable to access message creator type: " + type, e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException("Unable to create  message creator instance of type: " + type, e);
        }
    }
}
