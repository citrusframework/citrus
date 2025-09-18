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

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.assertj.core.api.Assertions;
import org.citrusframework.cucumber.steps.jms.connection.activemq.ActiveMQConnectionFactoryCreator;
import org.citrusframework.cucumber.steps.jms.connection.activemq.artemis.ActiveMQArtemisConnectionFactoryCreator;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

public class ConnectionFactoryCreatorTest {

    @Test
    public void shouldLookup() throws ClassNotFoundException {
        ConnectionFactoryCreator creator = ConnectionFactoryCreator.lookup(org.apache.activemq.ActiveMQConnectionFactory.class.getName());
        Assertions.assertThat(creator.getClass()).isEqualTo(ActiveMQConnectionFactoryCreator.class);

        creator = ConnectionFactoryCreator.lookup(ActiveMQConnectionFactory.class.getName());
        Assertions.assertThat(creator.getClass()).isEqualTo(ActiveMQArtemisConnectionFactoryCreator.class);
    }

    @Test
    public void shouldLookupDefault() throws ClassNotFoundException {
        ConnectionFactoryCreator creator = ConnectionFactoryCreator.lookup(DummyConnectionFactory.class.getName());
        Assertions.assertThat(creator.getClass()).isEqualTo(DefaultConnectionFactoryCreator.class);
    }

    @Test(expectedExceptions = ClassNotFoundException.class)
    public void shouldHandleUnsupportedTypeInformation() throws ClassNotFoundException {
        ConnectionFactoryCreator.lookup("org.unknown.Type");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldHandleMissingTypeInformation() throws ClassNotFoundException {
        ConnectionFactoryCreator.lookup(null);
    }

    /**
     * Empty connection factory implementation for testing.
     */
    private static class DummyConnectionFactory implements ConnectionFactory {
        @Override
        public Connection createConnection() throws JMSException {
            return null;
        }

        @Override
        public Connection createConnection(String s, String s1) throws JMSException {
            return null;
        }

        @Override
        public JMSContext createContext() {
            return null;
        }

        @Override
        public JMSContext createContext(String s, String s1) {
            return null;
        }

        @Override
        public JMSContext createContext(String s, String s1, int i) {
            return null;
        }

        @Override
        public JMSContext createContext(int i) {
            return null;
        }
    }
}
