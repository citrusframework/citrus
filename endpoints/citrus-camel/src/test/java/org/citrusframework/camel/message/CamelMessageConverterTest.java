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

package org.citrusframework.camel.message;

import java.util.UUID;

import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
import org.apache.camel.support.DefaultExchange;
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
public class CamelMessageConverterTest extends AbstractTestNGUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final ExtendedCamelContext extendedCamelContext = Mockito.mock(ExtendedCamelContext.class);
    private final CamelMessageConverter messageConverter = new CamelMessageConverter();
    private final CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();

    @BeforeClass
    void setupMocks() {
        endpointConfiguration.setCamelContext(camelContext);

        reset(camelContext);
        when(camelContext.getCamelContextExtension()).thenReturn(extendedCamelContext);
        when(extendedCamelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
    }

    @Test
    public void testConvertOutbound() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");

        Exchange exchange = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
    }

    @Test
    public void testConvertOutboundExchange() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
    }

    @Test
    public void testConvertInbound() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assert.assertEquals(result.getPayload(), "Hello from Citrus!");
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_ID), exchange.getExchangeId());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN), ExchangePattern.InOnly.name());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED), false);
        Assert.assertEquals(result.getHeader("operation"), "sayHello");
    }

    @Test
    public void testConvertInboundWithProperties() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");

        exchange.setProperty("SpecialProperty", "foo");
        exchange.setProperty("VerySpecialProperty", "bar");

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assert.assertEquals(result.getPayload(), "Hello from Citrus!");
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_ID), exchange.getExchangeId());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN), ExchangePattern.InOnly.name());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED), false);
        Assert.assertEquals(result.getHeader("operation"), "sayHello");
        Assert.assertEquals(result.getHeader("SpecialProperty"), "foo");
        Assert.assertEquals(result.getHeader("VerySpecialProperty"), "bar");
    }

    @Test
    public void testConvertInboundWithException() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");
        exchange.setException(new CitrusRuntimeException("Something went wrong"));

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assert.assertEquals(result.getPayload(), "Hello from Citrus!");
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_ID), exchange.getExchangeId());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN), ExchangePattern.InOnly.name());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED), true);
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION), CitrusRuntimeException.class.getName());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE), "Something went wrong");
        Assert.assertEquals(result.getHeader("operation"), "sayHello");
    }
}
