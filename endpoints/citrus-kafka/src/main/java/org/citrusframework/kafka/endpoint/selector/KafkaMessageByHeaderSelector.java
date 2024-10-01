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
import org.apache.kafka.common.header.Header;
import org.citrusframework.exceptions.CitrusRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.CONTAINS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.EQUALS;
import static org.citrusframework.util.StringUtils.isEmpty;

/**
 * A matcher for Kafka {@link ConsumerRecord}, based on header key-value pairs. This class implements
 * the {@link KafkaMessageSelector} interface and provides flexible matching capabilities for Kafka message
 * headers.
 *
 * <p>The matching mechanism works as follows:</p>
 * <ul>
 *   <li>If the <code>key</code> is <code>null</code>, it matches against all headers in the record.</li>
 *   <li>If the <code>key</code> is specified, it matches only headers with that exact key.</li>
 *   <li>
 *       If the <code>value</code> is <code>null</code>, it matches any value for the specified key (or any header if
 *       key is {@code null}).
 *   </li>
 *   <li>
 *       If both <code>key</code> and <code>value</code> are specified, it matches headers with the exact key and
 *       applies the specified matching mechanism to the value.
 *   </li>
 * </ul>
 *
 * <p>The 'matchingMechanism' applies only to the <code>value</code> and can be one of the following:</p>
 * <ul>
 *   <li><code>EQUALS</code>: The header value must exactly match the specified value.</li>
 *   <li><code>CONTAINS</code>: The header value must contain the specified value as a substring.</li>
 *   <li><code>STARTS_WITH</code>: The header value must start with the specified value.</li>
 *   <li><code>ENDS_WITH</code>: The header value must end with the specified value.</li>
 * </ul>
 *
 * <p>If no matching mechanism is specified, <code>EQUALS</code> is used by default.</p>
 *
 * @see ValueMatchingStrategy
 */
public class KafkaMessageByHeaderSelector implements KafkaMessageSelector {

    /**
     * @see KafkaMessageByHeaderSelector#key
     */
    static final String HEADER_FILTER_KEY = "header-filter-key";

    /**
     * @see KafkaMessageByHeaderSelector#value
     */
    static final String HEADER_FILTER_VALUE = "header-filter-value";

    /**
     * @see KafkaMessageByHeaderSelector#valueMatchingStrategy
     */
    static final String HEADER_FILTER_COMPARATOR = "header-filter-comparator";

    /**
     * Key-filter being applied to Kafka messages. Matches exact if specified, all keys if {@code null}.
     */
    private @Nullable String key;

    /**
     * Value-filter being applied to Kafka messages. Matches all values if {@code null}. Otherwise, matches as specified
     * in the {@link KafkaMessageByHeaderSelector#valueMatchingStrategy}.
     */
    private @Nullable String value;

    /**
     * Specifies how the {@link KafkaMessageByHeaderSelector#value} should be matched.
     */
    private @Nullable ValueMatchingStrategy valueMatchingStrategy;

    public static KafkaMessageByHeaderSelectorBuilder builder() {
        return new KafkaMessageByHeaderSelectorBuilder();
    }

    /**
     * Creates a {@link KafkaMessageByHeaderSelector} that checks if a header with the specified key contains the given
     * value.
     * <p>
     * This method is a convenient shortcut for creating a matcher with the {@link ValueMatchingStrategy#CONTAINS} matching
     * mechanism.
     *
     * @param key   The header key to match. If {@code null}, all header keys will be checked.
     * @param value The value to search for within the header value. If {@code null}, any value will match.
     * @return A {@link KafkaMessageByHeaderSelector} configured to use the {@link ValueMatchingStrategy#CONTAINS} matching mechanism.
     */
    public static KafkaMessageByHeaderSelector kafkaHeaderContains(String key, String value) {
        return keyValueBuilder(key, value)
                .valueMatchingStrategy(CONTAINS)
                .build();
    }

    /**
     * Creates a {@link KafkaMessageByHeaderSelector} that checks if a header with the specified key exactly equals the
     * given value.
     * <p>
     * This method is a convenient shortcut for creating a matcher with the {@link ValueMatchingStrategy#EQUALS} matching
     * mechanism.
     *
     * @param key   The header key to match. If {@code null}, all header keys will be checked.
     * @param value The exact value to match in the header. If {@code null}, any value will match.
     * @return A {@link KafkaMessageByHeaderSelector} configured to use the {@link ValueMatchingStrategy#EQUALS} matching mechanism.
     */
    public static KafkaMessageByHeaderSelector kafkaHeaderEquals(String key, String value) {
        return keyValueBuilder(key, value)
                .valueMatchingStrategy(EQUALS)
                .build();
    }

