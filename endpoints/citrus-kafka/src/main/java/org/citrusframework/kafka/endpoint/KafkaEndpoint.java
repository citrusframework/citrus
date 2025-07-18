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
import org.apache.commons.lang3.RandomStringUtils;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelector;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelectorFactory;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderEquals;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;
import static org.citrusframework.util.StringUtils.hasText;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Kafka message endpoint capable of sending/receiving messages from Kafka message destination.
 * Either uses a Kafka connection factory or a Spring Kafka template to connect with Kafka
 * destinations.
 *
 * @since 2.8
 */
public class KafkaEndpoint extends AbstractEndpoint implements ShutdownPhase {

    /**
     * Cached producer or consumer
     */
    private @Nullable KafkaProducer kafkaProducer;
    private @Nullable KafkaConsumer kafkaConsumer;

    private final ThreadLocal<KafkaConsumer> threadLocalKafkaConsumer;

    public static SimpleKafkaEndpointBuilder builder() {
        return new SimpleKafkaEndpointBuilder();
    }

    /**
     * Default constructor initializing endpoint configuration.
     */
    public KafkaEndpoint() {
        this(new KafkaEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     */
    public KafkaEndpoint(KafkaEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        threadLocalKafkaConsumer = ThreadLocal.withInitial(() -> new KafkaConsumer(getConsumerName(), getEndpointConfiguration()));
    }

    static KafkaEndpoint newKafkaEndpoint(
            @Nullable Boolean randomConsumerGroup,
            @Nullable String server,
            @Nullable Long timeout,
            @Nullable String topic,
            KafkaMessageSelectorFactory.KafkaMessageSelectorFactories customStrategies,
            boolean useThreadSafeConsumer
    ) {
        var kafkaEndpoint = new KafkaEndpoint();

        if (TRUE.equals(randomConsumerGroup)) {
            kafkaEndpoint.getEndpointConfiguration()
                    .setConsumerGroup(KAFKA_PREFIX + RandomStringUtils.insecure().nextAlphabetic(10).toLowerCase());
        }
        if (hasText(server)) {
            kafkaEndpoint.getEndpointConfiguration().setServer(server);
        }
        if (nonNull(timeout)) {
            kafkaEndpoint.getEndpointConfiguration().setTimeout(timeout);
        }
        if (hasText(topic)) {
            kafkaEndpoint.getEndpointConfiguration().setTopic(topic);
        }
        if (!isEmpty(customStrategies)) {
            kafkaEndpoint.getEndpointConfiguration().getKafkaMessageSelectorFactory().setCustomStrategies(customStrategies);
        }

        kafkaEndpoint.getEndpointConfiguration().setUseThreadSafeConsumer(useThreadSafeConsumer);

        return kafkaEndpoint;
    }

    /**
     * @deprecated {@link org.apache.kafka.clients.consumer.KafkaConsumer} is <b>not</b> thread-safe
     * and manual consumer management is error-prone. Use
     * {@link #newKafkaEndpoint(Boolean, String, Long, String, KafkaMessageSelectorFactory.KafkaMessageSelectorFactories, boolean)} instead to obtain properly
     * managed consumer instances. This method will be removed in a future release.
     */
    @Deprecated(forRemoval = true)
    static KafkaEndpoint newKafkaEndpoint(
            @Nullable org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer,
            @Nullable org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer,
            @Nullable Boolean randomConsumerGroup,
            @Nullable String server,
            @Nullable Long timeout,
            @Nullable String topic,
            KafkaMessageSelectorFactory.KafkaMessageSelectorFactories customStrategies,
            boolean useThreadSafeConsumer
    ) {
        var kafkaEndpoint = newKafkaEndpoint(randomConsumerGroup, server, timeout, topic, customStrategies, useThreadSafeConsumer);

        // Make sure these come at the end, so endpoint configuration is already initialized
        if (nonNull(kafkaConsumer)) {
            kafkaEndpoint.createConsumer().setConsumer(kafkaConsumer);
        }
        if (nonNull(kafkaProducer)) {
            kafkaEndpoint.createProducer().setProducer(kafkaProducer);
        }

        return kafkaEndpoint;
    }

    @Override
    public KafkaConsumer createConsumer() {
        if (getEndpointConfiguration().useThreadSafeConsumer()) {
            return threadLocalKafkaConsumer.get();
        } else if (isNull(kafkaConsumer)) {
            kafkaConsumer = new KafkaConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return kafkaConsumer;
    }

    @Override
    public KafkaProducer createProducer() {
        if (kafkaProducer == null) {
            kafkaProducer = new KafkaProducer(getProducerName(), getEndpointConfiguration());
        }

        return kafkaProducer;
    }

    @Override
    public KafkaEndpointConfiguration getEndpointConfiguration() {
        return (KafkaEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void destroy() {
        if (getEndpointConfiguration().useThreadSafeConsumer()) {
            threadLocalKafkaConsumer.get()
                    .stop();
            threadLocalKafkaConsumer.remove();
        } else if (nonNull(kafkaConsumer)) {
            kafkaConsumer.stop();
        }
    }

    public ReceiveMessageAction.ReceiveMessageActionBuilderSupport findKafkaEventHeaderEquals(Duration lookbackWindow, String key, String value) {
        return receive(this)
                .selector(
                        KafkaMessageFilter.kafkaMessageFilter()
                                .eventLookbackWindow(lookbackWindow)
                                .kafkaMessageSelector(kafkaHeaderEquals(key, value))
                                .build()
                )
                .getMessageBuilderSupport();
    }

    public static class SimpleKafkaEndpointBuilder {

        private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer;
        private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer;
        private Boolean randomConsumerGroup;
        private String server;
        private Long timeout;
        private String topic;
        private boolean useThreadSafeConsumer = false;
        private KafkaMessageSelectorFactory.KafkaMessageSelectorFactories customStrategies;

        /**
         * @deprecated {@link org.apache.kafka.clients.consumer.KafkaConsumer} is <b>not</b> thread-safe and manual consumer management is error-prone.
         * This method will be removed in a future release.
         */
        @Deprecated(forRemoval = true)
        public SimpleKafkaEndpointBuilder kafkaConsumer(org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer) {
            this.kafkaConsumer = kafkaConsumer;
            return this;
        }

        @Deprecated(forRemoval = true)
        public SimpleKafkaEndpointBuilder kafkaProducer(org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer) {
            this.kafkaProducer = kafkaProducer;
            return this;
        }

        public SimpleKafkaEndpointBuilder randomConsumerGroup(Boolean randomConsumerGroup) {
            this.randomConsumerGroup = randomConsumerGroup;
            return this;
        }

        public SimpleKafkaEndpointBuilder server(String server) {
            this.server = server;
            return this;
        }

        public SimpleKafkaEndpointBuilder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public SimpleKafkaEndpointBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public SimpleKafkaEndpointBuilder useThreadSafeConsumer() {
            this.useThreadSafeConsumer = true;
            return this;
        }

        public SimpleKafkaEndpointBuilder withCustomStrategy(
                Predicate<Map<String, Object>> selector, Function<Map<String, Object>, KafkaMessageSelector> initializer
        ) {
            this.customStrategies.put(selector, initializer);
            return this;
        }

        public KafkaEndpoint build() {
            return newKafkaEndpoint(kafkaConsumer, kafkaProducer, randomConsumerGroup, server, timeout, topic, customStrategies, useThreadSafeConsumer);
        }
    }
}
