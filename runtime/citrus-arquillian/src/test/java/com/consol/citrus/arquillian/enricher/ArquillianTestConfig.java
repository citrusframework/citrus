/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.enricher;

import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
@Configuration
public class ArquillianTestConfig extends CitrusSpringConfig {

    @Bean(name = "someEndpoint")
    public Endpoint someEndpoint() {
        return new ChannelEndpoint();
    }

    @Bean(name ="jmsEndpoint")
    public JmsEndpoint jmsEndpoint() {
        return new JmsEndpoint();
    }

    @Bean(name ="jmsSyncEndpoint")
    public JmsSyncEndpoint otherEndpoint() {
        return new JmsSyncEndpoint();
    }
}
