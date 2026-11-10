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

package org.citrusframework.kafka.config.xml;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.citrusframework.TestActor;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

public class KafkaEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKafkaEndpointParser() {
        Map<String, KafkaEndpoint> endpoints = beanDefinitionContext.getBeansOfType(KafkaEndpoint.class);

        assertThat(endpoints)
            .hasSize(5);

        // 1st message receiver
        KafkaEndpoint kafkaEndpoint = endpoints.get("kafkaEndpoint1");
        assertNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals("localhost:9091", kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals(KafkaMessageHeaderMapper.class, kafkaEndpoint.getEndpointConfiguration().getHeaderMapper().getClass());
        assertEquals(KafkaMessageConverter.class, kafkaEndpoint.getEndpointConfiguration().getMessageConverter().getClass());
        assertTrue(kafkaEndpoint.getEndpointConfiguration().isAutoCommit());
        assertEquals(1000L, kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval());
        assertEquals("earliest", kafkaEndpoint.getEndpointConfiguration().getOffsetReset());
        assertEquals("test", kafkaEndpoint.getEndpointConfiguration().getTopic());
        assertEquals(0, kafkaEndpoint.getEndpointConfiguration().getPartition());
        assertEquals(5000L, kafkaEndpoint.getEndpointConfiguration().getTimeout());
        assertEquals(KAFKA_PREFIX + "group", kafkaEndpoint.getEndpointConfiguration().getConsumerGroup());
        assertEquals(0, kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size());
        assertEquals(0, kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size());
        assertEquals(StringSerializer.class, kafkaEndpoint.getEndpointConfiguration().getKeySerializer());
        assertEquals(StringSerializer.class, kafkaEndpoint.getEndpointConfiguration().getValueSerializer());
        assertEquals(StringDeserializer.class, kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer());
        assertEquals(StringDeserializer.class, kafkaEndpoint.getEndpointConfiguration().getValueDeserializer());
        assertEquals(false, kafkaEndpoint.getEndpointConfiguration().useThreadSafeConsumer());

        // 2nd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint2");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        assertEquals("kafkaEndpoint2", kafkaEndpoint.getEndpointConfiguration().getClientId());
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals("localhost:9092", kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals(beanDefinitionContext.getBean("headerMapper"), kafkaEndpoint.getEndpointConfiguration().getHeaderMapper());
        assertEquals(beanDefinitionContext.getBean("messageConverter"), kafkaEndpoint.getEndpointConfiguration().getMessageConverter());
        assertFalse(kafkaEndpoint.getEndpointConfiguration().isAutoCommit());
        assertEquals(500L, kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval());
        assertEquals("latest", kafkaEndpoint.getEndpointConfiguration().getOffsetReset());
        assertEquals("test", kafkaEndpoint.getEndpointConfiguration().getTopic());
        assertEquals(1, kafkaEndpoint.getEndpointConfiguration().getPartition());
        assertEquals("citrus_group", kafkaEndpoint.getEndpointConfiguration().getConsumerGroup());
        assertEquals(10000L, kafkaEndpoint.getEndpointConfiguration().getTimeout());
        assertEquals(IntegerSerializer.class, kafkaEndpoint.getEndpointConfiguration().getKeySerializer());
        assertEquals(ByteArraySerializer.class, kafkaEndpoint.getEndpointConfiguration().getValueSerializer());
        assertEquals(IntegerDeserializer.class, kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer());
        assertEquals(ByteArrayDeserializer.class, kafkaEndpoint.getEndpointConfiguration().getValueDeserializer());
        assertEquals(false, kafkaEndpoint.getEndpointConfiguration().useThreadSafeConsumer());

        // 3rd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint3");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals("localhost:9093", kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertNull(kafkaEndpoint.getEndpointConfiguration().getTopic());
        assertNotNull(kafkaEndpoint.getActor());
        assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), kafkaEndpoint.getActor());
        assertEquals(1, kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size());
        assertEquals(true, kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals(1, kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size());
        assertEquals(1024, kafkaEndpoint.getEndpointConfiguration().getProducerProperties().get(ProducerConfig.MAX_REQUEST_SIZE_CONFIG));
        assertEquals(false, kafkaEndpoint.getEndpointConfiguration().useThreadSafeConsumer());

        // 4th message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint4");
        assertThat(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup())
            .startsWith(KAFKA_PREFIX)
            .hasSize(23)
            .containsPattern(".*[a-z]{10}$");
        assertEquals(true, kafkaEndpoint.getEndpointConfiguration().useThreadSafeConsumer());

        // 5th message receiver (bootstrap-servers alias)
        kafkaEndpoint = endpoints.get("kafkaEndpoint5");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals("localhost:9094", kafkaEndpoint.getEndpointConfiguration().getServer());
        assertEquals("localhost:9094", kafkaEndpoint.getEndpointConfiguration().getBootstrapServers());
        assertEquals("test", kafkaEndpoint.getEndpointConfiguration().getTopic());
    }
}
