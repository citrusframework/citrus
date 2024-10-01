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

package org.citrusframework.kafka.endpoint;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractMessageConsumer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import static org.citrusframework.kafka.endpoint.KafkaMessageConsumerUtils.parseConsumerRecordsToMessage;
import static org.citrusframework.kafka.endpoint.KafkaMessageConsumerUtils.resolveTopic;
import static org.slf4j.LoggerFactory.getLogger;

class KafkaMessageSingleConsumer extends AbstractMessageConsumer {

    private static final Logger logger = getLogger(KafkaMessageSingleConsumer.class);

    private final org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

    public static KafkaMessageSingleConsumerBuilder builder() {
        return new KafkaMessageSingleConsumerBuilder();
    }

    private KafkaMessageSingleConsumer(
            KafkaEndpointConfiguration endpointConfiguration,
            org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer
    ) {
        super(KafkaMessageSingleConsumer.class.getSimpleName(), endpointConfiguration);
        this.consumer = consumer;
    }

    @Override
    public Message receive(TestContext testContext, long timeout) {
        String topic = resolveTopic(getEndpointConfiguration(), testContext);

        logger.debug("Receiving Kafka message on topic: '{}'", topic);

        if (consumer.subscription() == null || consumer.subscription().isEmpty()) {
            consumer.subscribe(Arrays.stream(topic.split(",")).toList());
        }

        ConsumerRecords<Object, Object> consumerRecords = consumer.poll(Duration.ofMillis(timeout));
        if (consumerRecords.isEmpty()) {
            throw new MessageTimeoutException(timeout, topic);
        }

        var received = parseConsumerRecordsToMessage(
                StreamSupport.stream(consumerRecords.spliterator(), false).toList(),
                getEndpointConfiguration(),
                testContext);

        consumer.commitSync(Duration.ofMillis(getEndpointConfiguration().getTimeout()));

        logger.info("Received Kafka message on topic: '{}", topic);
        return received;
    }

    @Override
    protected KafkaEndpointConfiguration getEndpointConfiguration() {
        return (KafkaEndpointConfiguration) super.getEndpointConfiguration();
    }

    public static class KafkaMessageSingleConsumerBuilder {

        private KafkaEndpointConfiguration endpointConfiguration;
        private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

        public KafkaMessageSingleConsumerBuilder endpointConfiguration(KafkaEndpointConfiguration endpointConfiguration) {
            this.endpointConfiguration = endpointConfiguration;
            return this;
        }

        public KafkaMessageSingleConsumerBuilder consumer(KafkaConsumer<Object, Object> consumer) {
            this.consumer = consumer;
            return this;
        }

        public KafkaMessageSingleConsumer build() {
            return new KafkaMessageSingleConsumer(endpointConfiguration, consumer);
        }
    }
}
