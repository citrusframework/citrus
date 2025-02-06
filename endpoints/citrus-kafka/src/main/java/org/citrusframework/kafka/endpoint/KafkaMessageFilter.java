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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class KafkaMessageFilter {

    /**
     * @see KafkaMessageFilter#eventLookbackWindow
     */
    static final String EVENT_LOOKBACK_WINDOW = "event-lookback-window";

    /**
     * @see KafkaMessageFilter#pollTimeout
     */
    static final String POLL_TIMEOUT = "poll-timeout";

    private static final String EVENT_LOOKBACK_WINDOW_EXCEPTION = "Cannot find Kafka messages without offset limitation";

    private final KafkaMessageSelectorFactory kafkaMessageSelectorFactory;

    /**
     * See {@link KafkaMessageFilterBuilder#eventLookbackWindow(Duration)} for documentation.
     */
    private Duration eventLookbackWindow;

    /**
     * See {@link KafkaMessageFilterBuilder#pollTimeout(Duration)} for documentation.
     */
    private Duration pollTimeout;

    /**
     * See {@link KafkaMessageFilterBuilder#kafkaMessageSelector(KafkaMessageSelector)} for documentation.
     */
    private KafkaMessageSelector kafkaMessageSelector;

    static KafkaMessageFilterBuilder builder() {
        return new KafkaMessageFilterBuilder();
    }

    public static KafkaMessageFilterBuilder kafkaMessageFilter() {
        return KafkaMessageFilter.builder()
                .kafkaMessageSelectorFactory(new KafkaMessageSelectorFactory());
    }

    static KafkaMessageFilter kafkaMessageFilter(String selector) {
        return kafkaMessageFilter(selector, new KafkaMessageSelectorFactory());
    }

    static KafkaMessageFilter kafkaMessageFilter(String selector, KafkaMessageSelectorFactory kafkaMessageSelectorFactory) {
        return new KafkaMessageFilterBuilder().
                kafkaMessageSelectorFactory(kafkaMessageSelectorFactory)
                .buildFilter()
                .fromSelectorString(selector);
    }

    private KafkaMessageFilter(KafkaMessageSelectorFactory kafkaMessageSelectorFactory, Duration eventLookbackWindow, Duration pollTimeout, KafkaMessageSelector kafkaMessageSelector) {
        this.kafkaMessageSelectorFactory = kafkaMessageSelectorFactory;
        this.eventLookbackWindow = eventLookbackWindow;
        this.pollTimeout = pollTimeout;
        this.kafkaMessageSelector = kafkaMessageSelector;
    }

    KafkaMessageSelectorFactory getKafkaMessageSelectorFactory() {
        return kafkaMessageSelectorFactory;
    }

    Duration getEventLookbackWindow() {
        return eventLookbackWindow;
    }

    Duration getPollTimeout() {
        return pollTimeout;
    }

    void setPollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    KafkaMessageSelector getKafkaMessageSelector() {
        return kafkaMessageSelector;
    }

    private KafkaMessageFilter fromSelectorString(String selector) {
        var messageSelectors = MessageSelectorBuilder.withString(selector).toKeyValueMap();

        eventLookbackWindow = Duration.parse(
                Optional.ofNullable(messageSelectors.get(EVENT_LOOKBACK_WINDOW))
                        .orElseThrow(() -> new CitrusRuntimeException(EVENT_LOOKBACK_WINDOW_EXCEPTION))
        );

        pollTimeout = Duration.parse(
                Optional.ofNullable(messageSelectors.get(POLL_TIMEOUT))
                        .orElse("PT0.100S") // 100 ms
        );

        kafkaMessageSelector = kafkaMessageSelectorFactory.parseFromSelector(messageSelectors);

        return this;
    }

    @SuppressWarnings({"unchecked"})
    <T> Map<String, T> asSelector() {
        Map<String, T> selector = new HashMap<>();

        if (nonNull(eventLookbackWindow)) {
            selector.put(EVENT_LOOKBACK_WINDOW, (T) eventLookbackWindow.toString());
        }
        if (nonNull(pollTimeout)) {
            selector.put(POLL_TIMEOUT, (T) pollTimeout.toString());
        }
        if (nonNull(kafkaMessageSelector)) {
            selector.putAll(kafkaMessageSelector.asSelector());
        }

        return selector;
    }

    void sanitize() {
        if (isNull(eventLookbackWindow)) {
            throw new CitrusRuntimeException(EVENT_LOOKBACK_WINDOW_EXCEPTION);
        } else if (isNull(pollTimeout)) {
            throw new CitrusRuntimeException("No poll timeout defined when looking for Kafka messages");
        } else if (isNull(kafkaMessageSelector)) {
            throw new CitrusRuntimeException("No matcher specified when looking for Kafka messages");
        }
    }

    @Override
    public String toString() {
        return "KafkaMessageFilter{" +
                "kafkaMessageSelectorFactory=" + kafkaMessageSelectorFactory +
                ", eventLookbackWindow=" + eventLookbackWindow +
                ", pollTimeout=" + pollTimeout +
                ", kafkaMessageSelector=" + kafkaMessageSelector +
                '}';
    }

    public static class KafkaMessageFilterBuilder {

        private KafkaMessageSelectorFactory kafkaMessageSelectorFactory;
        private Duration eventLookbackWindow;
        private Duration pollTimeout = Duration.ofMillis(100);
        private KafkaMessageSelector kafkaMessageSelector;

        KafkaMessageFilterBuilder kafkaMessageSelectorFactory(KafkaMessageSelectorFactory kafkaMessageSelectorFactory) {
            this.kafkaMessageSelectorFactory = kafkaMessageSelectorFactory;
            return this;
        }

        /**
         * Defines the time window to look back for events in the Kafka topic. This duration is subtracted from the current
         * time to determine the starting point for consuming messages.
         * <p>
         * Because finding messages in a Kafka topic is relatively complicated, message consumption starts at an
         * offset {@code Ox = OT-n}. Where {@code T} is the current timestamp and {@code n} is the maximum timespan in which
         * the wanted event is expected to have been published.
         * <p>
         * The timespan {@code n} should also be as small as possible, because the amount of consumed messages is
         * potentially very high.
         */
        public KafkaMessageFilterBuilder eventLookbackWindow(Duration eventLookbackWindow) {
            this.eventLookbackWindow = eventLookbackWindow;
            return this;
        }

        /**
         * The timeout duration for each poll operation when consuming messages from Kafka. This value determines how long
         * the consumer will wait for new records in each poll cycle.
         */
        public KafkaMessageFilterBuilder pollTimeout(Duration pollTimeout) {
            this.pollTimeout = pollTimeout;
            return this;
        }

        /**
         * A custom matcher implementing {@link KafkaMessageSelector}. This matcher is used to determine whether a
         * consumed record meets specific criteria defined by the user.
         */
        public KafkaMessageFilterBuilder kafkaMessageSelector(KafkaMessageSelector kafkaMessageSelector) {
            this.kafkaMessageSelector = kafkaMessageSelector;
            return this;
        }

        public <T> Map<String, T> build() {
            return buildFilter()
                    .asSelector();
        }

        public KafkaMessageFilter buildFilter() {
            return new KafkaMessageFilter(kafkaMessageSelectorFactory, eventLookbackWindow, pollTimeout, kafkaMessageSelector);
        }
    }
}
