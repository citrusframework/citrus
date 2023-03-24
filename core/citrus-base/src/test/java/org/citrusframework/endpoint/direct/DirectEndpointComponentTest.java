/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint.direct;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 */
public class DirectEndpointComponentTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void testCreateDirectEndpoint() throws Exception {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Assert.assertFalse(context.getReferenceResolver().isResolvable("queueName"));
        Endpoint endpoint = component.createEndpoint("direct:queueName", context);

        Assert.assertEquals(endpoint.getClass(), DirectEndpoint.class);

        Assert.assertEquals(((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName(), "queueName");
        Assert.assertEquals(((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertTrue(context.getReferenceResolver().isResolvable("queueName"));
    }

    @Test
    public void testCreateSyncDirectEndpoint() throws Exception {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Assert.assertFalse(context.getReferenceResolver().isResolvable("queueName"));
        Endpoint endpoint = component.createEndpoint("direct:sync:queueName", context);

        Assert.assertEquals(endpoint.getClass(), DirectSyncEndpoint.class);

        Assert.assertEquals(((DirectSyncEndpoint)endpoint).getEndpointConfiguration().getQueueName(), "queueName");
        Assert.assertTrue(context.getReferenceResolver().isResolvable("queueName"));
    }

    @Test
    public void testCreateDirectEndpointWithParameters() throws Exception {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Endpoint endpoint = component.createEndpoint("direct:queueName?timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), DirectEndpoint.class);

        Assert.assertEquals(((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName(), "queueName");
        Assert.assertEquals(((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 1L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("direct").isPresent());
    }
}
