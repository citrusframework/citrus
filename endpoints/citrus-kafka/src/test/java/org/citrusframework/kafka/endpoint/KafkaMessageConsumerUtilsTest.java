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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.message.KafkaMessageConverter;
import org.citrusframework.message.Message;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

public class KafkaMessageConsumerUtilsTest {

    @Mock
    private TestContext testContextMock;

    private AutoCloseable mockitoContext;

    @BeforeMethod
    public void beforeMethodSetup() {
        mockitoContext = openMocks(this);
    }

    @Test
    public void resolveTopic() {
        var kafkaEndpointConfiguration = new KafkaEndpointConfiguration();

        var topic = "earth";
        kafkaEndpointConfiguration.setTopic(topic);

        var resolvedTopic = "vogon";
        doReturn(resolvedTopic).when(testContextMock).replaceDynamicContentInString(topic);

        assertThat(KafkaMessageConsumerUtils.resolveTopic(kafkaEndpointConfiguration, testContextMock))
                .isEqualTo(resolvedTopic);

        verify(testContextMock).replaceDynamicContentInString(topic);
    }

    @Test
    public void resolveTopic_throwsException_ifTopicIsNull() {
        var kafkaEndpointConfiguration = new KafkaEndpointConfiguration();

        assertThatThrownBy(() -> KafkaMessageConsumerUtils.resolveTopic(kafkaEndpointConfiguration, testContextMock))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Missing Kafka topic to receive messages from - add topic to endpoint configuration");

        verifyNoInteractions(testContextMock);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void parseConsumerRecordsToMessage() {
        var consumerRecordMock = mock(ConsumerRecord.class);
        List<ConsumerRecord<Object, Object>> consumerRecords = singletonList(consumerRecordMock);

        var kafkaEndpointConfiguration = new KafkaEndpointConfiguration();

        var messageConverterMock = mock(KafkaMessageConverter.class);
        kafkaEndpointConfiguration.setMessageConverter(messageConverterMock);

        var messageMock = mock(Message.class);
        doReturn(messageMock).when(messageConverterMock).convertInbound(consumerRecordMock, kafkaEndpointConfiguration, testContextMock);

        var result = KafkaMessageConsumerUtils.parseConsumerRecordsToMessage(consumerRecords, kafkaEndpointConfiguration, testContextMock);

        assertThat(result)
                .isEqualTo(messageMock);

        verify(testContextMock).onInboundMessage(messageMock);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void parseConsumerRecordsToMessage_throwsException_ifNonUniqueResultMapped() {
        List<ConsumerRecord<Object, Object>> consumerRecords = List.of(
                mock(ConsumerRecord.class),
                mock(ConsumerRecord.class)
        );

        var kafkaEndpointConfiguration = new KafkaEndpointConfiguration();

        var topic = "Mars";
        kafkaEndpointConfiguration.setTopic(topic);
        doReturn(topic).when(testContextMock).replaceDynamicContentInString(topic);

        assertThatThrownBy(() -> KafkaMessageConsumerUtils.parseConsumerRecordsToMessage(consumerRecords, kafkaEndpointConfiguration, testContextMock))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("More than one matching record found in topic " + topic);
    }

    @AfterMethod
    public void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }
}
