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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.assertj.core.api.Assertions;
import org.citrusframework.cucumber.steps.jms.connection.activemq.artemis.ActiveMQArtemisConnectionFactoryCreator;
import org.testng.annotations.Test;

public class ActiveMQConnectionFactoryCreatorTest {

    private final ActiveMQConnectionFactoryCreator connectionFactoryCreator = new ActiveMQConnectionFactoryCreator();
    private final ActiveMQArtemisConnectionFactoryCreator artemisConnectionFactoryCreator = new ActiveMQArtemisConnectionFactoryCreator();

    @Test
    public void shouldCreate() throws IOException {
        ConnectionFactory connectionFactory = connectionFactoryCreator.create(Collections.emptyMap());

        Assertions.assertThat(connectionFactory.getClass()).isEqualTo(org.apache.activemq.ActiveMQConnectionFactory.class);
        Assertions.assertThat(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getBrokerURL()).isEqualTo(org.apache.activemq.ActiveMQConnectionFactory.DEFAULT_BROKER_URL);

        connectionFactory = artemisConnectionFactoryCreator.create(Collections.emptyMap());

        Assertions.assertThat(connectionFactory.getClass()).isEqualTo(ActiveMQConnectionFactory.class);
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).toURI().toString()).startsWith("tcp://localhost:61616");
    }

    @Test
    public void shouldCreateWithProperties() {
        Map<String, String> connectionSettings = new LinkedHashMap<>();
        connectionSettings.put("brokerUrl", "tcp://localhost:61617");
        connectionSettings.put("username", "foo");
        connectionSettings.put("password", "secret");
        ConnectionFactory connectionFactory = connectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(org.apache.activemq.ActiveMQConnectionFactory.class).isEqualTo(connectionFactory.getClass());
        Assertions.assertThat("tcp://localhost:61617").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getBrokerURL());
        Assertions.assertThat("foo").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getUserName());
        Assertions.assertThat("secret").isEqualTo(((org.apache.activemq.ActiveMQConnectionFactory)connectionFactory).getPassword());

        connectionSettings = new LinkedHashMap<>();
        connectionSettings.put("brokerUrl", "tcp://localhost:61617");
        connectionSettings.put("username", "foo");
        connectionSettings.put("password", "secret");
        connectionFactory = artemisConnectionFactoryCreator.create(connectionSettings);

        Assertions.assertThat(connectionFactory.getClass()).isEqualTo(ActiveMQConnectionFactory.class);
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).getUser()).isEqualTo("foo");
        Assertions.assertThat(((ActiveMQConnectionFactory)connectionFactory).getPassword()).isEqualTo("secret");
    }

    @Test
    public void shouldSupport() {
        Assertions.assertThat(connectionFactoryCreator.supports(org.apache.activemq.ActiveMQConnectionFactory.class)).isTrue();
        Assertions.assertThat(connectionFactoryCreator.supports(ConnectionFactory.class)).isFalse();
        Assertions.assertThat(artemisConnectionFactoryCreator.supports(ActiveMQConnectionFactory.class)).isTrue();
        Assertions.assertThat(artemisConnectionFactoryCreator.supports(ConnectionFactory.class)).isFalse();
    }
}
