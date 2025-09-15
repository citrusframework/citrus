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

package org.citrusframework.cucumber.steps.jms.connection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ObjectHelper;

/**
 * Default connection factory creator implementation. Searches for proper constructor on given type and instantiates
 * the connection factory. The constructor arguments must be provided as properties in correct order.
 */
public class DefaultConnectionFactoryCreator implements ConnectionFactoryCreator {

    @Override
    public ConnectionFactory create(Map<String, String> properties) {
        String className = properties.remove("type");
        ObjectHelper.assertNotNull(className, "Missing connection factory type information");

        try {
            Class<?> type = Class.forName(className);

            if (!(ConnectionFactory.class.isAssignableFrom(type))) {
                throw new IllegalStateException(String.format("Unsupported type information %s - must be of type %s", type.getName(), ConnectionFactory.class.getName()));
            }

            Collection<String> constructorArgs = properties.values();
            Constructor<? extends ConnectionFactory> constructor = Arrays.stream(type.getConstructors())
                    .filter(c -> c.getParameterCount() == constructorArgs.size()
                            && Arrays.stream(c.getParameterTypes()).allMatch(paramType -> paramType.equals(String.class)))
                    .map(c -> (Constructor<? extends ConnectionFactory>) c)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find matching constructor on connection factory"));

            return constructor.newInstance(constructorArgs.toArray(new Object[0]));
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return ConnectionFactory.class.isAssignableFrom(type);
    }
}
