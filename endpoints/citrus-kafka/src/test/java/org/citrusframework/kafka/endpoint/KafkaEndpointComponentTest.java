/*
 * Copyright 2006-2018 the original author or authors.
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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class KafkaEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateEndpoint() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getServer(), "localhost:9092");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server=localhost:9091&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getServer(), "localhost:9091");
        Assert.assertEquals(((KafkaEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithNullParameters() {
        KafkaEndpointComponent component = new KafkaEndpointComponent();

        Endpoint endpoint = component.createEndpoint("kafka:test?server", context);

        Assert.assertEquals(endpoint.getClass(), KafkaEndpoint.class);

        Assert.assertEquals(((KafkaEndpoint)endpoint).getEndpointConfiguration().getTopic(), "test");
        Assert.assertNull(((KafkaEndpoint) endpoint).getEndpointConfiguration().getServer());
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
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("kafka"));
        Assert.assertEquals(validators.get("kafka").getClass(), KafkaEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("kafka").isPresent());
    }
}
