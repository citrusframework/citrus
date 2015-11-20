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

package com.consol.citrus.camel.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.camel.CamelContext;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(applicationContext);
        when(applicationContext.getBeansOfType(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(applicationContext.getBean(CamelContext.class)).thenReturn(camelContext);
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

        reset(applicationContext);
        when(applicationContext.getBeansOfType(CamelContext.class)).thenReturn(Collections.singletonMap("myCamelContext", camelContext));
        when(applicationContext.getBean(CamelContext.class)).thenReturn(camelContext);
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

        reset(applicationContext);
        when(applicationContext.getBeansOfType(CamelContext.class)).thenReturn(camelContextMap);
        when(applicationContext.containsBean("camelContext")).thenReturn(false);
        when(applicationContext.containsBean("myCamelContext")).thenReturn(true);
        when(applicationContext.getBean("myCamelContext")).thenReturn(camelContext);
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

        reset(applicationContext);
        when(applicationContext.getBeansOfType(CamelContext.class)).thenReturn(camelContextMap);
        when(applicationContext.containsBean("camelContext")).thenReturn(false);
        when(applicationContext.containsBean("myCamelContext")).thenReturn(true);
        when(applicationContext.getBean("myCamelContext")).thenReturn(camelContext);
        Endpoint endpoint = component.createEndpoint("camel:controlbus:route?routeId=news&timeout=10000&camelContext=myCamelContext&action=stats", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "controlbus:route?routeId=news&action=stats");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        try {
            reset(applicationContext);
            component.createEndpoint("camel:direct:news?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid parameter"));
        }

    }
}
