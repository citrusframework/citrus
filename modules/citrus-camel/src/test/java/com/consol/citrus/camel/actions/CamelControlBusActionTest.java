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

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.camel.*;
import org.apache.camel.impl.*;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class CamelControlBusActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private ProducerTemplate producerTemplate = Mockito.mock(ProducerTemplate.class);

    @Test
    public void testControlBusRouteAction() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");

        action.execute(context);

    }
    
    @Test
    public void testControlBusRouteActionVariableSupport() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        context.setVariable("routeId", "route_1");
        context.setVariable("action", "status");

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("${routeId}");
        action.setAction("${action}");

        action.execute(context);

    }

    @Test
    public void testControlBusRouteActionWithResult() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");
        action.setResult("Started");

        action.execute(context);

    }

    @Test(expectedExceptions = ValidationException.class)
    public void testControlBusRouteActionWithResultFailed() throws Exception {
        String endpointUri = "controlbus:route?routeId=route_1&action=status";

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setRouteId("route_1");
        action.setAction("status");
        action.setResult("Stopped");

        action.execute(context);

    }

    @Test
    public void testControlBusLanguageAction() throws Exception {
        String endpointUri = "controlbus:language:simple";

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Started");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setLanguageExpression("${camelContext.getRouteStatus('myRoute')}");

        action.execute(context);

    }

    @Test
    public void testControlBusLanguageActionVariableSupport() throws Exception {
        String endpointUri = "controlbus:language:simple";

        DefaultMessage message = new DefaultMessage(camelContext);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        context.setVariable("routeId", "myRoute");

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenAnswer(invocation -> {
            Processor processor = (Processor) invocation.getArguments()[1];
            processor.process(exchange);

            Assert.assertEquals(exchange.getIn().getBody().toString(), "${camelContext.getRouteStatus('myRoute')}");
            exchange.getIn().setBody("Started");
            return exchange;
        });

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setLanguageExpression("${camelContext.getRouteStatus('${routeId}')}");

        action.execute(context);
    }

    @Test
    public void testControlBusLanguageActionWithResult() throws Exception {
        String endpointUri = "controlbus:language:simple";

        DefaultMessage message = new DefaultMessage(camelContext);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenAnswer(invocation -> {
            Processor processor = (Processor) invocation.getArguments()[1];
            processor.process(exchange);

            Assert.assertEquals(exchange.getIn().getBody().toString(), "${camelContext.getRouteStatus('myRoute')}");
            exchange.getIn().setBody("Started");
            return exchange;
        });

        CamelControlBusAction action = new CamelControlBusAction();
        action.setCamelContext(camelContext);
        action.setLanguageExpression("${camelContext.getRouteStatus('myRoute')}");
        action.setResult("Started");

        action.execute(context);
    }
}