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

import org.citrusframework.exceptions.CitrusRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_KEY;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_VALUE;

public class KafkaMessageSelectorFactory {

    private static final KafkaMessageSelectorFactories strategies = new KafkaMessageSelectorFactories();

    static {
        strategies.put(messageSelectors -> messageSelectors.containsKey(HEADER_FILTER_KEY) || messageSelectors.containsKey(HEADER_FILTER_VALUE), KafkaMessageByHeaderSelector::fromSelector);
    }

    private final KafkaMessageSelectorFactories customStrategies = new KafkaMessageSelectorFactories();

    public void setCustomStrategies(KafkaMessageSelectorFactories customStrategies) {
        this.customStrategies.putAll(customStrategies);
    }

    @SuppressWarnings({"unchecked"})
    public <T> KafkaMessageSelector parseFromSelector(Map<String, T> messageSelectors) {
        return Stream.concat(
                        strategies.entrySet().stream(),
                        customStrategies.entrySet().stream()
                )
                .filter(strategy -> strategy.getKey().test((Map<String, Object>) messageSelectors))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(supplier -> supplier.apply((Map<String, Object>) messageSelectors))
                .orElseThrow(() -> new CitrusRuntimeException("Cannot instantiate Kafka matcher from selectors: " + messageSelectors));
    }

    public static final class KafkaMessageSelectorFactories extends HashMap<Predicate<Map<String, Object>>, Function<Map<String, Object>, KafkaMessageSelector>> {

        public static KafkaMessageSelectorFactories factoryWithKafkaMessageSelector(
                Predicate<Map<String, Object>> selector, Function<Map<String, Object>, KafkaMessageSelector> initializer
        ) {
            var kafkaMessageSelectorFactories = new KafkaMessageSelectorFactories();
            kafkaMessageSelectorFactories.put(selector, initializer);
            return kafkaMessageSelectorFactories;
        }
    }
}
