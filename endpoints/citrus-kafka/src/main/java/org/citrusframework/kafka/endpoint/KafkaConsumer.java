/*
 * Copyright 2006-2018 the original author or authors.
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

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaConsumer extends AbstractMessageConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    /** Endpoint configuration */
    protected final KafkaEndpointConfiguration endpointConfiguration;

    /** Kafka consumer */
    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

    /**
     * Default constructor using endpoint.
     * @param name
     * @param endpointConfiguration
     */
    public KafkaConsumer(String name, KafkaEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
        this.consumer = createConsumer();
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        String topic = context.replaceDynamicContentInString(Optional.ofNullable(endpointConfiguration.getTopic())
                                                                     .orElseThrow(() -> new CitrusRuntimeException("Missing Kafka topic to receive messages from - add topic to endpoint configuration")));

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving Kafka message on topic: '" + topic);
        }

        if (consumer.subscription() == null || consumer.subscription().isEmpty()) {
            consumer.subscribe(Arrays.stream(topic.split(",")).collect(Collectors.toList()));
        }

        ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(timeout));

        if (records.isEmpty()) {
            throw new MessageTimeoutException(timeout, topic);
        }

        if (logger.isDebugEnabled()) {
            records.forEach(record -> logger.debug("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset()));
        }

        Message received = endpointConfiguration.getMessageConverter()
                                                .convertInbound(records.iterator().next(), endpointConfiguration, context);
        context.onInboundMessage(received);

        consumer.commitSync(Duration.ofMillis(endpointConfiguration.getTimeout()));

        logger.info("Received Kafka message on topic: '" + topic);
        return received;
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
            consumer.close(Duration.ofMillis(10 * 1000L));
        }
    }

    /**
     * Create new Kafka consumer with given endpoint configuration.
     * @return
     */
    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> createConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, Optional.ofNullable(endpointConfiguration.getClientId()).orElseGet(()  -> KafkaMessageHeaders.KAFKA_PREFIX + "consumer_" + UUID.randomUUID().toString()));
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, endpointConfiguration.getConsumerGroup());
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Optional.ofNullable(endpointConfiguration.getServer()).orElse("localhost:9092"));
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, endpointConfiguration.isAutoCommit());
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, endpointConfiguration.getAutoCommitInterval());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, endpointConfiguration.getOffsetReset());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, endpointConfiguration.getKeyDeserializer());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, endpointConfiguration.getValueDeserializer());

        consumerProps.putAll(endpointConfiguration.getConsumerProperties());

        return new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);
    }

    /**
     * Sets the consumer.
     *
     * @param consumer
     */
    public void setConsumer(org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer) {
        this.consumer = consumer;
    }
}
