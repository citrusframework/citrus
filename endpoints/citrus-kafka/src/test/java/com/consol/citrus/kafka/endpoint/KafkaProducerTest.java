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

package com.consol.citrus.kafka.endpoint;

import java.util.Collections;
import java.util.concurrent.Future;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.kafka.message.KafkaMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.FutureRecordMetadata;
import org.apache.kafka.clients.producer.internals.ProduceRequestResult;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Time;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.util.SocketUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class KafkaProducerTest extends AbstractTestNGUnitTest {

    private KafkaProducer kafkaProducer = Mockito.mock(KafkaProducer.class);

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

            Assert.assertEquals(producerRecord.topic(), "default");
            Assert.assertEquals(producerRecord.key(), 1);
            Assert.assertEquals(producerRecord.value(), message.getPayload());

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

            Assert.assertEquals(producerRecord.topic(), "foo");
            Assert.assertEquals(producerRecord.key(), 99);
            Assert.assertEquals(producerRecord.value(), message.getPayload());

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
        endpoint.getEndpointConfiguration().setServer("localhost:" + SocketUtils.findAvailableTcpPort());
        endpoint.getEndpointConfiguration().setTopic("test");
        endpoint.getEndpointConfiguration().setProducerProperties(Collections.singletonMap(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000));

        try {
            endpoint.createProducer().send(new KafkaMessage("foo"), context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to send message to Kafka topic 'test'");
            Assert.assertEquals(e.getCause().getClass(), java.util.concurrent.ExecutionException.class);
            Assert.assertTrue(e.getCause().getMessage().contains("org.apache.kafka.common.errors.TimeoutException"));
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message timeout");
    }

    @Test
    public void testSendEmptyMessage() {
        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.getEndpointConfiguration().setServer("localhost:9092");
        endpoint.getEndpointConfiguration().setTopic("test");

        try {
            endpoint.createProducer().send(null, context);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Message is empty - unable to send empty message");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of sending empty message");
    }

}
