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

package org.citrusframework.kafka.integration;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.internal.GitHubIssue;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageSelector;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static java.util.Objects.nonNull;
import static org.citrusframework.kafka.endpoint.KafkaMessageFilter.kafkaMessageFilter;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.ENDS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.STARTS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderContains;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderEquals;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageSelectorFactory.KafkaMessageSelectorFactories.factoryWithKafkaMessageSelector;
import static org.citrusframework.kafka.integration.KafkaEndpointJavaIT.KafkaMessageByKeySelector.MESSAGE_KEY_FILTER_KEY;

@Test(singleThreaded = true)
public class KafkaEndpointJavaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @BindToRegistry
    private final KafkaEndpoint kafkaWithRandomConsumerGroupEndpoint = KafkaEndpoint.builder()
            .randomConsumerGroup(true)
            .topic(getClass().getSimpleName())
            .useThreadSafeConsumer()
            .build();

    @Test
    @CitrusTest
    public void findKafkaEvent_headerEquals_citrus_DSL() {
        var body = "findKafkaEvent_headerEquals_citrus_DSL";

        var key = "Name";
        var value = "Bilbo";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, value))
                                        .build()
                        )
                        .message()
                        .body(body)
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_headerContains_citrus_DSL() {
        var body = "findKafkaEvent_headerContains_citrus_DSL";

        var key = "Name";
        var value = "Frodo";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderContains(key, "odo"))
                                        .build()
                        )
                        .message()
                        .body(body)
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_headerStartsWith_citrus_DSL() {
        var body = "findKafkaEvent_headerStartsWith_citrus_DSL";

        var key = "Name";
        var value = "Galadriel";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(
                                                KafkaMessageByHeaderSelector.builder()
                                                        .key(key)
                                                        .value("Gala")
                                                        .valueMatchingStrategy(STARTS_WITH)
                                                        .build()
                                        )
                                        .build()
                        )
                        .message()
                        .body(body)
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_customSelector_byKey_citrus_DSL() {
        var messageKey = "important random key";
        var body = "findKafkaEvent_customSelector_byKey_citrus_DSL";

        kafkaWithRandomConsumerGroupEndpoint.getEndpointConfiguration()
                .getKafkaMessageSelectorFactory()
                .setCustomStrategies(
                        factoryWithKafkaMessageSelector(
                                messageSelectors -> messageSelectors.containsKey(MESSAGE_KEY_FILTER_KEY),
                                messageSelectors -> new KafkaMessageByKeySelector((String) messageSelectors.get(MESSAGE_KEY_FILTER_KEY))
                        )
                );

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).messageKey(messageKey))
        );

        then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(
                                                new KafkaMessageByKeySelector(messageKey)
                                        )
                                        .build()
                        )
                        .message()
                        .body(body)
        );
    }

    record KafkaMessageByKeySelector(String key) implements KafkaMessageSelector<String> {
        static final String MESSAGE_KEY_FILTER_KEY = "message-key";

        @Override
        public boolean matches(ConsumerRecord<Object, Object> consumerRecord) {
            return nonNull(consumerRecord.key()) && consumerRecord.key().equals(key);
        }

        @Override
        public Map<String, String> asSelector() {
            return Map.of(MESSAGE_KEY_FILTER_KEY, key);
        }
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_headerEndsWith_citrus_DSL() {
        var body = "findKafkaEvent_headerEndsWith_citrus_DSL";

        var key = "Name";
        var value = "Celeborn";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(
                                                KafkaMessageByHeaderSelector.builder()
                                                        .key(key)
                                                        .value("born")
                                                        .valueMatchingStrategy(ENDS_WITH)
                                                        .build()
                                        )
                                        .build()
                        )
                        .message()
                        .body(body)
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_nothingFound_noMatch_citrus_DSL() {
        var body = "findKafkaEvent_nothingFound_noMatch_citrus_DSL";

        var key = "Name";
        var value = "Elrond";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                assertException()
                    .exception(CitrusRuntimeException.class)
                    .message("@startsWith(Failed to resolve Kafka message using selector)@")
                    .when(receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, "Arwen"))
                                        .build()
                        )
                        .message()
                        .body(body)
                    )
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_nothingFound_outsideLookbackWindow_citrus_DSL() {
        var body = "findKafkaEvent_nothingFound_outsideLookbackWindow_citrus_DSL";

        var key = "Name";
        var value = "Gimli";

        given(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        when(sleep().seconds(2));

        then(
                assertException()
                    .exception(CitrusRuntimeException.class)
                    .message("@startsWith(Failed to resolve Kafka message using selector)@")
                    .when(receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, value))
                                        .build()
                        )
                        .message()
                        .body(body)
                    )
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_duplicateEntriesFound_citrus_DSL() {
        var body = "findKafkaEvent_duplicateEntriesFound_citrus_DSL";

        var key = "Name";

        given(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, "Gandalf the Grey"))
        );

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, "Gandalf the White"))
        );

        then(
                assertException()
                    .exception(CitrusRuntimeException.class)
                    .message("@startsWith(More than one matching record found in topic)@")
                    .when(receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderContains(key, "Gandalf"))
                                        .build()
                        )
                        .message()
                        .body(body)
                    )
        );
    }

    @Test
    @CitrusTest
    public void findKafkaEvent_headerEquals_java_DSL() {
        var body = "findKafkaEvent_headerEquals_java_DSL";

        var key = "Name";
        var value = "Gollum";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                kafkaWithRandomConsumerGroupEndpoint.findKafkaEventHeaderEquals(Duration.ofSeconds(1L), key, value)
                        .body(body)
        );
    }

    @Test
    @CitrusTest
    public void shutdown_afterTimeout_isThreadSafe() {
        KafkaEndpoint kafkaEndpoint = KafkaEndpoint.builder()
                .randomConsumerGroup(true)
                .topic("names")
                .useThreadSafeConsumer()
                .build();

        var body = "shutdown_afterTimeout_isThreadSafe";

        var key = "Name";
        var value = "Aragorn";

        when(
                send(kafkaEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        then(
                assertException()
                    .exception(ActionTimeoutException.class)
                    .message("Action timeout after 2000 milliseconds. Failed to receive message on endpoint: 'names'")
                    .when(receive(kafkaEndpoint)
                        .timeout(2_000)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, "Samwise"))
                                        .pollTimeout(Duration.ofSeconds(3)) // Note that pollTimeout > overall receive timeout
                                        .build()
                        )
                        .message()
                        .body(body)
                    )
        );
    }

    @Test
    @CitrusTest
    @GitHubIssue(1281)
    public void threadSafetyOfKafkaConsumer_onParallelAccess() {
        var body = "parallel_access_thread_safety";

        var key = "Name";

        var brother1 = "Elladan";
        var brother2 = "Elrohir";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, brother1))
        );

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, brother2))
        );

        then(
                parallel()
                        .actions(
                                kafkaWithRandomConsumerGroupEndpoint.findKafkaEventHeaderEquals(Duration.ofSeconds(1L), key, brother1)
                                        .body(body),
                                kafkaWithRandomConsumerGroupEndpoint.findKafkaEventHeaderEquals(Duration.ofSeconds(1L), key, brother2)
                                        .body(body)
                        )
        );
    }
}
