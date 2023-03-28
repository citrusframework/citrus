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

package org.citrusframework.vertx.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private VertxInstanceFactory instanceFactory = Mockito.mock(VertxInstanceFactory.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("vertxInstanceFactory")).thenReturn(true);
        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news");
        Assert.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("eventbus:news-feed", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news-feed");
        Assert.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("vertxInstanceFactory")).thenReturn(true);
        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news-feed?port=10105&timeout=10000&pubSubDomain=true", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news-feed");
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getPort(), 10105);
        Assert.assertTrue(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointCustomInstanceFactory() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolve("vertxFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news?vertxInstanceFactory=vertxFactory", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news");
        Assert.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        try {
            reset(referenceResolver);
            component.createEndpoint("vertx:news?param1=&param2=value2", context);
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
        Assert.assertNotNull(validators.get("vertx"));
        Assert.assertEquals(validators.get("vertx").getClass(), VertxEndpointComponent.class);
        Assert.assertNotNull(validators.get("eventbus"));
        Assert.assertEquals(validators.get("eventbus").getClass(), VertxEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("vertx").isPresent());
        Assert.assertTrue(EndpointComponent.lookup("eventbus").isPresent());
    }
}
