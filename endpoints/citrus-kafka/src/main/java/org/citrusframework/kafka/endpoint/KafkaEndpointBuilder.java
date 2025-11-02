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

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.8
 */
public class KafkaEndpointBuilder extends AbstractEndpointBuilder<KafkaEndpoint> {

    /** Endpoint target */
    private final KafkaEndpoint endpoint = new KafkaEndpoint();

    private String messageConverter;
    private String headerMapper;

    @Override
    public KafkaEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, KafkaMessageConverter.class));
            }

            if (StringUtils.hasText(headerMapper)) {
                headerMapper(referenceResolver.resolve(headerMapper, KafkaMessageHeaderMapper.class));
            }
        }

        return super.build();
    }

    @Override
    protected KafkaEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the server property.
     */
    public KafkaEndpointBuilder server(String server) {
        endpoint.getEndpointConfiguration().setServer(server);
        return this;
    }

    @SchemaProperty(description = "Sets the Kafka bootstrap server.")
    public void setServer(String server) {
        server(server);
    }

    /**
     * Sets the topicName property.
     */
    public KafkaEndpointBuilder topic(String topicName) {
        endpoint.getEndpointConfiguration().setTopic(topicName);
        return this;
    }

    @SchemaProperty(description = "Sets the Kafka topic.")
    public void setTopic(String topic) {
        topic(topic);
    }

    /**
     * Sets the partition property.
     */
    public KafkaEndpointBuilder partition(int partition) {
        endpoint.getEndpointConfiguration().setPartition(partition);
        return this;
    }

    @SchemaProperty(description = "Sets the Kafka topic partition.")
    public void setPartition(int partition) {
        partition(partition);
    }

    /**
     * Sets the autoCommit property.
     */
    public KafkaEndpointBuilder autoCommit(boolean autoCommit) {
        endpoint.getEndpointConfiguration().setAutoCommit(autoCommit);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets auto commit handling for this endpoint.")
    public void setAutoCommit(boolean autoCommit) {
        autoCommit(autoCommit);
    }

    /**
     * Sets the autoCommitInterval property.
     */
    public KafkaEndpointBuilder autoCommitInterval(int autoCommitInterval) {
        endpoint.getEndpointConfiguration().setAutoCommitInterval(autoCommitInterval);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the auto commit interval.")
    public void setAutoCommitInterval(int autoCommitInterval) {
        autoCommitInterval(autoCommitInterval);
    }

    /**
     * Sets the offsetReset property.
     */
    public KafkaEndpointBuilder offsetReset(String offsetReset) {
        endpoint.getEndpointConfiguration().setOffsetReset(offsetReset);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the offset reset.")
    public void setOffsetReset(String offsetReset) {
        offsetReset(offsetReset);
    }

    /**
     * Sets the clientId property.
     */
    public KafkaEndpointBuilder clientId(String clientId) {
        endpoint.getEndpointConfiguration().setClientId(clientId);
        return this;
    }

    @SchemaProperty(
            advanced = true,
            description = "Sets the client id used to connect to the Kafka server.")
    public void setClientId(String clientId) {
        clientId(clientId);
    }

    /**
     * Sets the consumer group property.
     */
    public KafkaEndpointBuilder consumerGroup(String group) {
        endpoint.getEndpointConfiguration().setConsumerGroup(group);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the consumer group used by the endpoint.")
    public void setConsumerGroup(String group) {
        consumerGroup(group);
    }

    /**
     * Sets the messageConverter property.
     */
    public KafkaEndpointBuilder messageConverter(KafkaMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the headerMapper property.
     */
    public KafkaEndpointBuilder headerMapper(KafkaMessageHeaderMapper headerMapper) {
        endpoint.getEndpointConfiguration().setHeaderMapper(headerMapper);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the Kafka header mapper.")
    public void setHeaderMapper(String headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the key serializer property.
     */
    public KafkaEndpointBuilder keySerializer(Class<? extends Serializer> serializer) {
        endpoint.getEndpointConfiguration().setKeySerializer(serializer);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:serialize") },
            description = "Sets the fully qualified key serializer type class name.")
    public void setKeySerializer(String serializerType) {
        try {
            keySerializer((Class<? extends Serializer>) Class.forName(serializerType));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to set key serializer type", e);
        }
    }

    /**
     * Sets the value serializer property.
     */
    public KafkaEndpointBuilder valueSerializer(Class<? extends Serializer> serializer) {
        endpoint.getEndpointConfiguration().setValueSerializer(serializer);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:serialize") },
            description = "Sets the fully qualified value serializer type class name.")
    public void setValueSerializer(String serializerType) {
        try {
            valueSerializer((Class<? extends Serializer>) Class.forName(serializerType));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to set value serializer type", e);
        }
    }

    /**
     * Sets the key deserializer property.
     */
    public KafkaEndpointBuilder keyDeserializer(Class<? extends Deserializer> deserializer) {
        endpoint.getEndpointConfiguration().setKeyDeserializer(deserializer);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:deserialize") },
            description = "Sets the fully qualified key deserializer type class name.")
    public void setKeyDeserializer(String serializerType) {
        try {
            keyDeserializer((Class<? extends Deserializer>) Class.forName(serializerType));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to set key deserializer type", e);
        }
    }

    /**
     * Sets the value deserializer property.
     */
    public KafkaEndpointBuilder valueDeserializer(Class<? extends Deserializer> deserializer) {
        endpoint.getEndpointConfiguration().setValueDeserializer(deserializer);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:deserialize") },
            description = "Sets the fully qualified value deserializer type class name.")
    public void setValueDeserializer(String serializerType) {
        try {
            valueDeserializer((Class<? extends Deserializer>) Class.forName(serializerType));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to set value deserializer type", e);
        }
    }

    /**
     * Sets the producer properties.
     */
    public KafkaEndpointBuilder producerProperties(Map<String, Object> producerProperties) {
        endpoint.getEndpointConfiguration().setProducerProperties(producerProperties);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:produce") },
            description = "Sets the Kafka producer properties.")
    public void setProducerProperties(Map<String, Object> producerProperties) {
        producerProperties(producerProperties);
    }

    /**
     * Sets the consumer properties.
     */
    public KafkaEndpointBuilder consumerProperties(Map<String, Object> consumerProperties) {
        endpoint.getEndpointConfiguration().setConsumerProperties(consumerProperties);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the Kafka consumer properties.")
    public void setConsumerProperties(Map<String, Object> consumerProperties) {
        consumerProperties(consumerProperties);
    }

    /**
     * Sets the default timeout.
     */
    public KafkaEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the Kafka consumer timeout.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
