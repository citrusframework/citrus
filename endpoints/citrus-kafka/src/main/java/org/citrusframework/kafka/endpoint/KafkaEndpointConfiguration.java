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

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.kafka.message.*;
import org.apache.kafka.common.serialization.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Client id */
    private String clientId;

    /** Consumer group id */
    private String consumerGroup = KafkaMessageHeaders.KAFKA_PREFIX + "group";

    /** The topic name */
    private String topic;

    /** List of server to connect with */
    private String server = "localhost:9092";

    /** The header mapper */
    private KafkaMessageHeaderMapper headerMapper = new KafkaMessageHeaderMapper();

    /** The message converter */
    private KafkaMessageConverter messageConverter = new KafkaMessageConverter();

    /** Key and value serializer types */
    private Class<? extends Serializer> keySerializer = StringSerializer.class;
    private Class<? extends Serializer> valueSerializer = StringSerializer.class;

    /** Key and value deserializer types */
    private Class<? extends Deserializer> keyDeserializer = StringDeserializer.class;
    private Class<? extends Deserializer> valueDeserializer = StringDeserializer.class;

    /** Consumer/producer properties */
    private Map<String, Object> consumerProperties = new HashMap<>();
    private Map<String, Object> producerProperties = new HashMap<>();

    /** Auto commit setting for consumer */
    private boolean autoCommit = true;
    private int autoCommitInterval = 1000;

    /** Offset reset setting for consumer  */
    private String offsetReset = "earliest";

    /** Topic partition */
    private int partition = 0;

    /**
     * Gets the topic name.
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the topic name.
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public KafkaMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(KafkaMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the headerMapper.
     *
     * @return
     */
    public KafkaMessageHeaderMapper getHeaderMapper() {
        return headerMapper;
    }

    /**
     * Sets the headerMapper.
     *
     * @param headerMapper
     */
    public void setHeaderMapper(KafkaMessageHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the server.
     *
     * @param server
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Gets the server.
     *
     * @return
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the group.
     *
     * @return
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Sets the group.
     *
     * @param consumerGroup
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /**
     * Gets the keySerializer.
     *
     * @return
     */
    public Class<? extends Serializer> getKeySerializer() {
        return keySerializer;
    }

    /**
     * Sets the keySerializer.
     *
     * @param keySerializer
     */
    public void setKeySerializer(Class<? extends Serializer> keySerializer) {
        this.keySerializer = keySerializer;
    }

    /**
     * Gets the valueSerializer.
     *
     * @return
     */
    public Class<? extends Serializer> getValueSerializer() {
        return valueSerializer;
    }

    /**
     * Sets the valueSerializer.
     *
     * @param valueSerializer
     */
    public void setValueSerializer(Class<? extends Serializer> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    /**
     * Gets the keyDeserializer.
     *
     * @return
     */
    public Class<? extends Deserializer> getKeyDeserializer() {
        return keyDeserializer;
    }

    /**
     * Sets the keyDeserializer.
     *
     * @param keyDeserializer
     */
    public void setKeyDeserializer(Class<? extends Deserializer> keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }

    /**
     * Gets the valueDeserializer.
     *
     * @return
     */
    public Class<? extends Deserializer> getValueDeserializer() {
        return valueDeserializer;
    }

    /**
     * Sets the valueDeserializer.
     *
     * @param valueDeserializer
     */
    public void setValueDeserializer(Class<? extends Deserializer> valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }

    /**
     * Gets the consumerProperties.
     *
     * @return
     */
    public Map<String, Object> getConsumerProperties() {
        return consumerProperties;
    }

    /**
     * Sets the consumerProperties.
     *
     * @param consumerProperties
     */
    public void setConsumerProperties(Map<String, Object> consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    /**
     * Gets the producerProperties.
     *
     * @return
     */
    public Map<String, Object> getProducerProperties() {
        return producerProperties;
    }

    /**
     * Sets the producerProperties.
     *
     * @param producerProperties
     */
    public void setProducerProperties(Map<String, Object> producerProperties) {
        this.producerProperties = producerProperties;
    }

    /**
     * Gets the clientId.
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the clientId.
     *
     * @param clientId
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the offsetReset.
     *
     * @return
     */
    public String getOffsetReset() {
        return offsetReset;
    }

    /**
     * Sets the offsetReset.
     *
     * @param offsetReset
     */
    public void setOffsetReset(String offsetReset) {
        this.offsetReset = offsetReset;
    }

    /**
     * Gets the autoCommit.
     *
     * @return
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Sets the autoCommit.
     *
     * @param autoCommit
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Gets the autoCommitInterval.
     *
     * @return
     */
    public int getAutoCommitInterval() {
        return autoCommitInterval;
    }

    /**
     * Sets the autoCommitInterval.
     *
     * @param autoCommitInterval
     */
    public void setAutoCommitInterval(int autoCommitInterval) {
        this.autoCommitInterval = autoCommitInterval;
    }

    /**
     * Gets the partition.
     *
     * @return
     */
    public int getPartition() {
        return partition;
    }

    /**
     * Sets the partition.
     *
     * @param partition
     */
    public void setPartition(int partition) {
        this.partition = partition;
    }
}
