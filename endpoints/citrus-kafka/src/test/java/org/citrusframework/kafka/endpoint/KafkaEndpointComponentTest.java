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
import org.testng.Assert;
import org.testng.annotations.Test;

public class KafkaEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateEndpoint() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers(), "localhost:9092");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server=localhost:9091&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers(), "localhost:9091");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithTopicNameAsParameter() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka?topic=test&server=localhost:9091&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers(), "localhost:9091");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithBootstrapServersParameter() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?bootstrapServers=localhost:9091&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers(), "localhost:9091");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers(), "localhost:9091");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithNullParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertNull(((KafkaEndpoint) endpoint).getEndpointConfiguration().getBootstrapServers());
    }

    @Test
    public void testCreateEndpointWithDynamicConsumerGroup() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint1 = component.createEndpoint("kafka:topic-1", context);
        Endpoint endpoint2 = component.createEndpoint("kafka:topic-2", context);

        String group1 = ((KafkaEndpoint) endpoint1).getEndpointConfiguration().getConsumerGroup();
        String group2 = ((KafkaEndpoint) endpoint2).getEndpointConfiguration().getConsumerGroup();

        Assert.assertTrue(group1.startsWith("citrus_kafka_group_"), "Consumer group should start with 'citrus_kafka_group_' but was: " + group1);
        Assert.assertTrue(group2.startsWith("citrus_kafka_group_"), "Consumer group should start with 'citrus_kafka_group_' but was: " + group2);
        Assert.assertNotEquals(group1, group2, "Dynamic endpoints should have different consumer groups");
    }

    @Test
    public void testCreateEndpointWithExplicitConsumerGroup() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:topic-1?consumerGroup=my-custom-group", context);

        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getConsumerGroup(), "my-custom-group");
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

            Assert.assertEquals(group1, "citrus_kafka_group");
            Assert.assertEquals(group2, "citrus_kafka_group");
        } finally {
            System.clearProperty("citrus.kafka.dynamic.consumer.group");
        }
    }

    @Test
    public void testInvalidEndpointUri() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();
        try {
            component.createEndpoint("kafka:test?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("message-store"));
        Assert.assertEquals(validators.get("message-store").getClass(), MessageStoreEndpointComponent.class);
        Assert.assertNotNull(validators.get("kafka"));
        Assert.assertEquals(validators.get("kafka").getClass(), KafkaEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("kafka").isPresent());
    }
}
