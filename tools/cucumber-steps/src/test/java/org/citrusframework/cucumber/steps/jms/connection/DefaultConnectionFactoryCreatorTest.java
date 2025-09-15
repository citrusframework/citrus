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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.assertj.core.api.Assertions;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

public class DefaultConnectionFactoryCreatorTest {

    private final DefaultConnectionFactoryCreator connectionFactoryCreator = new DefaultConnectionFactoryCreator();

    @Test
    public void shouldCreate() throws IOException {
        Map<String, String> connectionSettings = new HashMap<>();
        connectionSettings.put("type", org.apache.activemq.ActiveMQConnectionFactory.class.getName());
        ConnectionFactory connectionFactory = connectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(org.apache.activemq.ActiveMQConnectionFactory.class).isEqualTo(connectionFactory.getClass());
        Assertions.assertThat(org.apache.activemq.ActiveMQConnectionFactory.DEFAULT_BROKER_URL).isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getBrokerURL());

        connectionSettings = new HashMap<>();
        connectionSettings.put("type", ActiveMQConnectionFactory.class.getName());
        connectionFactory = connectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).toURI().toString()).startsWith("tcp://localhost:61616");
        Assertions.assertThat(connectionFactory.getClass()).isEqualTo(ActiveMQConnectionFactory.class);
    }

    @Test
    public void shouldCreateWithConstructorArgs() throws IOException {
        Map<String, String> connectionSettings = new LinkedHashMap<>();
        connectionSettings.put("type", org.apache.activemq.ActiveMQConnectionFactory.class.getName());
        connectionSettings.put("username", "foo");
        connectionSettings.put("password", "secret");
        connectionSettings.put("brokerUrl", "typ://localhost:61617");
        ConnectionFactory connectionFactory = connectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(org.apache.activemq.ActiveMQConnectionFactory.class).isEqualTo(connectionFactory.getClass());
        Assertions.assertThat("typ://localhost:61617").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getBrokerURL());
        Assertions.assertThat("foo").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getUserName());
        Assertions.assertThat("secret").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getPassword());

        connectionSettings = new LinkedHashMap<>();
        connectionSettings.put("type", ActiveMQConnectionFactory.class.getName());
        connectionSettings.put("brokerUrl", "tcp://localhost:61617");
        connectionSettings.put("username", "foo");
        connectionSettings.put("password", "secret");
        connectionFactory = connectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(ActiveMQConnectionFactory.class).isEqualTo(connectionFactory.getClass());
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).toURI().toString()).startsWith("tcp://localhost:61617");
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).getUser()).isEqualTo("foo");
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).getPassword()).isEqualTo("secret");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldHandleUnsupportedTypeInformation() {
        Map<String, String> connectionSettings = new LinkedHashMap<>();
        connectionSettings.put("type", "org.unknown.Type");
        connectionFactoryCreator.create(connectionSettings);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldHandleMissingTypeInformation() {
        connectionFactoryCreator.create(Collections.emptyMap());
    }

    @Test
    public void shouldSupports() {
        Assertions.assertThat(connectionFactoryCreator.supports(ConnectionFactory.class)).isTrue();
        Assertions.assertThat(connectionFactoryCreator.supports(ActiveMQConnectionFactory.class)).isTrue();
        Assertions.assertThat(connectionFactoryCreator.supports(String.class)).isFalse();
    }
}
