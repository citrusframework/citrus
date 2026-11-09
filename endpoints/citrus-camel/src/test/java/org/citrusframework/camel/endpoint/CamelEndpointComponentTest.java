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

package org.citrusframework.camel.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @since 1.4.1
 */
public class CamelEndpointComponentTest {

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private final CamelContext camelContext = Mockito.mock(CamelContext.class);
    private final TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateEndpoint() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:direct:news", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("camel:seda:news-feed", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "seda:news-feed");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateSyncEndpoint() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:sync:direct:news", context);

        Assertions.assertEquals(CamelSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelSyncEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news");
        Assertions.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("camel:sync:seda:news-feed", context);

        Assertions.assertEquals(CamelSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelSyncEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "seda:news-feed");
        Assertions.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelSyncEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<>();
        camelContextMap.put("someCamelContext", Mockito.mock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(camelContextMap);
        when(referenceResolver.isResolvable(CamelSettings.getContextName())).thenReturn(false);
        when(referenceResolver.isResolvable("myCamelContext")).thenReturn(true);
        when(referenceResolver.resolve("myCamelContext", CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:direct:news-feed?timeout=10000&camelContext=myCamelContext", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news-feed");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithCamelParameters() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<>();
        camelContextMap.put("someCamelContext", Mockito.mock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(camelContextMap);
        when(referenceResolver.isResolvable(CamelSettings.getContextName())).thenReturn(false);
        when(referenceResolver.isResolvable("myCamelContext")).thenReturn(true);
        when(referenceResolver.resolve("myCamelContext", CamelContext.class)).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:controlbus:route?routeId=news&timeout=10000&camelContext=myCamelContext&action=stats", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "controlbus:route?routeId=news&action=stats");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithBeanReference() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        SimpleRegistry registry = new SimpleRegistry();
        Object newsProvider = new Object();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        when(referenceResolver.isResolvable("newsProvider")).thenReturn(true);
        when(referenceResolver.resolve("newsProvider")).thenReturn(newsProvider);
        when(camelContext.getRegistry()).thenReturn(registry);

        Endpoint endpoint = component.createEndpoint("camel:direct:news?provider=#newsProvider", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news?provider=#newsProvider");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
        Assertions.assertTrue(registry.containsKey("newsProvider"));
        Assertions.assertEquals(newsProvider, registry.lookupByName("newsProvider"));
    }

    @Test
    public void testCreateEndpointWithUnknownBeanReference() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        SimpleRegistry registry = new SimpleRegistry();

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        when(referenceResolver.isResolvable("newsProvider")).thenReturn(false);
        when(camelContext.getRegistry()).thenReturn(registry);

        Endpoint endpoint = component.createEndpoint("camel:direct:news?provider=#newsProvider", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news?provider=#newsProvider");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
        Assertions.assertFalse(registry.containsKey("newsProvider"));
    }

    @Test
    public void testCreateEndpointWithBeanReferenceNoOverwrite() {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Object newsProvider = new Object();
        Object original = new Object();
        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("newsProvider", original);

        reset(referenceResolver);
        when(referenceResolver.resolveAll(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(referenceResolver.resolve(CamelContext.class)).thenReturn(camelContext);
        when(referenceResolver.isResolvable("newsProvider")).thenReturn(true);
        when(referenceResolver.resolve("newsProvider")).thenReturn(newsProvider);
        when(camelContext.getRegistry()).thenReturn(registry);

        Endpoint endpoint = component.createEndpoint("camel:direct:news?provider=#newsProvider", context);

        Assertions.assertEquals(CamelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news?provider=#newsProvider");
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assertions.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
        Assertions.assertTrue(registry.containsKey("newsProvider"));
        Assertions.assertEquals(original, registry.lookupByName("newsProvider"));
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("camel"));
        Assertions.assertEquals(CamelEndpointComponent.class, validators.get("camel").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("camel").isPresent());
    }
}
