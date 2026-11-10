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

package org.citrusframework.kafka.config.annotation;

import java.util.Map;

import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class KafkaEndpointConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint
    @KafkaEndpointConfig(bootstrapServers="localhost:9091", topic="test")
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
    @KafkaEndpointConfig(bootstrapServers="localhost:9093",
            topic="test",
            producerProperties = "producerProps",
            consumerProperties = "consumerProps",
            actor="testActor")
    private KafkaEndpoint kafkaEndpoint3;

    @CitrusEndpoint
    @KafkaEndpointConfig(server="localhost:9094", topic="test")
    private KafkaEndpoint kafkaEndpoint4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KafkaMessageConverter messageConverter;
    @Mock
    private KafkaMessageHeaderMapper headerMapper;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", KafkaMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("headerMapper", KafkaMessageHeaderMapper.class)).thenReturn(headerMapper);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testKafkaEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st endpoint
        Assertions.assertNotNull(kafkaEndpoint1.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("localhost:9091", kafkaEndpoint1.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertNull(kafkaEndpoint1.getEndpointConfiguration().getClientId());
        Assertions.assertEquals(KafkaMessageHeaderMapper.class, kafkaEndpoint1.getEndpointConfiguration().getHeaderMapper().getClass());
        Assertions.assertEquals(KafkaMessageConverter.class, kafkaEndpoint1.getEndpointConfiguration().getMessageConverter().getClass());
        Assertions.assertTrue(kafkaEndpoint1.getEndpointConfiguration().isAutoCommit());
        Assertions.assertEquals(1000L, kafkaEndpoint1.getEndpointConfiguration().getAutoCommitInterval());
        Assertions.assertEquals("earliest", kafkaEndpoint1.getEndpointConfiguration().getOffsetReset());
        Assertions.assertEquals("test", kafkaEndpoint1.getEndpointConfiguration().getTopic());
        Assertions.assertEquals(0, kafkaEndpoint1.getEndpointConfiguration().getPartition());
        Assertions.assertEquals(KafkaMessageHeaders.KAFKA_PREFIX + "group", kafkaEndpoint1.getEndpointConfiguration().getConsumerGroup());
        Assertions.assertEquals(5000L, kafkaEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(StringSerializer.class, kafkaEndpoint1.getEndpointConfiguration().getKeySerializer());
        Assertions.assertEquals(StringSerializer.class, kafkaEndpoint1.getEndpointConfiguration().getValueSerializer());
        Assertions.assertEquals(StringDeserializer.class, kafkaEndpoint1.getEndpointConfiguration().getKeyDeserializer());
        Assertions.assertEquals(StringDeserializer.class, kafkaEndpoint1.getEndpointConfiguration().getValueDeserializer());

        // 2nd endpoint
        Assertions.assertNotNull(kafkaEndpoint2.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("localhost:9092", kafkaEndpoint2.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("kafkaEndpoint2", kafkaEndpoint2.getEndpointConfiguration().getClientId());
        Assertions.assertEquals(headerMapper, kafkaEndpoint2.getEndpointConfiguration().getHeaderMapper());
        Assertions.assertEquals(messageConverter, kafkaEndpoint2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertFalse(kafkaEndpoint2.getEndpointConfiguration().isAutoCommit());
        Assertions.assertEquals(500L, kafkaEndpoint2.getEndpointConfiguration().getAutoCommitInterval());
        Assertions.assertEquals("latest", kafkaEndpoint2.getEndpointConfiguration().getOffsetReset());
        Assertions.assertEquals("test", kafkaEndpoint2.getEndpointConfiguration().getTopic());
        Assertions.assertEquals(1, kafkaEndpoint2.getEndpointConfiguration().getPartition());
        Assertions.assertEquals("citrus_group", kafkaEndpoint2.getEndpointConfiguration().getConsumerGroup());
        Assertions.assertEquals(10000L, kafkaEndpoint2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(IntegerSerializer.class, kafkaEndpoint2.getEndpointConfiguration().getKeySerializer());
        Assertions.assertEquals(ByteArraySerializer.class, kafkaEndpoint2.getEndpointConfiguration().getValueSerializer());
        Assertions.assertEquals(IntegerDeserializer.class, kafkaEndpoint2.getEndpointConfiguration().getKeyDeserializer());
        Assertions.assertEquals(ByteArrayDeserializer.class, kafkaEndpoint2.getEndpointConfiguration().getValueDeserializer());

        // 3rd endpoint
        Assertions.assertNotNull(kafkaEndpoint3.getActor());
        Assertions.assertEquals(testActor, kafkaEndpoint3.getActor());

        // 4th endpoint (bootstrapServers alias)
        Assertions.assertNotNull(kafkaEndpoint4.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("localhost:9094", kafkaEndpoint4.getEndpointConfiguration().getServer());
        Assertions.assertEquals("localhost:9094", kafkaEndpoint4.getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("test", kafkaEndpoint4.getEndpointConfiguration().getTopic());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("kafka.type"));
        Assertions.assertEquals(KafkaEndpointConfigParser.class, validators.get("kafka.type").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("kafka").isPresent());
    }
}
