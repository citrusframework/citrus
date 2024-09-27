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

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.kafka.message.KafkaMessageHeaders;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class KafkaEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /**
     * Client id
     */
    private String clientId;

    /**
     * Consumer group id
     */
    private String consumerGroup = KafkaMessageHeaders.KAFKA_PREFIX + "group";

    /**
     * The topic name
     */
    private String topic;

    /**
     * List of server to connect with
     */
    private String server = "localhost:9092";

    /**
     * The header mapper
     */
    private KafkaMessageHeaderMapper headerMapper = new KafkaMessageHeaderMapper();

    /**
     * The message converter
     */
    private KafkaMessageConverter messageConverter = new KafkaMessageConverter();

    /**
     * Key and value serializer types
     */
    private Class<? extends Serializer> keySerializer = StringSerializer.class;
    private Class<? extends Serializer> valueSerializer = StringSerializer.class;

    /**
     * Key and value deserializer types
     */
    private Class<? extends Deserializer> keyDeserializer = StringDeserializer.class;
    private Class<? extends Deserializer> valueDeserializer = StringDeserializer.class;

    /**
     * Consumer/producer properties
     */
    private Map<String, Object> consumerProperties = new HashMap<>();
    private Map<String, Object> producerProperties = new HashMap<>();

    /**
     * Auto commit setting for consumer
     */
    private boolean autoCommit = true;
    private int autoCommitInterval = 1000;

    /**
     * Offset reset setting for consumer
     */
    private String offsetReset = "earliest";

    /**
     * Topic partition
     */
    private int partition = 0;
}
