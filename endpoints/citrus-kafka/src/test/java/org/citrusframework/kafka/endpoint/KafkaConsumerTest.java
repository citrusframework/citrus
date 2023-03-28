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

package org.citrusframework.kafka.endpoint;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class KafkaConsumerTest extends AbstractTestNGUnitTest {

    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer = Mockito.mock(KafkaConsumer.class);

    @Test
    public void testReceiveMessage() {
        String topic = "default";

        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createConsumer().setConsumer(kafkaConsumer);

        endpoint.getEndpointConfiguration().setTopic(topic);

        TopicPartition partition = new TopicPartition(topic, 0);

        reset(kafkaConsumer);

        when(kafkaConsumer.subscription()).thenReturn(Collections.emptySet());
        doAnswer(invocation -> {
            List<String> topics = invocation.getArgument(0);
            Assert.assertEquals(topics.size(), 1L);
            Assert.assertEquals(topics.get(0), topic);

            return null;
        }).when(kafkaConsumer).subscribe(anyList());

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(Collections.singletonMap(partition, Collections.singletonList(consumerRecord)));
        when(kafkaConsumer.poll(Duration.ofMillis(5000L))).thenReturn(records);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
    }

    @Test
    public void testReceiveMessageTimeout() {
        String topic = "test";

        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createConsumer().setConsumer(kafkaConsumer);

        endpoint.getEndpointConfiguration().setServer("localhost:9092");
        endpoint.getEndpointConfiguration().setTopic(topic);

        reset(kafkaConsumer);
        when(kafkaConsumer.subscription()).thenReturn(Collections.singleton(topic));

        when(kafkaConsumer.poll(Duration.ofMillis(10000L))).thenReturn(ConsumerRecords.EMPTY);

        try {
            endpoint.createConsumer().receive(context, 10000L);
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().startsWith("Action timeout after 10000 milliseconds. Failed to receive message on endpoint: 'test'"));
            return;
        }

        Assert.fail("Missing " + ActionTimeoutException.class + " because of receiving message timeout");
    }

    @Test
    public void testWithCustomTimeout() {
        String topic = "timeout";

        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createConsumer().setConsumer(kafkaConsumer);

        endpoint.getEndpointConfiguration().setTopic(topic);

        endpoint.getEndpointConfiguration().setTimeout(10000L);

        TopicPartition partition = new TopicPartition(topic, 0);

        reset(kafkaConsumer);
        when(kafkaConsumer.subscription()).thenReturn(Collections.singleton(topic));

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(Collections.singletonMap(partition, Collections.singletonList(consumerRecord)));
        when(kafkaConsumer.poll(Duration.ofMillis(10000L))).thenReturn(records);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
    }

    @Test
    public void testWithMessageHeaders() {
        String topic = "headers";

        KafkaEndpoint endpoint = new KafkaEndpoint();
        endpoint.createConsumer().setConsumer(kafkaConsumer);

        endpoint.getEndpointConfiguration().setServer("localhost:9092");
        endpoint.getEndpointConfiguration().setTopic(topic);

        TopicPartition partition = new TopicPartition(topic, 0);

        reset(kafkaConsumer);
        when(kafkaConsumer.subscription()).thenReturn(Collections.singleton(topic));

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, 0, 0, 1, "<TestRequest><Message>Hello World!</Message></TestRequest>");
        consumerRecord.headers().add(new RecordHeader("Operation", "sayHello".getBytes()));
        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(Collections.singletonMap(partition, Collections.singletonList(consumerRecord)));
        when(kafkaConsumer.poll(Duration.ofMillis(5000L))).thenReturn(records);

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        Assert.assertNotNull(receivedMessage.getHeader("Operation"));
        Assert.assertTrue(receivedMessage.getHeader("Operation").equals("sayHello"));
    }
}
