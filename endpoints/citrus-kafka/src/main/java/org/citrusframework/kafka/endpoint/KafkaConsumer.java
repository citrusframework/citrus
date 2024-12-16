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

import static java.util.UUID.randomUUID;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumer extends AbstractSelectiveMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

    /**
     * Default constructor using endpoint.
     */
    public KafkaConsumer(String name, KafkaEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.consumer = createConsumer();
    }

    public org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> getConsumer() {
        return consumer;
    }

    public void setConsumer(org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Message receive(TestContext testContext, long timeout) {
        logger.debug("Receiving single message");
        return KafkaMessageSingleConsumer.builder()
            .consumer(consumer)
            .endpointConfiguration(getEndpointConfiguration())
            .build()
            .receive(testContext, timeout);
    }

    @Override
    public Message receive(String selector, TestContext testContext, long timeout) {
        logger.debug("Receiving selected message: {}", selector);
        return KafkaMessageFilteringConsumer.builder()
            .consumer(consumer)
            .endpointConfiguration(getEndpointConfiguration())
            .build()
            .receive(selector, testContext, timeout);
    }

    @Override
    protected KafkaEndpointConfiguration getEndpointConfiguration() {
        return (KafkaEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Stop message listener container.
     */
    public void stop() {
        try {
            if (consumer.subscription() != null && !consumer.subscription().isEmpty()) {
                consumer.unsubscribe();
            }
        } finally {
            consumer.close(Duration.ofSeconds(10));
        }
    }

    /**
     * Create new Kafka consumer with given endpoint configuration.
     */
    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> createConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(CLIENT_ID_CONFIG, Optional.ofNullable(getEndpointConfiguration().getClientId()).orElseGet(() -> KAFKA_PREFIX + "consumer_" + randomUUID()));
        consumerProps.put(GROUP_ID_CONFIG, getEndpointConfiguration().getConsumerGroup());
        consumerProps.put(BOOTSTRAP_SERVERS_CONFIG, Optional.ofNullable(getEndpointConfiguration().getServer()).orElse("localhost:9092"));
        consumerProps.put(MAX_POLL_RECORDS_CONFIG, 1);
        consumerProps.put(ENABLE_AUTO_COMMIT_CONFIG, getEndpointConfiguration().isAutoCommit());
        consumerProps.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, getEndpointConfiguration().getAutoCommitInterval());
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, getEndpointConfiguration().getOffsetReset());
        consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, getEndpointConfiguration().getKeyDeserializer());
        consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, getEndpointConfiguration().getValueDeserializer());

        consumerProps.putAll(getEndpointConfiguration().getConsumerProperties());

        return new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);
    }
}
