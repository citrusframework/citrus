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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.TOPIC;
import static org.citrusframework.util.StringUtils.isEmpty;

public class KafkaProducer implements Producer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    /**
     * The producer name.
     */
    private final String name;

    /**
     * Endpoint configuration.
     */
    private final KafkaEndpointConfiguration endpointConfiguration;

    /**
     * Kafka producer.
     */
    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer;

    /**
     * Default constructor using endpoint configuration.
     */
    public KafkaProducer(String name, KafkaEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
        this.producer = createKafkaProducer();
    }

    @Override
    public String getName() {
        return name;
    }

    public org.apache.kafka.clients.producer.KafkaProducer<Object, Object> getProducer() {
        return producer;
    }

    public void setProducer(org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer) {
        this.producer = producer;
    }

    @Override
    public void send(final Message message, final TestContext context) {
        if (isNull(message)) {
            throw new CitrusRuntimeException("Message is empty - unable to send empty message");
        }

        String topic = Optional.ofNullable(message.getHeader(TOPIC))
                .map(Object::toString)
                .map(context::replaceDynamicContentInString)
                .orElseGet(() -> context.replaceDynamicContentInString(endpointConfiguration.getTopic()));

        if (isEmpty(topic)) {
            throw new CitrusRuntimeException(format("Invalid Kafka stream topic header %s - must not be empty or null", TOPIC));
        }

        logger.debug("Sending Kafka stream message to topic: '{}'", topic);

        try {
            ProducerRecord<Object, Object> producerRecord = endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context);
            producer.send(producerRecord).get(endpointConfiguration.getTimeout(), MILLISECONDS);
            logger.info("Message was sent to Kafka stream topic: '{}'", topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CitrusRuntimeException("Thread was interrupted while publishing Kafka message", e);
        } catch (ExecutionException e) {
            throw new CitrusRuntimeException(format("Failed to send message to Kafka topic '%s'", topic), e);
        } catch (TimeoutException e) {
            throw new CitrusRuntimeException(format("Failed to send message to Kafka topic '%s' - timeout after %s milliseconds", topic, endpointConfiguration.getTimeout()), e);
        }

        context.onOutboundMessage(message);
    }

    /**
     * Creates default KafkaTemplate instance from endpoint configuration.
     */
    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> createKafkaProducer() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(BOOTSTRAP_SERVERS_CONFIG, endpointConfiguration.getServer());
        producerProps.put(REQUEST_TIMEOUT_MS_CONFIG, Long.valueOf(endpointConfiguration.getTimeout()).intValue());
        producerProps.put(KEY_SERIALIZER_CLASS_CONFIG, endpointConfiguration.getKeySerializer());
        producerProps.put(VALUE_SERIALIZER_CLASS_CONFIG, endpointConfiguration.getValueSerializer());

        producerProps.put(CLIENT_ID_CONFIG, Optional.ofNullable(endpointConfiguration.getClientId()).orElseGet(() -> KafkaMessageHeaders.KAFKA_PREFIX + "producer_" + UUID.randomUUID()));

        producerProps.putAll(endpointConfiguration.getProducerProperties());

        return new org.apache.kafka.clients.producer.KafkaProducer<>(producerProps);
    }
}
