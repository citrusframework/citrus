/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.validation.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourceHeaderDataBuilder;
import org.citrusframework.message.builder.FileResourcePayloadBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DefaultMessageBuilderTest extends UnitTestSupport {

    private final String variablePayloadResource = "classpath:org/citrusframework/validation/builder/variable-data-resource.txt";
    private final String initialVariableTestPayload = "{ \"person\": { \"name\": \"${name}\", \"age\": 20} }";
    private final String resultingVariableTestPayload = "{ \"person\": { \"name\": \"Frauke\", \"age\": 20} }";

    private DefaultMessageBuilder messageBuilder;

    @BeforeMethod
    public void setUp() {
        messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("TestMessagePayload"));

        context.getMessageProcessors().setMessageProcessors(Collections.emptyList());
    }

    @Test
    public void testMessageBuilder() {
        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
    }

    @Test
    public void testMessageBuilderVariableSupport() {
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("This ${placeholder} contains variables!"));
        context.setVariable("placeholder", "payload data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }

    @Test
    public void testMessageBuilderWithPayloadResource() {
        String textPayloadResource = "classpath:org/citrusframework/validation/builder/payload-data-resource.txt";

        messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder(textPayloadResource));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessageData");
    }

    @Test
    public void testMessageBuilderWithPayloadResourceVariableSupport() {
        messageBuilder = new DefaultMessageBuilder();

        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder(variablePayloadResource));
        context.setVariable("placeholder", "payload data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }

    @Test
    public void testMessageBuilderWithHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", "unitTesting");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertNotNull(resultingMessage.getHeader("operation"));
        assertEquals(resultingMessage.getHeader("operation"), "unitTesting");
    }

    @Test
    public void testMessageBuilderWithHeaderTypes() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("intValue", "{integer}:5");
        headers.put("longValue", "{long}:5");
        headers.put("floatValue", "{float}:5.0");
        headers.put("doubleValue", "{double}:5.0");
        headers.put("boolValue", "{boolean}:true");
        headers.put("shortValue", "{short}:5");
        headers.put("byteValue", "{byte}:1");
        headers.put("stringValue", "{string}:5.0");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertNotNull(resultingMessage.getHeader("intValue"));
        assertEquals(resultingMessage.getHeader("intValue"), 5);
        assertNotNull(resultingMessage.getHeader("longValue"));
        assertEquals(resultingMessage.getHeader("longValue"), 5L);
        assertNotNull(resultingMessage.getHeader("floatValue"));
        assertEquals(resultingMessage.getHeader("floatValue"), 5.0f);
        assertNotNull(resultingMessage.getHeader("doubleValue"));
        assertEquals(resultingMessage.getHeader("doubleValue"), 5.0);
        assertNotNull(resultingMessage.getHeader("boolValue"));
        assertEquals(resultingMessage.getHeader("boolValue"), Boolean.TRUE);
        assertNotNull(resultingMessage.getHeader("shortValue"));
        assertEquals(resultingMessage.getHeader("shortValue"), new Short("5"));
        assertNotNull(resultingMessage.getHeader("byteValue"));
        assertEquals(resultingMessage.getHeader("byteValue"), new Byte("1"));
        assertNotNull(resultingMessage.getHeader("stringValue"));
        assertEquals(resultingMessage.getHeader("stringValue"), "5.0");
    }

    @Test
    public void testMessageBuilderWithHeadersVariableSupport() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", "${operation}");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        context.setVariable("operation", "unitTesting");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertNotNull(resultingMessage.getHeader("operation"));
        assertEquals(resultingMessage.getHeader("operation"), "unitTesting");
    }

    @Test
    public void testMessageBuilderWithHeaderData() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData"));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }

    @Test
    public void testMessageBuilderWithMultipleHeaderData() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData1"));
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData2"));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 2L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData1");
        assertEquals(resultingMessage.getHeaderData().get(1), "MessageHeaderData2");
    }

    @Test
    public void testMessageBuilderWithHeaderDataVariableSupport() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("This ${placeholder} contains variables!"));
        context.setVariable("placeholder", "header data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
    }

    @Test
    public void testMessageBuilderWithHeaderResource() {
        String headerResource = "classpath:org/citrusframework/validation/builder/header-data-resource.txt";
        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder(headerResource));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }

    @Test
    public void testMessageBuilderWithHeaderResourceVariableSupport() {
        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder(variablePayloadResource));
        context.setVariable("placeholder", "header data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
    }

    @Test
    public void testVariablesInMessagePayloadsAreReplaced(){

        //GIVEN
        context.setVariable("name", "Frauke");
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(initialVariableTestPayload));

        //WHEN
        final Message message = messageBuilder.build(context, MessageType.JSON.name());

        //THEN
        assertEquals(message.getPayload(), resultingVariableTestPayload);
    }
}
