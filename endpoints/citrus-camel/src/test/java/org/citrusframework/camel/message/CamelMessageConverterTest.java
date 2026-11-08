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
import org.citrusframework.util.StringUtils;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @since 1.4.1
 */
public class CamelMessageConverterTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final ExtendedCamelContext extendedCamelContext = Mockito.mock(ExtendedCamelContext.class);
    private final CamelMessageConverter messageConverter = new CamelMessageConverter();
    private final CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();

    @BeforeEach
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
    }

    @Test
    public void testConvertOutboundExchange() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(ExchangePattern.InOnly, exchange.getPattern());
    }

    @Test
    public void testConvertOutboundExchangeWithExchangePattern() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_PATTERN, ExchangePattern.InOut);

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(ExchangePattern.InOut, exchange.getPattern());
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(CitrusRuntimeException.class, exchange.getException().getClass());
        Assertions.assertEquals("Something went wrong", exchange.getException().getMessage());
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(CamelExchangeException.class, exchange.getException().getClass());
        Assertions.assertEquals(((CamelExchangeException) exchange.getException()).getExchange(), exchange);
        Assertions.assertEquals("Something went wrong. %s".formatted(exchange), exchange.getException().getMessage());
    }

    @Test
    public void testConvertOutboundExchangeWithException() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, new CitrusRuntimeException("Something went wrong"));

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(CitrusRuntimeException.class, exchange.getException().getClass());
        Assertions.assertEquals("Something went wrong", exchange.getException().getMessage());
    }

    @Test
    public void testConvertOutboundExchangeWithExceptionMessage() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, "Something went completely wrong");

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals(CamelException.class, exchange.getException().getClass());
        Assertions.assertEquals("Something went completely wrong", exchange.getException().getMessage());
    }

    @Test
    public void testConvertOutboundExchangeWithProperties() {
        Message message = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello")
                .setHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES, StringUtils.convertToString(Collections.singletonMap("foo_prop", "bar")));

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());

        messageConverter.convertOutbound(exchange, message, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals("bar", exchange.getProperty("foo_prop"));
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals("bar", exchange.getVariable("foo_var"));
        Assertions.assertEquals("baz", exchange.getVariable("bar_var"));
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals("Something to add", exchange.getIn().getHeaders().get("additional"));
        Assertions.assertEquals("bar", exchange.getProperty("foo_prop"));
        Assertions.assertEquals("baz", exchange.getVariable("foo_var"));
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

        Assertions.assertEquals("Hello from Citrus!", exchange.getIn().getBody());
        Assertions.assertEquals("sayHello", exchange.getIn().getHeaders().get("operation"));
        Assertions.assertEquals("Something to add", exchange.getIn().getHeaders().get("additional"));
        Assertions.assertEquals("bar", exchange.getProperty("foo_prop"));
        Assertions.assertEquals("baz", exchange.getVariable("foo_var"));
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

        Assertions.assertEquals(exchange, converted);
        Assertions.assertEquals("Hello from Citrus!", converted.getIn().getBody());
        Assertions.assertEquals("sayHello", converted.getIn().getHeaders().get("operation"));
        Assertions.assertEquals("bar", converted.getProperty("foo_prop"));
        Assertions.assertEquals("baz", converted.getVariable("foo_var"));
    }

    @Test
    public void testConvertInbound() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", result.getPayload());
        Assertions.assertEquals(exchange.getExchangeId(), result.getHeader(CamelMessageHeaders.EXCHANGE_ID));
        Assertions.assertEquals(ExchangePattern.InOnly.name(), result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN));
        Assertions.assertEquals(false, result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED));
        Assertions.assertEquals("sayHello", result.getHeader("operation"));
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

        Assertions.assertEquals("Hello from Citrus!", result.getPayload());
        Assertions.assertEquals(exchange.getExchangeId(), result.getHeader(CamelMessageHeaders.EXCHANGE_ID));
        Assertions.assertEquals(ExchangePattern.InOnly.name(), result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN));
        Assertions.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES).toString().contains("\"VerySpecialProperty\": \"bar\""));
        Assertions.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES).toString().contains("\"SpecialProperty\": \"foo\""));
        Assertions.assertEquals("{}", result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES));
        Assertions.assertEquals(false, result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED));
        Assertions.assertEquals("sayHello", result.getHeader("operation"));
        Assertions.assertEquals("foo", result.getHeader("SpecialProperty"));
        Assertions.assertEquals("bar", result.getHeader("VerySpecialProperty"));
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

        Assertions.assertEquals("Hello from Citrus!", result.getPayload());
        Assertions.assertEquals(exchange.getExchangeId(), result.getHeader(CamelMessageHeaders.EXCHANGE_ID));
        Assertions.assertEquals(ExchangePattern.InOnly.name(), result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN));
        Assertions.assertEquals("{}", result.getHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES));
        Assertions.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES).toString().contains("\"VerySpecialVariable\": \"bar\""));
        Assertions.assertTrue(result.getHeader(CamelMessageHeaders.EXCHANGE_VARIABLES).toString().contains("\"SpecialVariable\": \"foo\""));
        Assertions.assertEquals(false, result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED));
        Assertions.assertEquals("sayHello", result.getHeader("operation"));
        Assertions.assertEquals("foo", result.getHeader("SpecialVariable"));
        Assertions.assertEquals("bar", result.getHeader("VerySpecialVariable"));
    }

    @Test
    public void testConvertInboundWithException() {
        DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId(UUID.randomUUID().toString());
        exchange.getIn().setBody("Hello from Citrus!");
        exchange.getIn().setHeader("operation", "sayHello");
        exchange.setException(new CitrusRuntimeException("Something went wrong"));

        Message result = messageConverter.convertInbound(exchange, endpointConfiguration, context);

        Assertions.assertEquals("Hello from Citrus!", result.getPayload());
        Assertions.assertEquals(exchange.getExchangeId(), result.getHeader(CamelMessageHeaders.EXCHANGE_ID));
        Assertions.assertEquals(ExchangePattern.InOnly.name(), result.getHeader(CamelMessageHeaders.EXCHANGE_PATTERN));
        Assertions.assertEquals(true, result.getHeader(CamelMessageHeaders.EXCHANGE_FAILED));
        Assertions.assertEquals(CitrusRuntimeException.class.getName(), result.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION));
        Assertions.assertEquals("Something went wrong", result.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE));
        Assertions.assertEquals("sayHello", result.getHeader("operation"));
    }
}
