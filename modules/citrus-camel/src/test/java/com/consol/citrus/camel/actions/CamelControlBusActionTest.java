/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.camel.actions;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.exceptions.ValidationException;
import org.apache.camel.*;
import org.apache.camel.impl.*;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

public class CamelControlBusActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);
    private ProducerTemplate producerTemplate = EasyMock.createMock(ProducerTemplate.class);

    @Test
     public void testControlBusRouteAction() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage();
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(producerTemplate.request(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        replay(camelContext, producerTemplate);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");

        action.execute(context);

        verify(camelContext, producerTemplate);
    }

    @Test
    public void testControlBusRouteActionWithResult() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage();
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(producerTemplate.request(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        replay(camelContext, producerTemplate);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");
        action.setResult("Started");

        action.execute(context);

        verify(camelContext, producerTemplate);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testControlBusRouteActionWithResultFailed() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage();
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(producerTemplate.request(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        replay(camelContext, producerTemplate);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");
        action.setResult("Stopped");

        action.execute(context);

        verify(camelContext, producerTemplate);
    }

    @Test
    public void testControlBusLanguageAction() throws Exception {
        String endpointUri = "controlbus:language:simple";

        DefaultMessage message = new DefaultMessage();
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(producerTemplate.request(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        replay(camelContext, producerTemplate);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setLanguageExpression("${camelContext.getRouteStatus('myRoute')}");

        action.execute(context);

        verify(camelContext, producerTemplate);
    }

    @Test
    public void testControlBusLanguageActionWithResult() throws Exception {
        String endpointUri = "controlbus:language:simple";

        DefaultMessage message = new DefaultMessage();
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(producerTemplate.request(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        replay(camelContext, producerTemplate);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setLanguageExpression("${camelContext.getRouteStatus('myRoute')}");
        action.setResult("Started");

        action.execute(context);

        verify(camelContext, producerTemplate);
    }
}