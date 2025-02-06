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

import jakarta.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelector;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.citrusframework.kafka.endpoint.KafkaMessageConsumerUtils.parseConsumerRecordsToMessage;
import static org.citrusframework.kafka.endpoint.KafkaMessageConsumerUtils.resolveTopic;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A specialized Kafka message consumer that filters messages based on specified criteria.
 * <p>
 * This consumer provides functionality to receive Kafka messages that match given selectors or matchers within a
 * specified time window.
 *
 * @see KafkaMessageSelector
 */
class KafkaMessageFilteringConsumer extends AbstractSelectiveMessageConsumer {

    private static final Logger logger = getLogger(KafkaMessageFilteringConsumer.class);

    private final org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

    private KafkaMessageFilter kafkaMessageFilter;

    public static KafkaMessageFilteringConsumerBuilder builder() {
        return new KafkaMessageFilteringConsumerBuilder();
    }

    private KafkaMessageFilteringConsumer(
            KafkaEndpointConfiguration endpointConfiguration,
            org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer,
            @Nullable Duration eventLookbackWindow,
            @Nullable Duration pollTimeout,
            @Nullable KafkaMessageSelector kafkaMessageSelector
    ) {
        super(KafkaMessageSingleConsumer.class.getSimpleName(), endpointConfiguration);

        this.consumer = consumer;

        if (nonNull(eventLookbackWindow) || nonNull(kafkaMessageSelector) || nonNull(pollTimeout)) {
            kafkaMessageFilter = KafkaMessageFilter.kafkaMessageFilter()
                    .eventLookbackWindow(eventLookbackWindow)
                    .kafkaMessageSelector(kafkaMessageSelector)
                    .pollTimeout(pollTimeout)
                    .buildFilter();
        }
    }

    public KafkaConsumer<Object, Object> getConsumer() {
        return consumer;
    }

    public KafkaMessageFilter getKafkaMessageFilter() {
        return kafkaMessageFilter;
    }

    @Override
    public Message receive(String selector, TestContext testContext, long timeout) {
        if (isEmpty(selector) && (isNull(kafkaMessageFilter))) {
            throw new CitrusRuntimeException("Cannot invoke filtering kafka message consumer without selectors");
        } else if (hasText(selector)) {
            kafkaMessageFilter = KafkaMessageFilter.kafkaMessageFilter(selector);
        }

        kafkaMessageFilter.sanitize();

        String topic = resolveTopic(getEndpointConfiguration(), testContext);

        logger.debug("Receiving Kafka message on topic '{}' using selector: {}", topic, kafkaMessageFilter);

        if (!consumer.subscription().isEmpty()) {
            logger.warn("Cancelling active subscriptions of consumer before looking for Kafka events, because subscription to topics, partitions and pattern are mutually exclusive");
            consumer.unsubscribe();
        }

        Duration messageTimeout = Duration.ofMillis(timeout);
        if (kafkaMessageFilter.getPollTimeout().compareTo(messageTimeout) > 0) {
            logger.warn(
                    "Truncating poll timeout to maximum message timeout ({} ms) - having one single poll exceeding the total timeout would prevent proper timeout handling",
                    messageTimeout.toMillis());
            kafkaMessageFilter.setPollTimeout(messageTimeout);
        }

        var consumerRecords = findMatchingMessageInTopicWithTimeout(topic, timeout);

        if (consumerRecords.isEmpty()) {
            throw new CitrusRuntimeException("Failed to resolve Kafka message using selector: " + selector);
        }

        var received = parseConsumerRecordsToMessage(
                consumerRecords,
                getEndpointConfiguration(),
                testContext);

        if (logger.isDebugEnabled()) {
            logger.info("Received Kafka message on topic '{}': {}", topic, received);
        } else {
            logger.info("Received Kafka message on topic '{}'", topic);
        }

        return received;
    }

    private List<ConsumerRecord<Object, Object>> findMatchingMessageInTopicWithTimeout(String topic, long timeout) {
        logger.trace("Applied timeout is {} ms", timeout);

        var executorService = newSingleThreadExecutor();
        final Future<List<ConsumerRecord<Object, Object>>> handler = executorService.submit(() -> findMessagesSatisfyingMatcher(topic));

        try {
            return handler.get(timeout, MILLISECONDS);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throw new CitrusRuntimeException("Thread was interrupted while waiting for Kafka message", e);
        } catch (ExecutionException e) {
            throw new CitrusRuntimeException(format("Failed to receive message on Kafka topic '%s'", topic), e);
        } catch (TimeoutException e) {
            logger.error("Failed to receive message on  Kafka topic '{}': {}", topic, getRootCause(e).getMessage());

            handler.cancel(true);

            throw new MessageTimeoutException(timeout, topic, e);
        } finally {
            shutdownExecutorAwaitingCurrentPoll(executorService);
        }
    }

