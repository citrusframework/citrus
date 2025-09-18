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

package org.citrusframework.cucumber.steps.jms.connection.activemq.artemis;

import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.DefaultConnectionProperties;
import org.citrusframework.cucumber.steps.jms.connection.ConnectionFactoryCreator;

public class ActiveMQArtemisConnectionFactoryCreator implements ConnectionFactoryCreator {

    @Override
    public ConnectionFactory create(Map<String, String> properties) {
        String brokerUrl = properties.getOrDefault("brokerUrl", DefaultConnectionProperties.DEFAULT_BROKER_URL);
        String user = properties.getOrDefault("username", DefaultConnectionProperties.DEFAULT_USER);
        String password = properties.getOrDefault("password", DefaultConnectionProperties.DEFAULT_PASSWORD);

        return new ActiveMQConnectionFactory(brokerUrl, user, password);
    }

    @Override
    public boolean supports(Class<?> type) {
        return "org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory".equals(type.getName());
    }
}
