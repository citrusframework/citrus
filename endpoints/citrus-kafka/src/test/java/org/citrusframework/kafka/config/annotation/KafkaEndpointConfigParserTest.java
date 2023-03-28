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

package org.citrusframework.kafka.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class KafkaEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @KafkaEndpointConfig(server="localhost:9091", topic="test")
    private KafkaEndpoint kafkaEndpoint1;

    @CitrusEndpoint
    @KafkaEndpointConfig(server="localhost:9092",
            clientId = "kafkaEndpoint2",
            topic="test",
            partition = 1,
            timeout=10000L,
            autoCommit = false,
            autoCommitInterval = 500,
            offsetReset = "latest",
            messageConverter="messageConverter",
            headerMapper = "headerMapper",
            keySerializer = IntegerSerializer.class,
            valueSerializer = ByteArraySerializer.class,
            keyDeserializer = IntegerDeserializer.class,
            valueDeserializer = ByteArrayDeserializer.class,
            consumerGroup = "citrus_group")
    private KafkaEndpoint kafkaEndpoint2;

    @CitrusEndpoint
    @KafkaEndpointConfig(server="localhost:9093",
            topic="test",
            producerProperties = "producerProps",
            consumerProperties = "consumerProps",
            actor="testActor")
    private KafkaEndpoint kafkaEndpoint3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KafkaMessageConverter messageConverter;
    @Mock
    private KafkaMessageHeaderMapper headerMapper;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", KafkaMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("headerMapper", KafkaMessageHeaderMapper.class)).thenReturn(headerMapper);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testKafkaEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st endpoint
        Assert.assertNotNull(kafkaEndpoint1.getEndpointConfiguration().getServer());
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getServer(), "localhost:9091");
        Assert.assertNull(kafkaEndpoint1.getEndpointConfiguration().getClientId());
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getHeaderMapper().getClass(), KafkaMessageHeaderMapper.class);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getMessageConverter().getClass(), KafkaMessageConverter.class);
        Assert.assertTrue(kafkaEndpoint1.getEndpointConfiguration().isAutoCommit());
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getAutoCommitInterval(), 1000L);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getOffsetReset(), "earliest");
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getPartition(), 0);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getConsumerGroup(), KafkaMessageHeaders.KAFKA_PREFIX + "group");
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getKeySerializer(), StringSerializer.class);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getValueSerializer(), StringSerializer.class);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getKeyDeserializer(), StringDeserializer.class);
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().getValueDeserializer(), StringDeserializer.class);

        // 2nd endpoint
        Assert.assertNotNull(kafkaEndpoint2.getEndpointConfiguration().getServer());
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getServer(), "localhost:9092");
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getClientId(), "kafkaEndpoint2");
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getHeaderMapper(), headerMapper);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertFalse(kafkaEndpoint2.getEndpointConfiguration().isAutoCommit());
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getAutoCommitInterval(), 500L);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getOffsetReset(), "latest");
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getPartition(), 1);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getConsumerGroup(), "citrus_group");
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getKeySerializer(), IntegerSerializer.class);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getValueSerializer(), ByteArraySerializer.class);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getKeyDeserializer(), IntegerDeserializer.class);
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().getValueDeserializer(), ByteArrayDeserializer.class);

        // 3rd endpoint
        Assert.assertNotNull(kafkaEndpoint3.getActor());
        Assert.assertEquals(kafkaEndpoint3.getActor(), testActor);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("kafka.type"));
        Assert.assertEquals(validators.get("kafka.type").getClass(), KafkaEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("kafka").isPresent());
    }
}
