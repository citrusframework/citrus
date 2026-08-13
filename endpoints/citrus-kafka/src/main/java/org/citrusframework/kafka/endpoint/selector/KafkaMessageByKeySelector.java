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

package org.citrusframework.kafka.endpoint.selector;

import jakarta.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.CONTAINS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.EQUALS;
import static org.citrusframework.util.StringUtils.isEmpty;

/**
 * A matcher for Kafka {@link ConsumerRecord}s based on the record {@code key()}. This class implements
 * the {@link KafkaMessageSelector} interface and provides flexible matching for Kafka message keys.
 *
 * <p>The matching mechanism works as follows:</p>
 * <ul>
 *   <li>If the configured key is {@code null}, any non-null record key matches.</li>
 *   <li>If both the configured key and the record key are specified, the configured
 *       {@link ValueMatchingStrategy} is applied to the record key's string representation.</li>
 * </ul>
 *
 * <p>The matching strategy can be one of the following:</p>
 * <ul>
 *   <li><code>EQUALS</code>: The record key must exactly match the specified value.</li>
 *   <li><code>CONTAINS</code>: The record key must contain the specified value as a substring.</li>
 *   <li><code>STARTS_WITH</code>: The record key must start with the specified value.</li>
 *   <li><code>ENDS_WITH</code>: The record key must end with the specified value.</li>
 * </ul>
 *
 * <p>If no matching strategy is specified, <code>EQUALS</code> is used by default.</p>
 *
 * @see ValueMatchingStrategy
 */
public class KafkaMessageByKeySelector implements KafkaMessageSelector<String> {

    /**
     * @see KafkaMessageByKeySelector#key
     */
    static final String KEY_FILTER_VALUE = "key-filter-value";

    /**
     * @see KafkaMessageByKeySelector#valueMatchingStrategy
     */
    static final String KEY_FILTER_COMPARATOR = "key-filter-comparator";

    /**
     * Key-filter being applied to Kafka records. Matches all non-null keys if {@code null}.
     * Otherwise matches as specified in the {@link KafkaMessageByKeySelector#valueMatchingStrategy}.
     */
    private final @Nullable String key;

    /**
     * Specifies how the {@link KafkaMessageByKeySelector#key} should be matched.
     */
    private final @Nullable ValueMatchingStrategy valueMatchingStrategy;

    public static KafkaMessageByKeySelectorBuilder builder() {
        return new KafkaMessageByKeySelectorBuilder();
    }

    /**
     * Creates a {@link KafkaMessageByKeySelector} that checks if the record key contains the given value.
     *
     * @param key The value to search for within the record key.
     * @return A selector configured with {@link ValueMatchingStrategy#CONTAINS}.
     */
    public static KafkaMessageByKeySelector kafkaKeyContains(String key) {
        return builder()
                .key(key)
                .valueMatchingStrategy(CONTAINS)
                .build();
    }

    /**
     * Creates a {@link KafkaMessageByKeySelector} that checks if the record key exactly equals the given value.
     *
     * @param key The exact record key to match.
     * @return A selector configured with {@link ValueMatchingStrategy#EQUALS}.
     */
    public static KafkaMessageByKeySelector kafkaKeyEquals(String key) {
        return builder()
                .key(key)
                .valueMatchingStrategy(EQUALS)
                .build();
    }

    static <T> KafkaMessageByKeySelector fromSelector(Map<String, T> messageSelectors) {
        var keyFilter = Optional.ofNullable(messageSelectors.get(KEY_FILTER_VALUE)).map(Objects::toString).orElse(null);
        var comparator = Optional.ofNullable(messageSelectors.get(KEY_FILTER_COMPARATOR)).map(Object::toString).orElse(EQUALS.toString());

        if (isEmpty(keyFilter)) {
            throw new CitrusRuntimeException("No matcher specified when looking for Kafka messages");
        }

        return KafkaMessageByKeySelector.builder()
                .key(keyFilter)
                .valueMatchingStrategy(ValueMatchingStrategy.valueOf(comparator.toUpperCase()))
                .build();
    }

    private KafkaMessageByKeySelector(@Nullable String key, @Nullable ValueMatchingStrategy valueMatchingStrategy) {
        this.key = key;
        this.valueMatchingStrategy = valueMatchingStrategy;
    }

    @Nullable
    String getKey() {
        return key;
    }

    @Nullable
    ValueMatchingStrategy getValueMatchingStrategy() {
        return valueMatchingStrategy;
    }

    @Override
    public boolean matches(ConsumerRecord<Object, Object> consumerRecord) {
        var recordKey = consumerRecord.key();
        if (isNull(recordKey)) {
            return false;
        }

        if (isNull(key)) {
            return true;
        }

        var recordKeyString = String.valueOf(recordKey);
        if (isNull(valueMatchingStrategy)) {
            return recordKeyString.equals(key);
        }

        return switch (valueMatchingStrategy) {
            case EQUALS -> recordKeyString.equals(key);
            case CONTAINS -> recordKeyString.contains(key);
            case STARTS_WITH -> recordKeyString.startsWith(key);
            case ENDS_WITH -> recordKeyString.endsWith(key);
        };
    }

    @Override
    public Map<String, String> asSelector() {
        Map<String, String> selector = new HashMap<>();

        if (nonNull(key)) {
            selector.put(KEY_FILTER_VALUE, key);
        }

        selector.put(KEY_FILTER_COMPARATOR, Optional.ofNullable(valueMatchingStrategy).orElse(EQUALS).toString());

        return selector;
    }

    @Override
    public String toString() {
        return "KafkaMessageByKeySelector{" +
                "key='" + key + '\'' +
                ", valueMatchingStrategy=" + valueMatchingStrategy +
                '}';
    }

    public static class KafkaMessageByKeySelectorBuilder {

        private String key;
        private ValueMatchingStrategy valueMatchingStrategy;

        public KafkaMessageByKeySelectorBuilder key(String key) {
            this.key = key;
            return this;
        }

        public KafkaMessageByKeySelectorBuilder valueMatchingStrategy(ValueMatchingStrategy valueMatchingStrategy) {
            this.valueMatchingStrategy = valueMatchingStrategy;
            return this;
        }

        public KafkaMessageByKeySelector build() {
            return new KafkaMessageByKeySelector(key, valueMatchingStrategy);
        }
    }
}
