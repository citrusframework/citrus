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

package org.citrusframework.base.validation.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourceHeaderDataBuilder;
import org.citrusframework.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultMessageBuilderTest extends UnitTestSupport {

    private final String variablePayloadResource = "classpath:org/citrusframework/base/validation/builder/variable-data-resource.txt";
    private final String initialVariableTestPayload = "{ \"person\": { \"name\": \"${name}\", \"age\": 20} }";
    private final String resultingVariableTestPayload = "{ \"person\": { \"name\": \"Frauke\", \"age\": 20} }";

    private DefaultMessageBuilder messageBuilder;

    @BeforeEach
    public void setUp() {
        messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("TestMessagePayload"));

        context.getMessageProcessors().setMessageProcessors(Collections.emptyList());
    }

    @Test
    public void testMessageBuilder() {
        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
    }

    @Test
    public void testMessageBuilderVariableSupport() {
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("This ${placeholder} contains variables!"));
        context.setVariable("placeholder", "payload data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("This payload data contains variables!", resultingMessage.getPayload());
    }

    @Test
    public void testMessageBuilderWithPayloadResource() {
        String textPayloadResource = "classpath:org/citrusframework/base/validation/builder/payload-data-resource.txt";

        messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder(textPayloadResource));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessageData", resultingMessage.getPayload());
    }

    @Test
    public void testMessageBuilderWithPayloadResourceVariableSupport() {
        messageBuilder = new DefaultMessageBuilder();

        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder(variablePayloadResource));
        context.setVariable("placeholder", "payload data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("This payload data contains variables!", resultingMessage.getPayload());
    }

    @Test
    public void testMessageBuilderWithHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", "unitTesting");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertNotNull(resultingMessage.getHeader("operation"));
        assertEquals("unitTesting", resultingMessage.getHeader("operation"));
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

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertNotNull(resultingMessage.getHeader("intValue"));
        assertEquals(5, resultingMessage.getHeader("intValue"));
        assertNotNull(resultingMessage.getHeader("longValue"));
        assertEquals(5L, resultingMessage.getHeader("longValue"));
        assertNotNull(resultingMessage.getHeader("floatValue"));
        assertEquals(5.0f, resultingMessage.getHeader("floatValue"));
        assertNotNull(resultingMessage.getHeader("doubleValue"));
        assertEquals(5.0, resultingMessage.getHeader("doubleValue"));
        assertNotNull(resultingMessage.getHeader("boolValue"));
        assertEquals(Boolean.TRUE, resultingMessage.getHeader("boolValue"));
        assertNotNull(resultingMessage.getHeader("shortValue"));
        assertEquals(Short.valueOf("5"), resultingMessage.getHeader("shortValue"));
        assertNotNull(resultingMessage.getHeader("byteValue"));
        assertEquals(Byte.valueOf("1"), resultingMessage.getHeader("byteValue"));
        assertNotNull(resultingMessage.getHeader("stringValue"));
        assertEquals("5.0", resultingMessage.getHeader("stringValue"));
    }

    @Test
    public void testMessageBuilderWithHeadersVariableSupport() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", "${operation}");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        context.setVariable("operation", "unitTesting");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertNotNull(resultingMessage.getHeader("operation"));
        assertEquals("unitTesting", resultingMessage.getHeader("operation"));
    }

    @Test
    public void testMessageBuilderWithHeaderData() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData"));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertEquals(1L, resultingMessage.getHeaderData().size());
        assertEquals("MessageHeaderData", resultingMessage.getHeaderData().get(0));
    }

    @Test
    public void testMessageBuilderWithMultipleHeaderData() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData1"));
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("MessageHeaderData2"));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertEquals(2L, resultingMessage.getHeaderData().size());
        assertEquals("MessageHeaderData1", resultingMessage.getHeaderData().get(0));
        assertEquals("MessageHeaderData2", resultingMessage.getHeaderData().get(1));
    }

    @Test
    public void testMessageBuilderWithHeaderDataVariableSupport() {
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("This ${placeholder} contains variables!"));
        context.setVariable("placeholder", "header data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertEquals(1L, resultingMessage.getHeaderData().size());
        assertEquals("This header data contains variables!", resultingMessage.getHeaderData().get(0));
    }

    @Test
    public void testMessageBuilderWithHeaderResource() {
        String headerResource = "classpath:org/citrusframework/base/validation/builder/header-data-resource.txt";
        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder(headerResource));

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertEquals(1L, resultingMessage.getHeaderData().size());
        assertEquals("MessageHeaderData", resultingMessage.getHeaderData().get(0));
    }

    @Test
    public void testMessageBuilderWithHeaderResourceVariableSupport() {
        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder(variablePayloadResource));
        context.setVariable("placeholder", "header data");

        Message resultingMessage = messageBuilder.build(context, CitrusSettings.getDefaultMessageType());

        assertEquals("TestMessagePayload", resultingMessage.getPayload());
        assertEquals(1L, resultingMessage.getHeaderData().size());
        assertEquals("This header data contains variables!", resultingMessage.getHeaderData().get(0));
    }

    @Test
    public void testVariablesInMessagePayloadsAreReplaced(){

        //GIVEN
        context.setVariable("name", "Frauke");
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(initialVariableTestPayload));

        //WHEN
        final Message message = messageBuilder.build(context, MessageType.JSON.name());

        //THEN
        assertEquals(resultingVariableTestPayload, message.getPayload());
    }
}
