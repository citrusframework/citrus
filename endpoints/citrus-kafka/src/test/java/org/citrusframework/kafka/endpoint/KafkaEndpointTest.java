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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class KafkaEndpointTest {

    @Test
    public void classHasBuilder() {
        assertThat(KafkaEndpoint.builder().build())
                .isInstanceOf(KafkaEndpoint.class);
    }

    @Test
    public void defaultConstructor_initializesKafkaEndpointConfiguration() {
        var fixture = new KafkaEndpoint();

        assertThat(fixture)
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull();
    }

    @Test
    public void newKafkaEndpoint_acceptsKafkaConsumer() {
        var kafkaConsumerMock = mock(org.apache.kafka.clients.consumer.KafkaConsumer.class);

        var fixture = KafkaEndpoint.newKafkaEndpoint(
                kafkaConsumerMock,
                null, null, null, null, null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getKafkaConsumer)
                .isNotNull()
                .extracting(org.citrusframework.kafka.endpoint.KafkaConsumer::getConsumer)
                .isEqualTo(kafkaConsumerMock);
    }

    @Test
    public void newKafkaEndpoint_acceptsKafkaProducer() {
        var kafkaProducerMock = mock(org.apache.kafka.clients.producer.KafkaProducer.class);

        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null,
                kafkaProducerMock,
                null, null, null, null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getKafkaProducer)
                .isNotNull()
                .extracting(org.citrusframework.kafka.endpoint.KafkaProducer::getProducer)
                .isEqualTo(kafkaProducerMock);
    }

    @DataProvider
    public static Object[][] defaultConsumerGroups() {
        return new Object[][]{{null}, {FALSE}};
    }

    @Test(dataProvider = "defaultConsumerGroups")
    public void newKafkaEndpoint_usesDefaultConsumerGroup(Boolean useRandomConsumerGroup) {
        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null, null,
                useRandomConsumerGroup,
                null, null, null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull()
                .extracting(KafkaEndpointConfiguration::getConsumerGroup)
                .asInstanceOf(STRING)
                .isNotEmpty()
                .isEqualTo("citrus_kafka_group");
    }

    @Test
    public void newKafkaEndpoint_isAbleToCreateRandomConsumerGroup() {
        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null, null,
                TRUE,
                null, null, null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull()
                .extracting(KafkaEndpointConfiguration::getConsumerGroup)
                .asInstanceOf(STRING)
                .isNotEmpty()
                .startsWith(KAFKA_PREFIX)
                .hasSize(23)
                .containsPattern(".*[a-z]{10}$")
                .satisfies(
                        // Additionally make sure that gets passed downstream
                        groupId -> assertThat(fixture.createConsumer().getConsumer())
                                .extracting("delegate")
                                .extracting("groupId")
                                .asInstanceOf(OPTIONAL)
                                .hasValue(groupId)
                );
    }

    @Test
    public void newKafkaEndpoint_acceptsServer() {
        var server = "localhost";

        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null, null, null,
                server,
                null, null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull()
                .extracting(KafkaEndpointConfiguration::getServer)
                .isEqualTo(server);
    }

    @Test
    public void newKafkaEndpoint_acceptsTimeout() {
        var timeout = 1234L;

        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null, null, null, null,
                timeout,
                null
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull()
                .extracting(KafkaEndpointConfiguration::getTimeout)
                .isEqualTo(timeout);
    }

    @Test
    public void newKafkaEndpoint_acceptsTopic() {
        var topic = "citrus";

        var fixture = KafkaEndpoint.newKafkaEndpoint(
                null, null, null, null, null,
                topic
        );

        assertThat(fixture)
                .isNotNull()
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isNotNull()
                .extracting(KafkaEndpointConfiguration::getTopic)
                .isEqualTo(topic);
    }

    @Test
    public void createConsumer_isMultipleInvocationAware() {
        var fixture = new KafkaEndpoint();

        var firstConsumer = fixture.createConsumer();
        var secondConsumer = fixture.createConsumer();

        assertThat(firstConsumer).isNotNull();
        assertThat(secondConsumer).isNotNull();
        assertThat(firstConsumer).isSameAs(secondConsumer);
    }

    @Test
    public void createProducer_isMultipleInvocationAware() {
        var fixture = new KafkaEndpoint();

        var firstConsumer = fixture.createProducer();
        var secondConsumer = fixture.createProducer();

        assertThat(firstConsumer).isNotNull();
        assertThat(secondConsumer).isNotNull();
        assertThat(firstConsumer).isSameAs(secondConsumer);
    }

    @Test
    public void getEndpointConfiguration_returnsInitialEndpointConfiguration() {
        var kafkaEndpointConfigurationMock = mock(KafkaEndpointConfiguration.class);

        var fixture = new KafkaEndpoint(kafkaEndpointConfigurationMock);

        assertThat(fixture)
                .extracting(KafkaEndpoint::getEndpointConfiguration)
                .isEqualTo(kafkaEndpointConfigurationMock);
    }

    @Test
    public void destroy_stopsKafkaConsumer() {
        var kafkaConsumerMock = mock(KafkaConsumer.class);

        var fixture = new KafkaEndpoint();
        setField(fixture, "kafkaConsumer", kafkaConsumerMock, KafkaConsumer.class);

        fixture.destroy();

        verify(kafkaConsumerMock).stop();
    }
}
