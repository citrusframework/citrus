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

package org.citrusframework.camel.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.spi.ReferenceResolver;
import org.apache.camel.CamelContext;
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
public class CamelEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:direct:news", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("camel:seda:news-feed", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "seda:news-feed");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateSyncEndpoint() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:sync:direct:news", context);

        Assert.assertEquals(endpoint.getClass(), CamelSyncEndpoint.class);

        Assert.assertEquals(((CamelSyncEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news");
        Assert.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("camel:sync:seda:news-feed", context);

        Assert.assertEquals(endpoint.getClass(), CamelSyncEndpoint.class);

        Assert.assertEquals(((CamelSyncEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "seda:news-feed");
        Assert.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<String, CamelContext>();
        camelContextMap.put("someCamelContext", Mockito.mock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(camelContextMap);
        when(referenceResolver.isResolvable("camelContext")).thenReturn(false);
        when(referenceResolver.isResolvable("myCamelContext")).thenReturn(true);
        when(referenceResolver.resolve("myCamelContext", CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:direct:news-feed?timeout=10000&camelContext=myCamelContext", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news-feed");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testCreateEndpointWithCamelParameters() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<String, CamelContext>();
        camelContextMap.put("someCamelContext", Mockito.mock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(camelContextMap);
        when(referenceResolver.isResolvable("camelContext")).thenReturn(false);
        when(referenceResolver.isResolvable("myCamelContext")).thenReturn(true);
        when(referenceResolver.resolve("myCamelContext", CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:controlbus:route?routeId=news&timeout=10000&camelContext=myCamelContext&action=stats", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "controlbus:route?routeId=news&action=stats");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("camel"));
        Assert.assertEquals(validators.get("camel").getClass(), CamelEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("camel").isPresent());
    }
}
