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

package org.citrusframework.cucumber.steps.jms.connection.activemq;

import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.citrusframework.cucumber.steps.jms.connection.ConnectionFactoryCreator;

public class ActiveMQConnectionFactoryCreator implements ConnectionFactoryCreator {

    @Override
    public ConnectionFactory create(Map<String, String> properties) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

        if (properties.containsKey("brokerUrl")) {
            connectionFactory.setBrokerURL(properties.get("brokerUrl"));
        }

        if (properties.containsKey("username")) {
            connectionFactory.setUserName(properties.get("username"));
        }

        if (properties.containsKey("password")) {
            connectionFactory.setPassword(properties.get("password"));
        }

        return connectionFactory;
    }

    @Override
    public boolean supports(Class<?> type) {
        return "org.apache.activemq.ActiveMQConnectionFactory".equals(type.getName());
    }
}
