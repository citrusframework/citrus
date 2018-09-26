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

package com.consol.citrus.kafka.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.kafka.endpoint.KafkaEndpoint;
import com.consol.citrus.kafka.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.kafka.common.serialization.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private KafkaMessageConverter messageConverter = Mockito.mock(KafkaMessageConverter.class);
    @Mock
    private KafkaMessageHeaderMapper headerMapper = Mockito.mock(KafkaMessageHeaderMapper.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", KafkaMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("headerMapper", KafkaMessageHeaderMapper.class)).thenReturn(headerMapper);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
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
        Assert.assertEquals(kafkaEndpoint1.getEndpointConfiguration().isAutoCommit(), true);
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
        Assert.assertEquals(kafkaEndpoint2.getEndpointConfiguration().isAutoCommit(), false);
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
}
