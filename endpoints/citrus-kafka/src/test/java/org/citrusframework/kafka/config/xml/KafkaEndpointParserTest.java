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
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class KafkaEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKafkaEndpointParser() {
        Map<String, KafkaEndpoint> endpoints = beanDefinitionContext.getBeansOfType(KafkaEndpoint.class);

        assertThat(endpoints)
            .hasSize(4);

        // 1st message receiver
        KafkaEndpoint kafkaEndpoint = endpoints.get("kafkaEndpoint1");
        assertNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9091");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getHeaderMapper().getClass(), KafkaMessageHeaderMapper.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), KafkaMessageConverter.class);
        assertTrue(kafkaEndpoint.getEndpointConfiguration().isAutoCommit());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval(), 1000L);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getOffsetReset(), "earliest");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getTopic(), "test");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getPartition(), 0);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup(), KAFKA_PREFIX + "group");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size(), 0);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size(), 0);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeySerializer(), StringSerializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueSerializer(), StringSerializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer(), StringDeserializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueDeserializer(), StringDeserializer.class);

        // 2nd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint2");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getClientId(), "kafkaEndpoint2");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9092");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getHeaderMapper(), beanDefinitionContext.getBean("headerMapper"));
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        assertFalse(kafkaEndpoint.getEndpointConfiguration().isAutoCommit());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval(), 500L);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getOffsetReset(), "latest");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getTopic(), "test");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getPartition(), 1);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup(), "citrus_group");
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeySerializer(), IntegerSerializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueSerializer(), ByteArraySerializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer(), IntegerDeserializer.class);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueDeserializer(), ByteArrayDeserializer.class);

        // 3rd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint3");
        assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9093");
        assertNull(kafkaEndpoint.getEndpointConfiguration().getTopic());
        assertNotNull(kafkaEndpoint.getActor());
        assertEquals(kafkaEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size(), 1);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), true);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size(), 1);
        assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().get(ProducerConfig.MAX_REQUEST_SIZE_CONFIG), 1024);

        // 4th message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint4");
        assertThat(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup())
            .startsWith(KAFKA_PREFIX)
            .hasSize(23)
            .containsPattern(".*[a-z]{10}$");
    }
}
