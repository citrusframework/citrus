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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelector;
import org.citrusframework.message.MessageSelectorBuilder;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class KafkaMessageFilteringConsumerTest {

    private static final Long TIMEOUT = 10_000L;

    @Mock
    private TestContext testContextMock;

    private AutoCloseable mockitoContext;

    @BeforeMethod
    public void beforeMethodSetup() {
        mockitoContext = openMocks(this);
    }

    @Test
    public void builder() {
        var eventLookbackWindow = Duration.ofSeconds(5);
        var kafkaConsumer = mock(KafkaConsumer.class);
        var kafkaMessageSelectorMock = mock(KafkaMessageSelector.class);
        var pollTimeout = Duration.ofSeconds(1);

        var fixture = KafkaMessageFilteringConsumer.builder()
                .consumer(kafkaConsumer)
                .eventLookbackWindow(eventLookbackWindow)
                .kafkaMessageSelector(kafkaMessageSelectorMock)
                .pollTimeout(pollTimeout)
                .build();

        assertThat(fixture).satisfies(
                c -> assertThat(c.getConsumer()).isEqualTo(kafkaConsumer),
                c -> assertThat(c.getKafkaMessageFilter())
                        .satisfies(
                                s -> assertThat(s.getEventLookbackWindow()).isEqualTo(eventLookbackWindow),
                                s -> assertThat(s.getKafkaMessageSelector()).isEqualTo(kafkaMessageSelectorMock),
                                s -> assertThat(s.getPollTimeout()).isEqualTo(pollTimeout)
                        )
        );
    }

    @DataProvider
    public static Object[][] nullishConsumerSelector() {
        return new Object[][]{
                {Duration.ofSeconds(5), mock(KafkaMessageSelector.class), null},
                {Duration.ofSeconds(5), null, Duration.ofSeconds(1)},
                {null, mock(KafkaMessageSelector.class), Duration.ofSeconds(1)},
        };
    }

    @Test(dataProvider = "nullishConsumerSelector")
    public void builder_requiresOneNonNullArgument_toBuildKafkaConsumerSelector(
            Duration eventLookbackWindow,
            KafkaMessageSelector kafkaMessageSelector,
            Duration pollTimeout) {
        var fixture = KafkaMessageFilteringConsumer.builder()
                .eventLookbackWindow(eventLookbackWindow)
                .kafkaMessageSelector(kafkaMessageSelector)
                .pollTimeout(pollTimeout)
                .build();

        assertThat(fixture.getKafkaMessageFilter())
                .satisfies(
                        s -> assertThat(s.getEventLookbackWindow()).isEqualTo(eventLookbackWindow),
                        s -> assertThat(s.getKafkaMessageSelector()).isEqualTo(kafkaMessageSelector),
                        s -> assertThat(s.getPollTimeout()).isEqualTo(pollTimeout)
                );
    }

    @DataProvider
    public static Object[][] emptyStrings() {
        return new Object[][]{{null}, {""}};
    }

    @Test(dataProvider = "emptyStrings")
    public void receive_throwsException_whenNoSelectorGiven(String selector) {
        var fixture = KafkaMessageFilteringConsumer.builder()
                .build();

        assertThatThrownBy(() -> fixture.receive(selector, testContextMock, TIMEOUT))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Cannot invoke filtering kafka message consumer without selectors");
    }

    @Test(dataProvider = "emptyStrings")
    public void receive_doesNotThrowException_whenNoSelectorGiven_butKafkaConsumerSelectorPresent(String selector) {
        var kafkaMessageFilterTest = mock(KafkaMessageFilter.class);

        var randomUUID = UUID.randomUUID().toString();
        doThrow(new CitrusRuntimeException(randomUUID)).when(kafkaMessageFilterTest).sanitize();

        var fixture = KafkaMessageFilteringConsumer.builder()
                .build();

        setField(fixture, "kafkaMessageFilter", kafkaMessageFilterTest, KafkaMessageFilter.class);

        assertThatThrownBy(() -> fixture.receive(selector, testContextMock, TIMEOUT))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage(randomUUID);
    }

    @Test
    public void receive_parsesSelectorString() {
        var kafkaMessageFilterMock = mock(KafkaMessageFilter.class);

        var fixture = KafkaMessageFilteringConsumer.builder()
                .build();

        setField(fixture, "kafkaMessageFilter", kafkaMessageFilterMock, KafkaMessageFilter.class);

        var kafkaMessageSelector = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(Duration.ofSeconds(1L))
                .kafkaMessageSelector(kafkaHeaderEquals("foo", "bar"))
                .build();
        var selector = MessageSelectorBuilder.fromKeyValueMap(kafkaMessageSelector).build();

        assertThatThrownBy(() -> fixture.receive(selector, testContextMock, TIMEOUT))
                // Resolving the topic is the next step, but it isn't specified
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cannot invoke", "kafkaEndpointConfiguration", "is null");

        assertThat(fixture.getKafkaMessageFilter())
                .isNotNull()
                .isNotEqualTo(kafkaMessageFilterMock)
                .isNotSameAs(kafkaMessageFilterMock);
    }

    @AfterMethod
    public void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }
}
