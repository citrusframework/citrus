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
import org.apache.kafka.common.header.internals.RecordHeader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_COMPARATOR;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_KEY;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_VALUE;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.CONTAINS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.EQUALS;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.STARTS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderContains;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KafkaMessageByHeaderSelectorTest {

    @Test
    public void builder() {
        var key = "key";
        var value = "value";

        var fixture = KafkaMessageByHeaderSelector.builder()
                .key(key)
                .value(value)
                .valueMatchingStrategy(EQUALS)
                .build();

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValue()).isEqualTo(value),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(EQUALS)
                );
    }

    @Test
    public void kafkaHeaderContains_returns_CONTAINS_matcher() {
        var key = "key";
        var value = "value";

        var fixture = kafkaHeaderContains(key, value);

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValue()).isEqualTo(value),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(CONTAINS)
                );
    }

    @Test
    public void kafkaHeaderEquals_returns_EQUALS_matcher() {
        var key = "key";
        var value = "value";

        var fixture = kafkaHeaderEquals(key, value);

        assertThat(fixture)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValue()).isEqualTo(value),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(EQUALS)
                );
    }

    @Test
    public void fromSelector_throwsException_whenNeitherKeyNorValuePresent() {
        var messageSelectors = new HashMap<String, Object>();

        assertThatThrownBy(() -> KafkaMessageByHeaderSelector.fromSelector(messageSelectors))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("No matcher specified when looking for Kafka messages");
    }

    @Test
    public void fromSelector_createsKafkaMessageByHeaderSelector() {
        var key = "key";
        var value = "value";

        var messageSelectors = Map.of(
                HEADER_FILTER_KEY, key,
                HEADER_FILTER_VALUE, value,
                HEADER_FILTER_COMPARATOR, STARTS_WITH.toString()
        );

        var result = KafkaMessageByHeaderSelector.fromSelector(messageSelectors);

        assertThat(result)
                .satisfies(
                        m -> assertThat(m.getKey()).isEqualTo(key),
                        m -> assertThat(m.getValue()).isEqualTo(value),
                        m -> assertThat(m.getValueMatchingStrategy()).isEqualTo(STARTS_WITH)
                );
    }

    @Test
    public void matches_AnyHeaderWhenKeyIsNull() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .value("test")
                .build();

        var record = createRecordWithHeaders(
                new RecordHeader("header1", "test".getBytes()),
                new RecordHeader("header2", "other".getBytes())
        );

        assertTrue(matcher.matches(record));
    }

    @Test
    public void matches_specificHeaderWhenKeyIsDefined() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header1")
                .value("test")
                .build();

        var record = createRecordWithHeaders(
                new RecordHeader("header1", "test".getBytes()),
                new RecordHeader("header2", "test".getBytes())
        );

        assertTrue(matcher.matches(record));
    }

    @Test
    public void matches_doesNotMatchWhenKeyIsDefinedButNotPresent() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header3")
                .value("test")
                .build();

        var record = createRecordWithHeaders(
                new RecordHeader("header1", "test".getBytes()),
                new RecordHeader("header2", "test".getBytes())
        );

        assertFalse(matcher.matches(record));
    }

    @Test
    public void matches_anyValueWhenValueIsNull() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header1")
                .build();

        var record = createRecordWithHeaders(new RecordHeader("header1", "anything".getBytes()));

        assertTrue(matcher.matches(record));
    }

    @DataProvider
    public static Object[][] matchingMechanism() {
        return stream(ValueMatchingStrategy.values())
                .map(m -> new Object[]{m})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "matchingMechanism")
    public void matches_usingSpecifiedMechanism(ValueMatchingStrategy mechanism) {
        String headerValue = "testValue";
        String matchValue = switch (mechanism) {
            case EQUALS -> "testValue";
            case CONTAINS -> "stVal";
            case STARTS_WITH -> "test";
            case ENDS_WITH -> "Value";
        };

        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header")
                .value(matchValue)
                .valueMatchingStrategy(mechanism)
                .build();

        var record = createRecordWithHeaders(new RecordHeader("header", headerValue.getBytes()));

        assertTrue(matcher.matches(record));
    }

    @Test
    public void matches_emptyValueWithEquality() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header1")
                .value("")
                .build();

        var record = createRecordWithHeaders(new RecordHeader("header1", "".getBytes()));

        assertTrue(matcher.matches(record));
    }

    @Test
    public void matches_doesNotMatchWhenValueDoesNotMatchUsingSpecifiedMechanism() {
        var matcher = KafkaMessageByHeaderSelector.builder()
                .key("header")
                .value("different")
                .valueMatchingStrategy(EQUALS)
                .build();

        var record = createRecordWithHeaders(new RecordHeader("header", "testValue".getBytes()));

        assertFalse(matcher.matches(record));
    }

    @Test
    public void asSelector_ignoresNullValues() {
        var fixture = KafkaMessageByHeaderSelector.builder().build();

        var result = fixture.asSelector();

        assertThat(result)
                .doesNotContainKey(HEADER_FILTER_KEY)
                .doesNotContainKey(HEADER_FILTER_VALUE);
    }

    @Test
    public void asSelector_exportsKey() {
        var key = "key";

        var fixture = KafkaMessageByHeaderSelector.builder()
                .key(key)
                .build();

        var result = fixture.asSelector();

        assertThat(result)
                .containsEntry(HEADER_FILTER_KEY, key);
    }

    @Test
    public void asSelector_exportsValue() {
        var value = "value";

        var fixture = KafkaMessageByHeaderSelector.builder()
                .value(value)
                .build();

        var result = fixture.asSelector();

        assertThat(result)
                .containsEntry(HEADER_FILTER_VALUE, value);
    }

    @Test
    public void asSelector_containsEqualsMatcherPerDefault() {
        var fixture = KafkaMessageByHeaderSelector.builder().build();

        var result = fixture.asSelector();

        assertThat(result)
                .containsEntry(HEADER_FILTER_COMPARATOR, EQUALS.toString());
    }

    private ConsumerRecord<Object, Object> createRecordWithHeaders(RecordHeader... headers) {
        var consumerRecord = new ConsumerRecord<>("topic", 0, 0, null, null);
        stream(headers).forEach(header -> consumerRecord.headers().add(header));
        return consumerRecord;
    }
}
