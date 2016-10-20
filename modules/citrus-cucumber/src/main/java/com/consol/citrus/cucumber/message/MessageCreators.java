/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.cucumber.message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class MessageCreators {

    /** Available message creator POJO objects */
    private List<Object> messageCreators = new ArrayList<>();

    /**
     * Create message by delegating message creation to known message creators that
     * are able to create message with given name.
     * @param messageName
     * @return
     */
    public Message createMessage(final String messageName) {
        final Message[] message = { null };
        for (final Object messageCreator : messageCreators) {
            ReflectionUtils.doWithMethods(messageCreator.getClass(), new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.getAnnotation(MessageCreator.class).value().equals(messageName)) {
                        try {
                            message[0] = (Message) method.invoke(messageCreator);
                        } catch (InvocationTargetException e) {
                            throw new CitrusRuntimeException("Unsupported message creator method: " + method.getName(), e);
                        }
                    }
                }
            }, new ReflectionUtils.MethodFilter() {
                @Override
                public boolean matches(Method method) {
                    return method.getAnnotationsByType(MessageCreator.class).length > 0;
                }
            });
        }

        if (message[0] == null) {
            throw new CitrusRuntimeException("Unable to find message creator for message: " + messageName);
        }

        return message[0];
    }

    /**
     * Adds new message creator POJO instance from type.
     * @param type
     */
    public void addType(String type) {
        try {
            messageCreators.add(Class.forName(type).newInstance());
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Unable to access message creator type: " + type, e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException("Unable to create  message creator instance of type: " + type, e);
        }
    }
}
