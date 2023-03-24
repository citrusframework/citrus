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

package org.citrusframework.kafka.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class KafkaEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKafkaEndpointParser() {
        Map<String, KafkaEndpoint> endpoints = beanDefinitionContext.getBeansOfType(KafkaEndpoint.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st message receiver
        KafkaEndpoint kafkaEndpoint = endpoints.get("kafkaEndpoint1");
        Assert.assertNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        Assert.assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9091");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getHeaderMapper().getClass(), KafkaMessageHeaderMapper.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), KafkaMessageConverter.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().isAutoCommit(), true);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval(), 1000L);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getOffsetReset(), "earliest");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getPartition(), 0);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup(), KafkaMessageHeaders.KAFKA_PREFIX + "group");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size(), 0);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size(), 0);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeySerializer(), StringSerializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueSerializer(), StringSerializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer(), StringDeserializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueDeserializer(), StringDeserializer.class);

        // 2nd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint2");
        Assert.assertNotNull(kafkaEndpoint.getEndpointConfiguration().getClientId());
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getClientId(), "kafkaEndpoint2");
        Assert.assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9092");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getHeaderMapper(), beanDefinitionContext.getBean("headerMapper"));
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().isAutoCommit(), false);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getAutoCommitInterval(), 500L);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getOffsetReset(), "latest");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getPartition(), 1);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerGroup(), "citrus_group");
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeySerializer(), IntegerSerializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueSerializer(), ByteArraySerializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getKeyDeserializer(), IntegerDeserializer.class);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getValueDeserializer(), ByteArrayDeserializer.class);

        // 3rd message receiver
        kafkaEndpoint = endpoints.get("kafkaEndpoint3");
        Assert.assertNotNull(kafkaEndpoint.getEndpointConfiguration().getServer());
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getServer(), "localhost:9093");
        Assert.assertNull(kafkaEndpoint.getEndpointConfiguration().getTopic());
        Assert.assertNotNull(kafkaEndpoint.getActor());
        Assert.assertEquals(kafkaEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().size(), 1);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getConsumerProperties().get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), true);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().size(), 1);
        Assert.assertEquals(kafkaEndpoint.getEndpointConfiguration().getProducerProperties().get(ProducerConfig.MAX_REQUEST_SIZE_CONFIG), 1024);

    }
}