    static <T> KafkaMessageByHeaderSelector fromSelector(Map<String, T> messageSelectors) {
        var keyFilter = Optional.ofNullable(messageSelectors.get(HEADER_FILTER_KEY)).map(Object::toString).orElse("");
        var valueFilter = Optional.ofNullable(messageSelectors.get(HEADER_FILTER_VALUE)).map(Objects::toString).orElse(null);
        var comparator = Optional.ofNullable(messageSelectors.get(HEADER_FILTER_COMPARATOR)).map(Object::toString).orElse(EQUALS.toString());

        if (isEmpty(keyFilter) && isEmpty(valueFilter)) {
            throw new CitrusRuntimeException("No matcher specified when looking for Kafka messages");
        }

        return KafkaMessageByHeaderSelector.builder()
                .key(keyFilter)
                .value(valueFilter)
                .valueMatchingStrategy(ValueMatchingStrategy.valueOf(comparator.toUpperCase()))
                .build();
    }

    private static KafkaMessageByHeaderSelectorBuilder keyValueBuilder(String key, String value) {
        return KafkaMessageByHeaderSelector.builder()
                .key(key)
                .value(value);
    }

    private KafkaMessageByHeaderSelector(@Nullable String key, @Nullable String value, @Nullable ValueMatchingStrategy valueMatchingStrategy) {
        this.key = key;
        this.value = value;
        this.valueMatchingStrategy = valueMatchingStrategy;
    }

    @Nullable
    String getKey() {
        return key;
    }

    @Nullable
    String getValue() {
        return value;
    }

    @Nullable
    ValueMatchingStrategy getValueMatchingStrategy() {
        return valueMatchingStrategy;
    }

    @Override
    public boolean matches(ConsumerRecord<Object, Object> consumerRecord) {
        var headers = consumerRecord.headers();

        for (Header header : headers) {
            if ((isEmpty(key) || header.key().equals(key))
                    && matchesValue(header.value())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> Map<String, T> asSelector() {
        Map<String, T> selector = new HashMap<>();

        if (nonNull(key)) {
            selector.put(HEADER_FILTER_KEY, (T) key);
        }
        if (nonNull(value)) {
            selector.put(HEADER_FILTER_VALUE, (T) value);
        }

        selector.put(HEADER_FILTER_COMPARATOR, (T) Optional.ofNullable(valueMatchingStrategy).orElse(EQUALS).toString());

        return selector;
    }

    private boolean matchesValue(byte[] headerValue) {
        if (isNull(value)) {
            return true; // If value is not defined, consider it a match
        }

        var headerValueString = new String(headerValue, UTF_8);

        if (isNull(valueMatchingStrategy)) {
            return headerValueString.equals(value);
        }

        return switch (valueMatchingStrategy) {
            case EQUALS -> headerValueString.equals(value);
            case CONTAINS -> headerValueString.contains(value);
            case STARTS_WITH -> headerValueString.startsWith(value);
            case ENDS_WITH -> headerValueString.endsWith(value);
        };
    }

    public enum ValueMatchingStrategy {
        EQUALS,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }

    @Override
    public String toString() {
        return "KafkaMessageByHeaderSelector{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", valueMatchingStrategy=" + valueMatchingStrategy +
                '}';
    }

    public static class KafkaMessageByHeaderSelectorBuilder {

        private String key;
        private String value;
        private KafkaMessageByHeaderSelector.ValueMatchingStrategy valueMatchingStrategy;

        public KafkaMessageByHeaderSelectorBuilder key(String key) {
            this.key = key;
            return this;
        }

        public KafkaMessageByHeaderSelectorBuilder value(String value) {
            this.value = value;
            return this;
        }

        public KafkaMessageByHeaderSelectorBuilder valueMatchingStrategy(KafkaMessageByHeaderSelector.ValueMatchingStrategy valueMatchingStrategy) {
            this.valueMatchingStrategy = valueMatchingStrategy;
            return this;
        }

        public KafkaMessageByHeaderSelector build() {
            return new KafkaMessageByHeaderSelector(key, value, valueMatchingStrategy);
        }
    }
}
