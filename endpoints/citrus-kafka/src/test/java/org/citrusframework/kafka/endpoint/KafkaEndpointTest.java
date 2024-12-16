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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
            null, null, null, null, null, false
        );

        assertThat(fixture)
            .isNotNull()
            .extracting(KafkaEndpoint::createConsumer)
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
            null, null, null, null, false
        );

        assertThat(fixture)
            .isNotNull()
            .extracting(KafkaEndpoint::createProducer)
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
            useRandomConsumerGroup,
            null, null, null, false
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
            TRUE,
            null, null, null, false
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
            // Make sure the random group id is propagated to new consumers
            .satisfies(
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
            null,
            server,
            null, null, false
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
            null, null,
            timeout,
            null, false
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
            null, null, null,
            topic,
            false
        );

        assertThat(fixture)
            .isNotNull()
            .extracting(KafkaEndpoint::getEndpointConfiguration)
            .isNotNull()
            .extracting(KafkaEndpointConfiguration::getTopic)
            .isEqualTo(topic);
    }

    @Test
    public void newKafkaEndpoint_acceptsThreadSafetyConfiguration() {
        var topic = "citrus";

        var fixture = KafkaEndpoint.newKafkaEndpoint(
            null, null, null,
            topic,
            true
        );

        assertThat(fixture)
            .isNotNull()
            .extracting(KafkaEndpoint::getEndpointConfiguration)
            .isNotNull()
            .extracting(KafkaEndpointConfiguration::useThreadSafeConsumer)
            .isEqualTo(true);
    }

    @Test
    public void createConsumer_isMultipleInvocationAware() {
        var fixture = new KafkaEndpoint();

        var firstConsumer = fixture.createConsumer();
        var secondConsumer = fixture.createConsumer();

        assertThat(firstConsumer)
            .isNotNull();
        assertThat(secondConsumer)
            .isNotNull()
            .isSameAs(firstConsumer);
    }

    @Test
    public void createConsumer_returnsNewConsumers_inThreadSafeMode() {
        ThreadLocal<KafkaConsumer> threadLocalKafkaConsumerMock = mock();

        var kafkaConsumerMock = mock(KafkaConsumer.class);
        doReturn(kafkaConsumerMock).when(threadLocalKafkaConsumerMock).get();

        var fixture = new KafkaEndpoint();
        fixture.getEndpointConfiguration().setUseThreadSafeConsumer(true);
        setField(fixture, "threadLocalKafkaConsumer", threadLocalKafkaConsumerMock, ThreadLocal.class);

        var consumer = fixture.createConsumer();

        assertThat(consumer)
            .isEqualTo(kafkaConsumerMock);
        verify(threadLocalKafkaConsumerMock).get();
    }

    @Test
    public void createConsumer_createsNonExistingConsumer_inThreadSafeMode() {
        var fixture = new KafkaEndpoint();
        fixture.getEndpointConfiguration().setUseThreadSafeConsumer(true);

        var firstConsumer = fixture.createConsumer();
        var secondConsumer = fixture.createConsumer();

        assertThat(firstConsumer)
            .isNotNull();
        assertThat(secondConsumer)
            .isNotNull()
            .isSameAs(firstConsumer);
    }

    @Test
    public void createProducer_isMultipleInvocationAware() {
        var fixture = new KafkaEndpoint();

        var firstProducer = fixture.createProducer();
        var secondProducer = fixture.createProducer();

        assertThat(firstProducer)
            .isNotNull();
        assertThat(secondProducer)
            .isNotNull()
            .isSameAs(firstProducer);
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

    @Test
    public void destroy_stopsKafkaConsumer_inThreadSafeMode() {
        ThreadLocal<KafkaConsumer> threadLocalKafkaConsumerMock = mock();

        var kafkaConsumerMock = mock(KafkaConsumer.class);
        doReturn(kafkaConsumerMock).when(threadLocalKafkaConsumerMock).get();

        var fixture = new KafkaEndpoint();
        fixture.getEndpointConfiguration().setUseThreadSafeConsumer(true);
        setField(fixture, "threadLocalKafkaConsumer", threadLocalKafkaConsumerMock, ThreadLocal.class);

        fixture.destroy();

        verify(kafkaConsumerMock).stop();
        verify(threadLocalKafkaConsumerMock).remove();
    }
}
