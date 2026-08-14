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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.CONTAINS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.EQUALS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.STARTS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByKeySelector.KEY_FILTER_COMPARATOR;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByKeySelector.KEY_FILTER_VALUE;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByKeySelector.kafkaKeyContains;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByKeySelector.kafkaKeyEquals;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.MESSAGE_KEY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KafkaMessageByKeySelectorTest {

    @Test
    public void builder() {
        var key = "order-42";

        var fixture = KafkaMessageByKeySelector.builder()
                .key(key)
                .valueMatchingStrategy(EQUALS)
                .build();

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(EQUALS)
                );
    }

    @Test
    public void kafkaKeyContains_returns_CONTAINS_matcher() {
        var key = "order";

        var fixture = kafkaKeyContains(key);

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(CONTAINS)
                );
    }

    @Test
    public void kafkaKeyEquals_returns_EQUALS_matcher() {
        var key = "order-42";

        var fixture = kafkaKeyEquals(key);

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(EQUALS)
                );
    }

    @Test
    public void fromSelector_throwsException_whenKeyValueMissing() {
        var messageSelectors = new HashMap<String, Object>();

        assertThatThrownBy(() -> KafkaMessageByKeySelector.fromSelector(messageSelectors))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("No matcher specified when looking for Kafka messages");
    }

    @Test
    public void fromSelector_createsKafkaMessageByKeySelector() {
        var key = "order-42";

        var messageSelectors = Map.of(
                KEY_FILTER_VALUE, key,
                KEY_FILTER_COMPARATOR, STARTS_WITH.toString()
        );

        var result = KafkaMessageByKeySelector.fromSelector(messageSelectors);

        assertThat(result)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(STARTS_WITH)
                );
    }

    @Test
    public void fromSelector_createsSelectorFromMessageKeyHeader() {
        var key = "order-42";

        var result = KafkaMessageByKeySelector.fromSelector(Map.of(MESSAGE_KEY, key));

        assertThat(result)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(EQUALS)
                );
    }

    @Test
    public void matches_exactRecordKey() {
        var matcher = KafkaMessageByKeySelector.builder()
                .key("order-42")
                .build();

        assertTrue(matcher.matches(recordWithKey("order-42")));
        assertFalse(matcher.matches(recordWithKey("order-43")));
    }

    @Test
    public void matches_doesNotMatchNullRecordKey() {
        var matcher = KafkaMessageByKeySelector.builder()
                .key("order-42")
                .build();

        assertFalse(matcher.matches(recordWithKey(null)));
    }

    @Test
    public void matches_nonStringRecordKeyViaToString() {
        var matcher = KafkaMessageByKeySelector.builder()
                .key("42")
                .build();

        assertTrue(matcher.matches(recordWithKey(42)));
    }

    @DataProvider
    public static Object[][] matchingMechanism() {
        return stream(ValueMatchingStrategy.values())
                .map(m -> new Object[]{m})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "matchingMechanism")
    public void matches_usingSpecifiedMechanism(ValueMatchingStrategy mechanism) {
        String recordKey = "testValue";
        String matchValue = switch (mechanism) {
            case EQUALS -> "testValue";
            case CONTAINS -> "stVal";
            case STARTS_WITH -> "test";
            case ENDS_WITH -> "Value";
        };

        var matcher = KafkaMessageByKeySelector.builder()
                .key(matchValue)
                .valueMatchingStrategy(mechanism)
                .build();

        assertTrue(matcher.matches(recordWithKey(recordKey)));
    }

    @Test
    public void matches_doesNotMatchWhenValueDoesNotMatchUsingSpecifiedMechanism() {
        var matcher = KafkaMessageByKeySelector.builder()
                .key("different")
                .valueMatchingStrategy(EQUALS)
                .build();

        assertFalse(matcher.matches(recordWithKey("testValue")));
    }

    @Test
    public void asSelector_exportsKeyAndDefaultEqualsComparator() {
        var key = "order-42";

        var fixture = KafkaMessageByKeySelector.builder()
                .key(key)
                .build();

        var result = fixture.asSelector();

        assertThat(result)
                .containsEntry(KEY_FILTER_VALUE, key)
                .containsEntry(KEY_FILTER_COMPARATOR, EQUALS.toString());
    }

    @Test
    public void asSelector_ignoresNullKey() {
        var fixture = KafkaMessageByKeySelector.builder().build();

        var result = fixture.asSelector();

        assertThat(result)
                .doesNotContainKey(KEY_FILTER_VALUE)
                .containsEntry(KEY_FILTER_COMPARATOR, EQUALS.toString());
    }

    private ConsumerRecord<Object, Object> recordWithKey(Object key) {
        return new ConsumerRecord<>("topic", 0, 0, key, null);
    }
}
