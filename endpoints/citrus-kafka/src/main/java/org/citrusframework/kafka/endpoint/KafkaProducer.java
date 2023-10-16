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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaProducer implements Producer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    /** The producer name. */
    private final String name;

    /** Endpoint configuration */
    private final KafkaEndpointConfiguration endpointConfiguration;

    /** Kafka producer */
    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public KafkaProducer(String name, KafkaEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
        this. producer = createKafkaProducer();
    }

    @Override
    public void send(final Message message, final TestContext context) {
        if (message == null) {
            throw new CitrusRuntimeException("Message is empty - unable to send empty message");
        }

        String topic = Optional.ofNullable(message.getHeader(KafkaMessageHeaders.TOPIC))
                .map(Object::toString)
                .map(context::replaceDynamicContentInString)
                .orElseGet(() -> context.replaceDynamicContentInString(endpointConfiguration.getTopic()));

        if (!StringUtils.hasText(topic)) {
            throw new CitrusRuntimeException(String.format("Invalid Kafka stream topic header %s - must not be empty or null", KafkaMessageHeaders.TOPIC));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending Kafka stream message to topic: '" + topic + "'");
        }

        try {
            ProducerRecord<Object, Object> producerRecord = endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context);
            producer.send(producerRecord).get(endpointConfiguration.getTimeout(), TimeUnit.MILLISECONDS);
            logger.info("Message was sent to Kafka stream topic: '" + topic + "'");
        } catch (InterruptedException | ExecutionException e) {
            throw new CitrusRuntimeException(String.format("Failed to send message to Kafka topic '%s'", topic), e);
        } catch (TimeoutException e) {
            throw new CitrusRuntimeException(String.format("Failed to send message to Kafka topic '%s' - timeout after %s milliseconds", topic, endpointConfiguration.getTimeout()), e);
        }

        context.onOutboundMessage(message);
    }

    /**
     * Creates default KafkaTemplate instance from endpoint configuration.
     */
    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> createKafkaProducer() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, endpointConfiguration.getServer());
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Long.valueOf(endpointConfiguration.getTimeout()).intValue());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, endpointConfiguration.getKeySerializer());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, endpointConfiguration.getValueSerializer());

        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, Optional.ofNullable(endpointConfiguration.getClientId()).orElseGet(()  -> KafkaMessageHeaders.KAFKA_PREFIX + "producer_" + UUID.randomUUID()));

        producerProps.putAll(endpointConfiguration.getProducerProperties());

        return new org.apache.kafka.clients.producer.KafkaProducer<>(producerProps);
    }


    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the producer.
     *
     * @param producer
     */
    public void setProducer(org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer) {
        this.producer = producer;
    }
}
