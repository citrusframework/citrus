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

import org.assertj.core.api.ThrowableAssert;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.kafka.endpoint.KafkaMessageFilter.kafkaMessageFilter;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.ENDS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.ValueMatchingStrategy.STARTS_WITH;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderContains;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.kafkaHeaderEquals;

@Test(singleThreaded = true)
public class KafkaEndpointJavaIT extends TestNGCitrusSpringSupport {

    @BindToRegistry
    private final KafkaEndpoint kafkaWithRandomConsumerGroupEndpoint = KafkaEndpoint.builder()
            .randomConsumerGroup(true)
            .topic("hello")
            .build();

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
                        .getMessageBuilderSupport()
                        .body(body)
        );
    }

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
                        .getMessageBuilderSupport()
                        .body(body)
        );
    }

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
                        .getMessageBuilderSupport()
                        .body(body)
        );
    }

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
                        .getMessageBuilderSupport()
                        .body(body)
        );
    }

    @CitrusTest
    public void findKafkaEvent_nothingFound_noMatch_citrus_DSL() {
        var body = "findKafkaEvent_nothingFound_noMatch_citrus_DSL";

        var key = "Name";
        var value = "Elrond";

        when(
                send(kafkaWithRandomConsumerGroupEndpoint)
                        .message(new KafkaMessage(body).setHeader(key, value))
        );

        ThrowableAssert.ThrowingCallable receiver = () -> then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, "Arwen"))
                                        .build()
                        )
                        .getMessageBuilderSupport()
                        .body(body)
        );

        assertThatThrownBy(receiver)
                .isInstanceOf(TestCaseFailedException.class)
                .hasRootCauseInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("Failed to resolve Kafka message using selector");
    }

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

        ThrowableAssert.ThrowingCallable receiver = () -> then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderEquals(key, value))
                                        .build()
                        )
                        .getMessageBuilderSupport()
                        .body(body)
        );

        assertThatThrownBy(receiver)
                .isInstanceOf(TestCaseFailedException.class)
                .hasRootCauseInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("Failed to resolve Kafka message using selector");
    }

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

        ThrowableAssert.ThrowingCallable receiver = () -> then(
                receive(kafkaWithRandomConsumerGroupEndpoint)
                        .selector(
                                kafkaMessageFilter()
                                        .eventLookbackWindow(Duration.ofSeconds(1L))
                                        .kafkaMessageSelector(kafkaHeaderContains(key, "Gandalf"))
                                        .build()
                        )
                        .getMessageBuilderSupport()
                        .body(body)
        );

        assertThatThrownBy(receiver)
                .isInstanceOf(TestCaseFailedException.class)
                .hasRootCauseInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("More than one matching record found in topic");
    }

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

}
