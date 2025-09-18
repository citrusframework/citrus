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

package org.citrusframework.cucumber.steps.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.core.message.MessageCreator;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointConfiguration;
import org.citrusframework.endpoint.direct.DirectEndpoints;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StandardEndpointConfiguration {

    @Bean
    public MessageCreator fooMessage() {
        return () -> new DefaultMessage("Hello from Foo!");
    }

    @Bean
    public DirectEndpoint fooEndpoint() {
        return DirectEndpoints
                          .direct()
                          .asynchronous()
                          .queue("foo")
                          .build();
    }

    @Bean
    public EchoEndpoint echoEndpoint() {
        return new EchoEndpoint();
    }

    public static class EchoEndpoint extends AbstractEndpoint {

        private Message latest;

        public EchoEndpoint() {
            super(new DirectEndpointConfiguration());
        }

        @Override
        public Producer createProducer() {
            return new Producer() {
                @Override
                public void send(Message message, TestContext context) {
                    latest = message;
                }

                @Override
                public String getName() {
                    return "echo";
                }
            };
        }

        @Override
        public Consumer createConsumer() {
            return new Consumer() {
                @Override
                public Message receive(TestContext context) {
                    if (latest != null) {
                        return new DefaultMessage(latest).setPayload("You just said: " + latest.getPayload(String.class));
                    } else  {
                        return null;
                    }
                }

                @Override
                public Message receive(TestContext context, long timeout) {
                    return receive(context);
                }

                @Override
                public String getName() {
                    return "echo";
                }
            };
        }
    }

}
