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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

public class KafkaConsumerTest extends AbstractTestNGUnitTest {

    private final org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumerMock = mock(KafkaConsumer.class);

    @Test
    public void receiveMessage() {
        String topic = "default";

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .topic(topic)
            .build();

        var partition = new TopicPartition(topic, 0);

        reset(kafkaConsumerMock);

        when(kafkaConsumerMock.subscription()).thenReturn(emptySet());
        doAnswer(invocation -> {
            List<String> topics = invocation.getArgument(0);
            assertEquals(topics.size(), 1L);
            assertEquals(topics.get(0), topic);

            return null;
        }).when(kafkaConsumerMock).subscribe(anyList());

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(singletonMap(partition, singletonList(consumerRecord)));
        when(kafkaConsumerMock.poll(Duration.ofMillis(5000L))).thenReturn(records);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message receivedMessage = endpoint.createConsumer().receive(context);
        assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
    }

    @Test
    public void receiveMessage_inRandomConsumerGroup() {
        String topic = "default";

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .topic(topic)
            .build();

        var partition = new TopicPartition(topic, 0);

        reset(kafkaConsumerMock);

        when(kafkaConsumerMock.subscription()).thenReturn(emptySet());
        doAnswer(invocation -> {
            List<String> topics = invocation.getArgument(0);
            assertEquals(topics.size(), 1L);
            assertEquals(topics.get(0), topic);

            return null;
        }).when(kafkaConsumerMock).subscribe(anyList());

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(singletonMap(partition, singletonList(consumerRecord)));
        when(kafkaConsumerMock.poll(Duration.ofMillis(5000L))).thenReturn(records);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message receivedMessage = endpoint.createConsumer().receive(context);
        assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
    }

    @Test
    public void receiveMessage_runIntoTimeout() {
        String topic = "test";

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .server("localhost:9092")
            .topic(topic)
            .build();

        reset(kafkaConsumerMock);
        when(kafkaConsumerMock.subscription()).thenReturn(singleton(topic));

        when(kafkaConsumerMock.poll(Duration.ofMillis(10000L))).thenReturn(ConsumerRecords.EMPTY);

        try {
            endpoint.createConsumer().receive(context, 10000L);
        } catch (ActionTimeoutException e) {
            assertTrue(e.getMessage().startsWith("Action timeout after 10000 milliseconds. Failed to receive message on endpoint: 'test'"));
            return;
        }

        fail("Missing " + ActionTimeoutException.class + " because of receiving message timeout");
    }

    @Test
    public void receiveMessage_customTimeout_runIntoTimeout() {
        String topic = "timeout";

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .timeout(10_000L)
            .topic(topic)
            .build();

        var partition = new TopicPartition(topic, 0);

        reset(kafkaConsumerMock);
        when(kafkaConsumerMock.subscription()).thenReturn(singleton(topic));

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(singletonMap(partition, singletonList(consumerRecord)));
        when(kafkaConsumerMock.poll(Duration.ofMillis(10000L))).thenReturn(records);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message receivedMessage = endpoint.createConsumer().receive(context);
        assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
    }

    @Test
    public void receiveMessage_withMessageHeaders() {
        String topic = "headers";

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .server("localhost:9092")
            .topic(topic)
            .build();

        var partition = new TopicPartition(topic, 0);

        reset(kafkaConsumerMock);
        when(kafkaConsumerMock.subscription()).thenReturn(singleton(topic));

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        consumerRecord.headers().add(new RecordHeader("Operation", "sayHello".getBytes()));
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(singletonMap(partition, singletonList(consumerRecord)));
        when(kafkaConsumerMock.poll(Duration.ofMillis(5000L))).thenReturn(records);

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        Message receivedMessage = endpoint.createConsumer().receive(context);
        assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        assertNotNull(receivedMessage.getHeader("Operation"));
        assertEquals(receivedMessage.getHeader("Operation"), "sayHello");
    }

    @Test
    public void getConsumer_returnsSetConsumer() {
        var kafkaConsumerMock = mock(KafkaConsumer.class);
        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .build();

        var result = endpoint.createConsumer().getConsumer();
        assertThat(result)
            .isEqualTo(kafkaConsumerMock);
    }

    @Test
    public void getConsumer_createsConsumerIfNonSet() {
        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(null) // null for explicity
            .build();

        var result = endpoint.createConsumer().getConsumer();
        assertThat(result)
            .isNotNull();
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void stop_unsubscribesAndClosesConsumer() {
        var kafkaConsumerMock = mock(KafkaConsumer.class);
        doReturn(Set.of("subscription")).when(kafkaConsumerMock).subscription();

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .build();

        endpoint.createConsumer().stop();
        verify(kafkaConsumerMock).unsubscribe();
        verify(kafkaConsumerMock).close(Duration.ofSeconds(10));
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void stop_closesConsumerEvenAfterUnsubscriptionError() {
        var kafkaConsumerMock = mock(KafkaConsumer.class);
        var unsubscribeException = new RuntimeException();
        doReturn(Set.of("subscription")).when(kafkaConsumerMock).subscription();
        doThrow(unsubscribeException).when(kafkaConsumerMock).unsubscribe();

        KafkaEndpoint endpoint = KafkaEndpoint.builder()
            .kafkaConsumer(kafkaConsumerMock)
            .build();

        assertThatThrownBy(() -> endpoint.createConsumer().stop())
            .isEqualTo(unsubscribeException);

        verify(kafkaConsumerMock).close(Duration.ofSeconds(10));
    }
}
