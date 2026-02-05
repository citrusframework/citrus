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

package org.citrusframework.endpoint.context;

import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageStoreEndpoint extends AbstractEndpoint {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MessageStoreEndpoint.class);

    public MessageStoreEndpoint() {
        this(new MessageStoreEndpointConfiguration());
    }

    public MessageStoreEndpoint(MessageStoreEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        setName("message-store");
    }

    @Override
    public Producer createProducer() {
        return new Producer() {
            @Override
            public void send(Message message, TestContext context) {
                String messageName = Optional.ofNullable(getEndpointConfiguration().getMessageName())
                        .or(() -> Optional.ofNullable(message.getName()))
                        .orElse(MessageStoreEndpoint.this.getName() + ".message");
                logger.info("Sending message '{}' to message store", messageName);
                context.getMessageStore().storeMessage(messageName, message);
            }

            @Override
            public String getName() {
                return MessageStoreEndpoint.this.getProducerName();
            }
        };
    }

    @Override
    public Consumer createConsumer() {
        return new Consumer() {
            @Override
            public Message receive(TestContext context, long timeout) {
                String messageName = Optional.ofNullable(getEndpointConfiguration().getMessageName())
                        .orElse(MessageStoreEndpoint.this.getName() + ".message");
                Message received = context.getMessageStore().getMessage(messageName);
                if (received == null) {
                    throw new MessageTimeoutException(timeout, MessageStoreEndpoint.this.getName());
                }

                logger.info("Received message '{}' from message store", messageName);

                return received;
            }

            @Override
            public Message receive(TestContext context) {
                return receive(context, getEndpointConfiguration().getTimeout());
            }

            @Override
            public String getName() {
                return MessageStoreEndpoint.this.getConsumerName();
            }
        };
    }

    @Override
    public MessageStoreEndpointConfiguration getEndpointConfiguration() {
        return (MessageStoreEndpointConfiguration) super.getEndpointConfiguration();
    }
}
