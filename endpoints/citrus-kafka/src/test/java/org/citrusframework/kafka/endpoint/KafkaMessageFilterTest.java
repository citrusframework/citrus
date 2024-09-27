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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelector;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelectorFactory;
import org.citrusframework.message.MessageSelectorBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.kafka.endpoint.KafkaMessageFilter.EVENT_LOOKBACK_WINDOW;
import static org.citrusframework.kafka.endpoint.KafkaMessageFilter.POLL_TIMEOUT;
import static org.citrusframework.kafka.endpoint.KafkaMessageFilter.kafkaMessageFilter;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KafkaMessageFilterTest {

    private KafkaMessageFilter fixture;

    @BeforeMethod
    public void beforeMethodSetup() {
        fixture = KafkaMessageFilter.kafkaMessageFilter().buildFilter();
    }

    @Test
    public void hasKafkaConsumerRecordMatcherFactory() {
        assertThat(fixture.getKafkaMessageSelectorFactory())
                .isNotNull();
    }

    @Test
    public void defaultPollTimeout() {
        assertThat(fixture.getPollTimeout())
                .isEqualTo(Duration.ofMillis(100));
    }

    @Test
    public void kafkaMessageFilter_throwsException_whenNoOffsetConfigured() {
        assertThatThrownBy(() -> KafkaMessageFilter.kafkaMessageFilter(""))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Cannot find Kafka messages without offset limitation");
    }

    @Test
    public void kafkaMessageFilter_extractsOffset() {
        var eventLookbackWindow = Duration.ofSeconds(1);

        var keyValueMap = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .build();
        var messageSelector = MessageSelectorBuilder.fromKeyValueMap(keyValueMap).build();

        var kafkaConsumerRecordMatcherFactoryMock = mock(KafkaMessageSelectorFactory.class);
        var fixture = kafkaMessageFilter(messageSelector, kafkaConsumerRecordMatcherFactoryMock);

        assertThat(fixture.getEventLookbackWindow())
                .isEqualTo(eventLookbackWindow);

        verify(kafkaConsumerRecordMatcherFactoryMock).parseFromSelector(keyValueMap);
    }

    @Test
    public void kafkaMessageFilter_usesDefaultPollTimeout_whenNotGiven() {
        var eventLookbackWindow = Duration.ofSeconds(1);

        var keyValueMap = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .build();
        var messageSelector = MessageSelectorBuilder.fromKeyValueMap(keyValueMap).build();

        var kafkaConsumerRecordMatcherFactoryMock = mock(KafkaMessageSelectorFactory.class);
        var fixture = kafkaMessageFilter(messageSelector, kafkaConsumerRecordMatcherFactoryMock);

        assertThat(fixture.getPollTimeout())
                .isEqualTo(Duration.ofMillis(100));

        verify(kafkaConsumerRecordMatcherFactoryMock).parseFromSelector(keyValueMap);
    }

    @Test
    public void kafkaMessageFilter_extractsPollTimeout() {
        var eventLookbackWindow = Duration.ofSeconds(5);
        var pollTimeout = Duration.ofSeconds(1);

        var keyValueMap = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .pollTimeout(pollTimeout)
                .build();
        var messageSelector = MessageSelectorBuilder.fromKeyValueMap(keyValueMap).build();

        var kafkaConsumerRecordMatcherFactoryMock = mock(KafkaMessageSelectorFactory.class);
        var fixture = kafkaMessageFilter(messageSelector, kafkaConsumerRecordMatcherFactoryMock);

        assertThat(fixture.getPollTimeout())
                .isEqualTo(pollTimeout);

        verify(kafkaConsumerRecordMatcherFactoryMock).parseFromSelector(keyValueMap);
    }

    @Test
    public void kafkaMessageSelector_extractsKafkaConsumerRecordMatcher() {
        var eventLookbackWindow = Duration.ofSeconds(5);
        var pollTimeout = Duration.ofSeconds(1);

        var keyValueMap = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .pollTimeout(pollTimeout)
                .build();
        var messageSelector = MessageSelectorBuilder.fromKeyValueMap(keyValueMap).build();

        var kafkaConsumerRecordMatcherFactoryMock = mock(KafkaMessageSelectorFactory.class);

        var kafkaConsumerRecordMatcher = mock(KafkaMessageSelector.class);
        doReturn(kafkaConsumerRecordMatcher).when(kafkaConsumerRecordMatcherFactoryMock).parseFromSelector(keyValueMap);

        var fixture = kafkaMessageFilter(messageSelector, kafkaConsumerRecordMatcherFactoryMock);

        assertThat(fixture.getKafkaMessageSelector())
                .isEqualTo(kafkaConsumerRecordMatcher);

        verify(kafkaConsumerRecordMatcherFactoryMock).parseFromSelector(keyValueMap);
    }

    @Test
    public void build_ignoresNullValues() {
        var selector = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(null)
                .kafkaMessageSelector(null)
                .pollTimeout(null)
                .build();

        assertThat(selector)
                .isEmpty();
    }

    @Test
    public void build_exportsAllNonNullValues() {
        var eventLookbackWindow = Duration.ofSeconds(5);
        var kafkaConsumerRecordMatcherMock = mock(KafkaMessageSelector.class);
        var pollTimeout = Duration.ofSeconds(1);

        var kafkaConsumerRecordMatcherSelector = singletonMap("foo", "bar");
        doReturn(kafkaConsumerRecordMatcherSelector).when(kafkaConsumerRecordMatcherMock).asSelector();

        var selector = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .kafkaMessageSelector(kafkaConsumerRecordMatcherMock)
                .pollTimeout(pollTimeout)
                .build();

        assertThat(selector)
                .hasEntrySatisfying(EVENT_LOOKBACK_WINDOW, r -> assertThat(r).isEqualTo(eventLookbackWindow.toString()))
                .hasEntrySatisfying(POLL_TIMEOUT, r -> assertThat(r).isEqualTo(pollTimeout.toString()))
                .containsAllEntriesOf(selector);

        verify(kafkaConsumerRecordMatcherMock).asSelector();
    }

    @Test
    public void sanitize_throwsNothing_whenSelectorValid() {
        var eventLookbackWindow = Duration.ofSeconds(1);
        var kafkaConsumerRecordMatcherMock = mock(KafkaMessageSelector.class);

        // Explicitly configure selector, but no lookback window, to minimize side effects
        fixture = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .kafkaMessageSelector(kafkaConsumerRecordMatcherMock)
                .buildFilter();

        assertThatNoException().isThrownBy(() -> fixture.sanitize());
    }

    @Test
    public void sanitize_throwsException_whenNoOffsetConfigured() {
        var kafkaConsumerRecordMatcherMock = mock(KafkaMessageSelector.class);

        // Explicitly configure selector, but no lookback window, to minimize side effects
        fixture = KafkaMessageFilter.kafkaMessageFilter()
                .kafkaMessageSelector(kafkaConsumerRecordMatcherMock)
                .buildFilter();

        assertThatThrownBy(() -> fixture.sanitize())
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Cannot find Kafka messages without offset limitation");
    }

    @Test
    public void sanitize_throwsException_whenNoPollTimeoutConfigured() {
        var eventLookbackWindow = Duration.ofSeconds(1);
        var kafkaConsumerRecordMatcherMock = mock(KafkaMessageSelector.class);

        // Explicitly configure selector, but nullish poll timeout, to minimize side effects
        fixture = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .kafkaMessageSelector(kafkaConsumerRecordMatcherMock)
                .pollTimeout(null)
                .buildFilter();

        assertThatThrownBy(() -> fixture.sanitize())
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("No poll timeout defined when looking for Kafka messages");
    }

    @Test
    public void sanitize_throwsException_whenNoMatcherConfigured() {
        var eventLookbackWindow = Duration.ofSeconds(1);

        // Explicitly configure selector, but no record matcher, to minimize side effects
        fixture = KafkaMessageFilter.kafkaMessageFilter()
                .eventLookbackWindow(eventLookbackWindow)
                .buildFilter();

        assertThatThrownBy(() -> fixture.sanitize())
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("No matcher specified when looking for Kafka messages");
    }
}
