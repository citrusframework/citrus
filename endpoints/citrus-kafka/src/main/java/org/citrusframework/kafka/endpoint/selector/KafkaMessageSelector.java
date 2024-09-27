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

import java.util.Map;

/**
 * Defines custom matching logic for filtering {@link ConsumerRecord} instances in Kafka. Implementations of this
 * interface can define specific criteria for matching Kafka messages based on their content, headers, or any other
 * attributes.
 *
 * @see org.citrusframework.kafka.endpoint.KafkaMessageFilteringConsumer
 */
public interface KafkaMessageSelector {

    /**
     * Determines whether a given ConsumerRecord matches specific criteria.
     *
     * @param consumerRecord The ConsumerRecord to be evaluated.
     * @return true if the record matches the defined criteria, false otherwise.
     */
    boolean matches(ConsumerRecord<Object, Object> consumerRecord);

    /**
     * Transforms the current matcher into its key-value representation, a so-called "selector" in the citrus context.
     *
     * @return Selector representation of the matcher.
     */
    <T> Map<String, T> asSelector();
}
