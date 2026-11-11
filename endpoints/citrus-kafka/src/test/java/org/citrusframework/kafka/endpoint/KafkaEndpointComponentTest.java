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

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Mutates global citrus.kafka.dynamic.consumer.group System property")
public class KafkaEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateEndpoint() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test", context);

        Assertions.assertEquals(KafkaEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("test", ((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic());
        Assertions.assertEquals("localhost:9092", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals(5000L, ((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server=localhost:9091&timeout=10000", context);

        Assertions.assertEquals(KafkaEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("test", ((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic());
        Assertions.assertEquals("localhost:9091", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals(10000L, ((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithTopicNameAsParameter() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka?topic=test&server=localhost:9091&timeout=10000", context);

        Assertions.assertEquals(KafkaEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("test", ((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic());
        Assertions.assertEquals("localhost:9091", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals(10000L, ((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithBootstrapServersParameter() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?bootstrapServers=localhost:9091&timeout=10000", context);

        Assertions.assertEquals(KafkaEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("test", ((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic());
        Assertions.assertEquals("localhost:9091", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals("localhost:9091", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
        Assertions.assertEquals(10000L, ((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithNullParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server", context);

        Assertions.assertEquals(KafkaEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("test", ((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic());
        Assertions.assertNull(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
    }

    @Test
    public void testCreateEndpointWithDynamicConsumerGroup() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint1 = component.createEndpoint("kafka:topic-1", context);
        Endpoint endpoint2 = component.createEndpoint("kafka:topic-2", context);

        String group1 = ((KafkaEndpoint) endpoint1).getEndpointConfiguration().getConsumerGroup();
        String group2 = ((KafkaEndpoint) endpoint2).getEndpointConfiguration().getConsumerGroup();

        Assertions.assertTrue(group1.startsWith("citrus_kafka_group_"), "Consumer group should start with 'citrus_kafka_group_' but was: " + group1);
        Assertions.assertTrue(group2.startsWith("citrus_kafka_group_"), "Consumer group should start with 'citrus_kafka_group_' but was: " + group2);
        Assertions.assertNotEquals(group1, group2, "Dynamic endpoints should have different consumer groups");
    }

    @Test
    public void testCreateEndpointWithExplicitConsumerGroup() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:topic-1?consumerGroup=my-custom-group", context);

        Assertions.assertEquals("my-custom-group", ((KafkaEndpoint) endpoint).getEndpointConfiguration().getConsumerGroup());
    }

    @Test
    public void testCreateEndpointWithDynamicConsumerGroupDisabled() {
        System.setProperty("citrus.kafka.dynamic.consumer.group", "false");
        try {
            KafkaEndpointComponent component = new KafkaEndpointComponent();

            Endpoint endpoint1 = component.createEndpoint("kafka:topic-1", context);
            Endpoint endpoint2 = component.createEndpoint("kafka:topic-2", context);

            String group1 = ((KafkaEndpoint) endpoint1).getEndpointConfiguration().getConsumerGroup();
            String group2 = ((KafkaEndpoint) endpoint2).getEndpointConfiguration().getConsumerGroup();

            Assertions.assertEquals("citrus_kafka_group", group1);
            Assertions.assertEquals("citrus_kafka_group", group2);
        } finally {
            System.clearProperty("citrus.kafka.dynamic.consumer.group");
        }
    }

    @Test
    public void testInvalidEndpointUri() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();
        try {
            component.createEndpoint("kafka:test?param1=&param2=value2", context);
            Assertions.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("kafka"));
        Assertions.assertEquals(KafkaEndpointComponent.class, validators.get("kafka").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("kafka").isPresent());
    }
}