    private void shutdownExecutorAwaitingCurrentPoll(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(kafkaMessageFilter.getPollTimeout().toMillis(), MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(kafkaMessageFilter.getPollTimeout().toMillis(), MILLISECONDS)) {
                    logger.error("Executor did not terminate, check for memory leaks!");
                }
            }

            logger.debug("Executor successfully shut down, unsubscribing consumer");

            consumer.unsubscribe();
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            currentThread().interrupt();
        }
    }

    @Override
    protected KafkaEndpointConfiguration getEndpointConfiguration() {
        return (KafkaEndpointConfiguration) super.getEndpointConfiguration();
    }

    private List<ConsumerRecord<Object, Object>> findMessagesSatisfyingMatcher(String topic) {
        List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                .toList();

        consumer.assign(partitions);

        offsetConsumerOnTopicByLookbackWindow(partitions);

        var endTime = Instant.now();
        var startTime = endTime.minus(kafkaMessageFilter.getEventLookbackWindow());

        var matchingConsumerRecords = new ArrayList<ConsumerRecord<Object, Object>>();

        while (true) {
            var consumerRecords = consumer.poll(kafkaMessageFilter.getPollTimeout());
            if (consumerRecords.isEmpty()) {
                break;  // No more records to process
            }

            for (ConsumerRecord<Object, Object> consumerRecord : consumerRecords) {
                if (isConsumerRecordNewerThanEndTime(consumerRecord, endTime)) {
                    return matchingConsumerRecords;
                } else if (!isConsumerRecordOlderThanStartTime(consumerRecord, startTime)
                        && kafkaMessageFilter.getKafkaMessageSelector().matches(consumerRecord)) {
                    matchingConsumerRecords.add(consumerRecord);
                }
            }
        }

        return matchingConsumerRecords;
    }

    private void offsetConsumerOnTopicByLookbackWindow(List<TopicPartition> partitions) {
        Map<TopicPartition, Long> partitionsWithTimestamps = partitions.stream().collect(toMap(
                Function.identity(),
                partition -> now().minusMillis(kafkaMessageFilter.getEventLookbackWindow().toMillis()).toEpochMilli()
        ));

        var newOffsets = consumer.offsetsForTimes(partitionsWithTimestamps);

        logger.trace("Applying new offsets: {}", newOffsets);

        newOffsets.forEach((partition, partitionOffset) -> {
            if (nonNull(partitionOffset)) {
                consumer.seek(partition, partitionOffset.offset());
            } else {
                consumer.seekToEnd(singletonList(partition));
            }
        });
    }

    private static boolean isConsumerRecordNewerThanEndTime(
            ConsumerRecord<Object, Object> consumerRecord,
            Instant endTime
    ) {
        return consumerRecord.timestamp() > endTime.toEpochMilli();
    }

    private static boolean isConsumerRecordOlderThanStartTime(
            ConsumerRecord<Object, Object> consumerRecord,
            Instant startTime
    ) {
        return consumerRecord.timestamp() < startTime.toEpochMilli();
    }

    public static class KafkaMessageFilteringConsumerBuilder {

        private KafkaEndpointConfiguration endpointConfiguration;
        private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;
        private Duration eventLookbackWindow;
        private Duration pollTimeout;
        private KafkaMessageSelector kafkaMessageSelector;

        public KafkaMessageFilteringConsumerBuilder endpointConfiguration(KafkaEndpointConfiguration endpointConfiguration) {
            this.endpointConfiguration = endpointConfiguration;
            return this;
        }

        public KafkaMessageFilteringConsumerBuilder consumer(KafkaConsumer<Object, Object> consumer) {
            this.consumer = consumer;
            return this;
        }

        public KafkaMessageFilteringConsumerBuilder eventLookbackWindow(Duration eventLookbackWindow) {
            this.eventLookbackWindow = eventLookbackWindow;
            return this;
        }

        public KafkaMessageFilteringConsumerBuilder pollTimeout(Duration pollTimeout) {
            this.pollTimeout = pollTimeout;
            return this;
        }

        public KafkaMessageFilteringConsumerBuilder kafkaMessageSelector(KafkaMessageSelector kafkaMessageSelector) {
            this.kafkaMessageSelector = kafkaMessageSelector;
            return this;
        }

        public KafkaMessageFilteringConsumer build() {
            return new KafkaMessageFilteringConsumer(endpointConfiguration, consumer, eventLookbackWindow, pollTimeout, kafkaMessageSelector);
        }
    }
}
