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

package org.citrusframework.camel.message;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.CamelException;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
import org.apache.camel.support.DefaultExchange;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.StringUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
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
        Assert.assertEquals(exchange.getPattern(), ExchangePattern.InOnly);
    }

    @Test
    public void testConvertOutboundExchangeWithExchangePattern() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_PATTERN, ExchangePattern.InOut);

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getPattern(), ExchangePattern.InOut);
    }

    @Test
    public void testConvertOutboundExchangeWithExceptionType() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, CitrusRuntimeException.class.getName())
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, "Something went wrong");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getException().getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(exchange.getException().getMessage(), "Something went wrong");
    }

    @Test
    public void testConvertOutboundExchangeWithCamelExchangeExceptionType() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, CamelExchangeException.class.getName())
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, "Something went wrong");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getException().getClass(), CamelExchangeException.class);
        Assert.assertEquals(((CamelExchangeException) exchange.getException()).getExchange(), exchange);
        Assert.assertEquals(exchange.getException().getMessage(), "Something went wrong. %s".formatted(exchange));
    }

    @Test
    public void testConvertOutboundExchangeWithException() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, new CitrusRuntimeException("Something went wrong"));

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getException().getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(exchange.getException().getMessage(), "Something went wrong");
    }

    @Test
    public void testConvertOutboundExchangeWithExceptionMessage() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, "Something went completely wrong");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getException().getClass(), CamelException.class);
        Assert.assertEquals(exchange.getException().getMessage(), "Something went completely wrong");
    }

    @Test
    public void testConvertOutboundExchangeWithProperties() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES, StringUtils.convertToString(Collections.singletonMap("foo_prop", "bar")));

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getProperty("foo_prop"), "bar");
    }

    @Test
    public void testConvertOutboundExchangeWithVariables() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_VARIABLES, StringUtils.convertToString(
                        Map.of("foo_var", "bar", "bar_var", "baz")));

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getVariable("foo_var"), "bar");
        Assert.assertEquals(exchange.getVariable("bar_var"), "baz");
    }

    @Test
    public void testConvertOutboundExchangeAsPayload() {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");
        exchange.setProperty("foo_prop", "bar");
        exchange.setVariable("foo_var", "baz");

        Message message = new DefaultMessage(exchange);
        message.setHeader("additional", "Something to add");

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getIn().getHeaders().get("additional"), "Something to add");
        Assert.assertEquals(exchange.getProperty("foo_prop"), "bar");
        Assert.assertEquals(exchange.getVariable("foo_var"), "baz");
    }

    @Test
    public void testConvertOutboundCopyExchange() {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        Exchange sourceExchange = new DefaultExchange(camelContext);
        sourceExchange.setExchangeId(UUID.randomUUID().toString());
        sourceExchange.getIn().setBody("Hello from Citrus!");
        sourceExchange.getIn().setHeader("operation", "sayHello");
        sourceExchange.setProperty("foo_prop", "bar");
        sourceExchange.setVariable("foo_var", "baz");

        Message message = new DefaultMessage(sourceExchange);
        message.setHeader("additional", "Something to add");

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assert.assertEquals(exchange.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(exchange.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(exchange.getIn().getHeaders().get("additional"), "Something to add");
        Assert.assertEquals(exchange.getProperty("foo_prop"), "bar");
        Assert.assertEquals(exchange.getVariable("foo_var"), "baz");
    }

    @Test
    public void testConvertOutboundUserProvidedExchange() {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");
        exchange.setProperty("foo_prop", "bar");
        exchange.setVariable("foo_var", "baz");

        Message message = new DefaultMessage(exchange);

        Exchange converted = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assert.assertEquals(converted, exchange);
        Assert.assertEquals(converted.getIn().getBody(), "Hello from Citrus!");
        Assert.assertEquals(converted.getIn().getHeaders().get("operation"), "sayHello");
        Assert.assertEquals(converted.getProperty("foo_prop"), "bar");
        Assert.assertEquals(converted.getVariable("foo_var"), "baz");
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
        Assert.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES).toString().contains("\"VerySpecialProperty\": \"bar\""));
        Assert.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES).toString().contains("\"SpecialProperty\": \"foo\""));
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES), "{}");
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED), false);
        Assert.assertEquals(result.getHeader("operation"), "sayHello");
        Assert.assertEquals(result.getHeader("SpecialProperty"), "foo");
        Assert.assertEquals(result.getHeader("VerySpecialProperty"), "bar");
    }

    @Test
    public void testConvertInboundWithVariables() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");

        exchange.setVariable("SpecialVariable", "foo");
        exchange.setVariable("VerySpecialVariable", "bar");

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assert.assertEquals(result.getPayload(), "Hello from Citrus!");
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_ID), exchange.getExchangeId());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN), ExchangePattern.InOnly.name());
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES), "{}");
        Assert.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES).toString().contains("\"VerySpecialVariable\": \"bar\""));
        Assert.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES).toString().contains("\"SpecialVariable\": \"foo\""));
        Assert.assertEquals(result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED), false);
        Assert.assertEquals(result.getHeader("operation"), "sayHello");
        Assert.assertEquals(result.getHeader("SpecialVariable"), "foo");
        Assert.assertEquals(result.getHeader("VerySpecialVariable"), "bar");
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
