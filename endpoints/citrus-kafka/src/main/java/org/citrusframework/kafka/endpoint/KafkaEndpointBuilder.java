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

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpointBuilder extends AbstractEndpointBuilder<KafkaEndpoint> {

    /** Endpoint target */
    private KafkaEndpoint endpoint = new KafkaEndpoint();

    @Override
    protected KafkaEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the server property.
     * @param server
     * @return
     */
    public KafkaEndpointBuilder server(String server) {
        endpoint.getEndpointConfiguration().setServer(server);
        return this;
    }

    /**
     * Sets the topicName property.
     * @param topicName
     * @return
     */
    public KafkaEndpointBuilder topic(String topicName) {
        endpoint.getEndpointConfiguration().setTopic(topicName);
        return this;
    }

    /**
     * Sets the partition property.
     * @param partition
     * @return
     */
    public KafkaEndpointBuilder partition(int partition) {
        endpoint.getEndpointConfiguration().setPartition(partition);
        return this;
    }

    /**
     * Sets the autoCommit property.
     * @param autoCommit
     * @return
     */
    public KafkaEndpointBuilder autoCommit(boolean autoCommit) {
        endpoint.getEndpointConfiguration().setAutoCommit(autoCommit);
        return this;
    }

    /**
     * Sets the autoCommitInterval property.
     * @param autoCommitInterval
     * @return
     */
    public KafkaEndpointBuilder autoCommitInterval(int autoCommitInterval) {
        endpoint.getEndpointConfiguration().setAutoCommitInterval(autoCommitInterval);
        return this;
    }

    /**
     * Sets the offsetReset property.
     * @param offsetReset
     * @return
     */
    public KafkaEndpointBuilder offsetReset(String offsetReset) {
        endpoint.getEndpointConfiguration().setOffsetReset(offsetReset);
        return this;
    }

    /**
     * Sets the clientId property.
     * @param clientId
     * @return
     */
    public KafkaEndpointBuilder clientId(String clientId) {
        endpoint.getEndpointConfiguration().setClientId(clientId);
        return this;
    }

    /**
     * Sets the consumer group property.
     * @param group
     * @return
     */
    public KafkaEndpointBuilder consumerGroup(String group) {
        endpoint.getEndpointConfiguration().setConsumerGroup(group);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public KafkaEndpointBuilder messageConverter(KafkaMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the headerMapper property.
     * @param headerMapper
     * @return
     */
    public KafkaEndpointBuilder headerMapper(KafkaMessageHeaderMapper headerMapper) {
        endpoint.getEndpointConfiguration().setHeaderMapper(headerMapper);
        return this;
    }

    /**
     * Sets the key serializer property.
     * @param serializer
     * @return
     */
    public KafkaEndpointBuilder keySerializer(Class<? extends Serializer> serializer) {
        endpoint.getEndpointConfiguration().setKeySerializer(serializer);
        return this;
    }

    /**
     * Sets the value serializer property.
     * @param serializer
     * @return
     */
    public KafkaEndpointBuilder valueSerializer(Class<? extends Serializer> serializer) {
        endpoint.getEndpointConfiguration().setValueSerializer(serializer);
        return this;
    }

    /**
     * Sets the key deserializer property.
     * @param deserializer
     * @return
     */
    public KafkaEndpointBuilder keyDeserializer(Class<? extends Deserializer> deserializer) {
        endpoint.getEndpointConfiguration().setKeyDeserializer(deserializer);
        return this;
    }

    /**
     * Sets the value deserializer property.
     * @param deserializer
     * @return
     */
    public KafkaEndpointBuilder valueDeserializer(Class<? extends Deserializer> deserializer) {
        endpoint.getEndpointConfiguration().setValueDeserializer(deserializer);
        return this;
    }

    /**
     * Sets the producer properties.
     * @param producerProperties
     * @return
     */
    public KafkaEndpointBuilder producerProperties(Map<String, Object> producerProperties) {
        endpoint.getEndpointConfiguration().setProducerProperties(producerProperties);
        return this;
    }

    /**
     * Sets the consumer properties.
     * @param consumerProperties
     * @return
     */
    public KafkaEndpointBuilder consumerProperties(Map<String, Object> consumerProperties) {
        endpoint.getEndpointConfiguration().setConsumerProperties(consumerProperties);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public KafkaEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
