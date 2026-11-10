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

import java.util.Collections;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.FutureRecordMetadata;
import org.apache.kafka.clients.producer.internals.ProduceRequestResult;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Time;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.util.SocketUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KafkaProducerTest extends AbstractUnitTest {

    private final KafkaProducer kafkaProducer = Mockito.mock(KafkaProducer.class);

    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultSettings() {
        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createProducer().setProducer(kafkaProducer);

        endpoint.getEndpointConfiguration().setTopic("default");

        final Message message = new KafkaMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                        .messageKey(1);

        reset(kafkaProducer);

        when(kafkaProducer.send(any(ProducerRecord.class))).thenAnswer((Answer<Future<RecordMetadata>>) invocation -> {
            ProducerRecord producerRecord = invocation.getArgument(0);

            Assertions.assertEquals("default", producerRecord.topic());
            Assertions.assertEquals(1, producerRecord.key());
            Assertions.assertEquals(message.getPayload(), producerRecord.value());

            ProduceRequestResult result = new ProduceRequestResult(new TopicPartition("default", 0));
            result.set(0, 0, null);
            result.done();
            return new FutureRecordMetadata(result, 0, System.currentTimeMillis(), 0, message.getPayload(byte[].class).length, Time.SYSTEM);
        });

        endpoint.createProducer().send(message, context);

        verify(kafkaProducer).send(any(ProducerRecord.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithTopicOverwrite() {
        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createProducer().setProducer(kafkaProducer);

        endpoint.getEndpointConfiguration().setTopic("default");

        final Message message = new KafkaMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .topic("foo")
                .messageKey(99);

        reset(kafkaProducer);

        when(kafkaProducer.send(any(ProducerRecord.class))).thenAnswer((Answer<Future<RecordMetadata>>) invocation -> {
            ProducerRecord producerRecord = invocation.getArgument(0);

            Assertions.assertEquals("foo", producerRecord.topic());
            Assertions.assertEquals(99, producerRecord.key());
            Assertions.assertEquals(message.getPayload(), producerRecord.value());

            ProduceRequestResult result = new ProduceRequestResult(new TopicPartition("foo", 0));
            result.set(0, 0, null);
            result.done();
            return new FutureRecordMetadata(result, 0, System.currentTimeMillis(), 0, message.getPayload(byte[].class).length, Time.SYSTEM);
        });

        endpoint.createProducer().send(message, context);

        verify(kafkaProducer).send(any(ProducerRecord.class));
    }

    @Test
    public void testSendMessageTimeout() {
        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.getEndpointConfiguration().setBootstrapServers("localhost:" + SocketUtils.findAvailableTcpPort());
        endpoint.getEndpointConfiguration().setTopic("test");
        endpoint.getEndpointConfiguration().setProducerProperties(Collections.singletonMap(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000));

        try {
            endpoint.createProducer().send(new KafkaMessage("foo"), context);
        } catch(CitrusRuntimeException e) {
            Assertions.assertEquals("Failed to send message to Kafka topic 'test'", e.getMessage());
            Assertions.assertEquals(java.util.concurrent.ExecutionException.class, e.getCause().getClass());
            Assertions.assertTrue(e.getCause().getMessage().contains("org.apache.kafka.common.errors.TimeoutException"));
            return;
        }

        Assertions.fail("Missing " + CitrusRuntimeException.class + " because of message timeout");
    }

    @Test
    public void testSendEmptyMessage() {
        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.getEndpointConfiguration().setBootstrapServers("localhost:9092");
        endpoint.getEndpointConfiguration().setTopic("test");

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class, () -> endpoint.createProducer().send(null, context));
        Assertions.assertEquals("Message is empty - unable to send empty message", e.getMessage());
    }
}
