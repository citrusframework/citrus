/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.TypeConversionUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.camel.message.CamelTransformMessageProcessor.Builder.transform;

/**
 * @author Christoph Deppisch
 */
public class CamelTransformMessageProcessorTest extends AbstractTestNGUnitTest {

    private final CamelContext camelContext = new DefaultCamelContext();

    @BeforeClass
    public void setupTypeConverter() {
        System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, "camel");
        TypeConversionUtils.loadDefaultConverter();
    }

    @AfterClass(alwaysRun = true)
    public void restoreTypeConverterDefault() {
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

        Assert.assertEquals(in.getPayload(String.class), "Hello from Camel!");
        Assert.assertEquals(in.getHeader("operation"), "sayHello");
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

        Assert.assertEquals(in.getPayload(String.class), "Hello from Camel!");
        Assert.assertEquals(in.getHeader("operation"), "sayHello");
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

        Assert.assertEquals(in.getPayload(String.class), "Hello from Camel!");
        Assert.assertEquals(in.getHeader("operation"), "sayHelloFromCamel");
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

        Assert.assertEquals(in.getPayload(String.class), "sayHello");
        Assert.assertEquals(in.getHeader("operation"), "sayHello");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to process message.*")
    public void shouldHandleExchangeException() {
        CamelTransformMessageProcessor messageProcessor = transform()
                .method(new Pojo(), "fail")
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage()
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);
    }

    private static final class Pojo implements Processor {
        public String getText() {
            return "Hello from Camel!";
        }

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
