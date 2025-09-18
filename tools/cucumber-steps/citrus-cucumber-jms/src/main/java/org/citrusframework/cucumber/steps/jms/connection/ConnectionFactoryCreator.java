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

import java.util.Map;
import java.util.ServiceLoader;

import jakarta.jms.ConnectionFactory;
import org.citrusframework.util.ObjectHelper;

public interface ConnectionFactoryCreator {

    ServiceLoader<ConnectionFactoryCreator> SERVICE_LOADER = ServiceLoader.load(ConnectionFactoryCreator.class);

    /**
     * Create connection factory from given properties.
     */
    ConnectionFactory create(Map<String, String> properties);

    /**
     * Creates class from given type information and checks
     */
    boolean supports(Class<?> type);

    /**
     * Static lookup method makes use of service loader to find a proper connection factory creator implementation for the given type.
     */
    static ConnectionFactoryCreator lookup(String type) throws ClassNotFoundException {
        ObjectHelper.assertNotNull(type, "Missing connection factory type information");
        Class<?> connectionFactoryType = Class.forName(type);

        for (ConnectionFactoryCreator connectionFactoryCreator : SERVICE_LOADER) {
            if (connectionFactoryCreator.supports(connectionFactoryType)) {
                return connectionFactoryCreator;
            }
        }

        return new DefaultConnectionFactoryCreator();
    }
}
