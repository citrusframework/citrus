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

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.TypeConversionUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.citrusframework.camel.message.CamelTransformMessageProcessor.Builder.transform;

@Isolated("Mutates global TypeConverter state via System properties")
public class CamelTransformMessageProcessorTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    private final CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    public void setupTypeConverter() {
        System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, "camel");
        TypeConversionUtils.loadDefaultConverter();
    }

    @AfterAll
    public static void restoreTypeConverterDefault() {
        System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, CitrusSettings.TYPE_CONVERTER_DEFAULT);
        TypeConversionUtils.loadDefaultConverter();
    }

    @Test
    public void shouldSetBodyFromConstantExpression() {
        CamelTransformMessageProcessor messageProcessor = transform()
                .constant("Hello from Camel!")
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals("Hello from Camel!", in.getPayload(String.class));
        Assertions.assertEquals("sayHello", in.getHeader("operation"));
    }

    @Test
    public void shouldSetBodyFromSimpleBodyExpression() {
        CamelTransformMessageProcessor messageProcessor = transform()
                .simple("${body.text}")
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage(new Pojo())
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals("Hello from Camel!", in.getPayload(String.class));
        Assertions.assertEquals("sayHello", in.getHeader("operation"));
    }

    @Test
    public void shouldSetBodyFromBeanExpression() {
        CamelTransformMessageProcessor messageProcessor = transform()
                .method(new Pojo())
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals("Hello from Camel!", in.getPayload(String.class));
        Assertions.assertEquals("sayHelloFromCamel", in.getHeader("operation"));
    }

    @Test
    public void shouldSetBodyFromHeaderExpression() {
        CamelTransformMessageProcessor messageProcessor = transform()
                .header("operation")
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage()
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals("sayHello", in.getPayload(String.class));
        Assertions.assertEquals("sayHello", in.getHeader("operation"));
    }

    @Test
    public void shouldHandleExchangeException() {

        CamelTransformMessageProcessor messageProcessor = transform()
                .method(new Pojo(), "fail")
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage()
                .setHeader("operation", "sayHello");
        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> messageProcessor.process(in, context));

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches("Failed to process message.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    private static final class Pojo implements Processor {
        public String getText() {
            return "Hello from Camel!";
        }

        @SuppressWarnings("deprecation")
        public void process(Exchange exchange) {
            exchange.getMessage().setBody(getText());
            exchange.getMessage().setHeader("operation", "sayHelloFromCamel");
            exchange.setOut(exchange.getIn());
        }

        public void fail(Exchange exchange) {
            exchange.setException(new CitrusRuntimeException("FAIL!"));
        }
    }
}
