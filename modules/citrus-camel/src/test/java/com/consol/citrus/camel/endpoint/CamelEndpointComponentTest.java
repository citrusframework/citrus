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
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.getBeansOfType(CamelContext.class)).andReturn(Collections.singletonMap("myCamelContext", camelContext)).times(2);
        expect(applicationContext.getBean(CamelContext.class)).andReturn(camelContext).times(2);
        replay(applicationContext);

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

        verify(applicationContext);
    }

    @Test
    public void testCreateSyncEndpoint() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.getBeansOfType(CamelContext.class)).andReturn(Collections.singletonMap("myCamelContext", camelContext)).times(2);
        expect(applicationContext.getBean(CamelContext.class)).andReturn(camelContext).times(2);
        replay(applicationContext);

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

        verify(applicationContext);
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<String, CamelContext>();
        camelContextMap.put("someCamelContext", EasyMock.createMock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(applicationContext);
        expect(applicationContext.getBeansOfType(CamelContext.class)).andReturn(camelContextMap).once();
        expect(applicationContext.containsBean("camelContext")).andReturn(false).once();
        expect(applicationContext.containsBean("myCamelContext")).andReturn(true).once();
        expect(applicationContext.getBean("myCamelContext")).andReturn(camelContext).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("camel:direct:news-feed?timeout=10000&camelContext=myCamelContext", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "direct:news-feed");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }

    @Test
    public void testCreateEndpointWithCamelParameters() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        Map<String, CamelContext> camelContextMap = new HashMap<String, CamelContext>();
        camelContextMap.put("someCamelContext", EasyMock.createMock(CamelContext.class));
        camelContextMap.put("myCamelContext", camelContext);

        reset(applicationContext);
        expect(applicationContext.getBeansOfType(CamelContext.class)).andReturn(camelContextMap).once();
        expect(applicationContext.containsBean("camelContext")).andReturn(false).once();
        expect(applicationContext.containsBean("myCamelContext")).andReturn(true).once();
        expect(applicationContext.getBean("myCamelContext")).andReturn(camelContext).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("camel:controlbus:route?routeId=news&timeout=10000&camelContext=myCamelContext&action=stats", context);

        Assert.assertEquals(endpoint.getClass(), CamelEndpoint.class);

        Assert.assertEquals(((CamelEndpoint)endpoint).getEndpointConfiguration().getEndpointUri(), "controlbus:route?routeId=news&action=stats");
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(((CamelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        CamelEndpointComponent component = new CamelEndpointComponent();

        reset(applicationContext);
        replay(applicationContext);

        try {
            component.createEndpoint("camel:direct:news?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid parameter"));
            verify(applicationContext);
        }

    }
}
