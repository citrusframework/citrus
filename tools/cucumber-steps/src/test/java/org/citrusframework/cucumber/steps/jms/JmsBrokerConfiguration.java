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
package org.citrusframework.cucumber.steps.jms;

import java.util.Collections;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class JmsBrokerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQ messageBroker() {
        EmbeddedActiveMQ broker = new EmbeddedActiveMQ();
        broker.setSecurityManager(securityManager());
        return broker;
    }

    @Bean
    public ActiveMQSecurityManager securityManager() {
        SecurityConfiguration securityConfiguration = new SecurityConfiguration(Collections.singletonMap("citrus", "citrus"),
                Collections.singletonMap("citrus", Collections.singletonList("citrus")));
        securityConfiguration.setDefaultUser("citrus");
        return new ActiveMQJAASSecurityManager(InVMLoginModule.class.getName(), securityConfiguration);
    }

	@Bean
	@DependsOn("messageBroker")
	public ConnectionFactory jmsConnectionFactory() {
		return new ActiveMQConnectionFactory("tcp://localhost:61616", "citrus", "citrus");
	}
}
