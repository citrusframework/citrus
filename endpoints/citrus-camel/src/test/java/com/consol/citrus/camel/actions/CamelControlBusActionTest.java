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
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultHeadersMapFactory;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.impl.JavaUuidGenerator;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .route("route_1", "status")
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .route("${routeId}", "${action}")
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .route("route_1", "status")
                .result("Started")
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .route("route_1", "status")
                .result("Stopped")
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .language("simple", "${camelContext.getRouteStatus('myRoute')}")
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .language(SimpleBuilder.simple("${camelContext.getRouteStatus('${routeId}')}"))
                .build();
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

        CamelControlBusAction action = new CamelControlBusAction.Builder()
                .context(camelContext)
                .language(SimpleBuilder.simple("${camelContext.getRouteStatus('myRoute')}"))
                .result("Started")
                .build();
        action.execute(context);
    }
}
