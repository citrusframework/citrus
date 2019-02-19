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

package com.consol.citrus.validation.builder;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.json.JsonMappingDataDictionary;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PayloadTemplateMessageBuilderTest extends AbstractTestNGUnitTest {

    private final String imagePayloadResource = "classpath:com/consol/citrus/validation/builder/button.png";
    private final String variablePayloadResource = "classpath:com/consol/citrus/validation/builder/variable-data-resource.txt";
    private final String initialDataDictionaryTestPayload = "{ \"person\": { \"name\": \"initial_value\", \"age\": 20} }";
    private final String resultingDataDictionaryTestPayload = "{\"person\":{\"name\":\"new_value\",\"age\":20}}";
    private final String initialVariableTestPayload = "{ \"person\": { \"name\": \"${name}\", \"age\": 20} }";
    private final String resultingVariableTestPayload = "{ \"person\": { \"name\": \"Frauke\", \"age\": 20} }";

    private PayloadTemplateMessageBuilder messageBuilder;

    @BeforeMethod
    public void setUp() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("TestMessagePayload");

        context.getGlobalMessageConstructionInterceptors().setMessageConstructionInterceptors(Collections.emptyList());
    }
    
    @Test
    public void testMessageBuilder() {
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
    }
    
    @Test
    public void testMessageBuilderVariableSupport() {
        messageBuilder.setPayloadData("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "payload data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithPayloadResource() {
        String textPayloadResource = "classpath:com/consol/citrus/validation/builder/payload-data-resource.txt";

        messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath(textPayloadResource);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessageData");
    }

    @Test
    public void testMessageBuilderWithPayloadResourceVariableSupport() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        
        messageBuilder.setPayloadResourcePath(variablePayloadResource);
        context.setVariable("placeholder", "payload data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }

    @Test
    public void testMessageBuilderWithPayloadResourceBinary() {
        messageBuilder = new PayloadTemplateMessageBuilder();

        messageBuilder.setPayloadResourcePath(imagePayloadResource);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.BINARY.name());

        assertEquals(resultingMessage.getPayload().getClass(), byte[].class);
    }

    @Test
    public void testMessageBuilderWithPayloadResourceGzip() {
        messageBuilder = new PayloadTemplateMessageBuilder();

        messageBuilder.setPayloadResourcePath(imagePayloadResource);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.GZIP.name());

        assertEquals(resultingMessage.getPayload().getClass(), byte[].class);
    }
    
    @Test
    public void testMessageBuilderWithHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", "unitTesting");
        messageBuilder.setMessageHeaders(headers);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
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
        messageBuilder.setMessageHeaders(headers);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
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
        messageBuilder.setMessageHeaders(headers);
        
        context.setVariable("operation", "unitTesting");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertNotNull(resultingMessage.getHeader("operation"));
        assertEquals(resultingMessage.getHeader("operation"), "unitTesting");
    }
    
    @Test
    public void testMessageBuilderWithHeaderData() {
        messageBuilder.getHeaderData().add("MessageHeaderData");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }

    @Test
    public void testMessageBuilderWithMultipleHeaderData() {
        messageBuilder.getHeaderData().add("MessageHeaderData1");
        messageBuilder.getHeaderData().add("MessageHeaderData2");

        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);

        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 2L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData1");
        assertEquals(resultingMessage.getHeaderData().get(1), "MessageHeaderData2");
    }
    
    @Test
    public void testMessageBuilderWithHeaderDataVariableSupport() {
        messageBuilder.getHeaderData().add("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "header data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResource() {
        String headerResource = "classpath:com/consol/citrus/validation/builder/header-data-resource.txt";
        messageBuilder.getHeaderResources().add(headerResource);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResourceVariableSupport() {
        messageBuilder.getHeaderResources().add(variablePayloadResource);
        context.setVariable("placeholder", "header data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        assertEquals(resultingMessage.getHeaderData().size(), 1L);
        assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
    }
    
    @Test
    public void testMessageBuilderInterceptor() {
        MessageConstructionInterceptor interceptor = new AbstractMessageConstructionInterceptor() {
            @Override
            public Message interceptMessage(Message message, String messageType, TestContext context) {
                message.setPayload("InterceptedMessagePayload");
                message.setHeader("NewHeader", "new");

                return message;
            }

            @Override
            public boolean supportsMessageType(String messageType) {
                return true;
            }
        };

        messageBuilder.add(interceptor);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        assertEquals(resultingMessage.getPayload(), "InterceptedMessagePayload");
        assertNotNull(resultingMessage.getHeader("NewHeader"));
    }

    @Test
    public void testMessageBuilderWithGlobalDataDictionary() {
        JsonMappingDataDictionary dataDictionary = new JsonMappingDataDictionary();
        dataDictionary.setMappings(Collections.singletonMap("person.name", "new_value"));

        context.getGlobalMessageConstructionInterceptors().setMessageConstructionInterceptors(Collections.singletonList(dataDictionary));
        messageBuilder.setPayloadData(initialDataDictionaryTestPayload);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name());

        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);
    }

    @Test
    public void testMessageBuilderWithExplicitDataDictionary() {
        JsonMappingDataDictionary dataDictionary = new JsonMappingDataDictionary();
        dataDictionary.setMappings(Collections.singletonMap("person.name", "new_value"));
        messageBuilder.setDataDictionary(dataDictionary);

        messageBuilder.setPayloadData(initialDataDictionaryTestPayload);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name());
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.INBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.OUTBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);
    }

    @Test
    public void testMessageBuilderWithGlobalAndExplicitDataDictionary() {
        JsonMappingDataDictionary globalDataDictionary = new JsonMappingDataDictionary();
        globalDataDictionary.setMappings(Collections.singletonMap("person.name", "global_value"));

        JsonMappingDataDictionary dataDictionary = new JsonMappingDataDictionary();
        dataDictionary.setMappings(Collections.singletonMap("person.name", "new_value"));

        context.getGlobalMessageConstructionInterceptors().setMessageConstructionInterceptors(Collections.singletonList(globalDataDictionary));
        messageBuilder.setDataDictionary(dataDictionary);

        messageBuilder.setPayloadData(initialDataDictionaryTestPayload);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name());
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.INBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.OUTBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);
    }

    @Test
    public void testMessageBuilderWithGlobalInboundDataDictionary() {
        JsonMappingDataDictionary dataDictionary = new JsonMappingDataDictionary();
        dataDictionary.setDirection(MessageDirection.INBOUND);
        dataDictionary.setMappings(Collections.singletonMap("person.name", "new_value"));

        context.getGlobalMessageConstructionInterceptors().setMessageConstructionInterceptors(Collections.singletonList(dataDictionary));
        messageBuilder.setPayloadData(initialDataDictionaryTestPayload);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.INBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.OUTBOUND);
        assertEquals(resultingMessage.getPayload(), initialDataDictionaryTestPayload);
    }

    @Test
    public void testMessageBuilderWithGlobalOutboundDataDictionary() {
        JsonMappingDataDictionary dataDictionary = new JsonMappingDataDictionary();
        dataDictionary.setDirection(MessageDirection.OUTBOUND);
        dataDictionary.setMappings(Collections.singletonMap("person.name", "new_value"));

        context.getGlobalMessageConstructionInterceptors().setMessageConstructionInterceptors(Collections.singletonList(dataDictionary));
        messageBuilder.setPayloadData(initialDataDictionaryTestPayload);

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.OUTBOUND);
        assertEquals(resultingMessage.getPayload(), resultingDataDictionaryTestPayload);

        resultingMessage = messageBuilder.buildMessageContent(context, MessageType.JSON.name(), MessageDirection.INBOUND);
        assertEquals(resultingMessage.getPayload(), initialDataDictionaryTestPayload);
    }

    @Test
    public void testMessagePayloadWithBinaryTargetIsConverted(){

        //GIVEN
        context.setVariable("name", "Frauke");
        messageBuilder.setPayloadData(initialVariableTestPayload);
        final byte[] expectedPayload = resultingVariableTestPayload.getBytes();

        //WHEN
        final Message message = messageBuilder.buildMessageContent(
                context, MessageType.BINARY.name(), MessageDirection.OUTBOUND);

        //THEN
        assertEquals(message.getPayload(), expectedPayload);
    }

    @Test
    public void testVariablesInMessagePayloadsAreReplaced(){

        //GIVEN
        context.setVariable("name", "Frauke");
        messageBuilder.setPayloadData(initialVariableTestPayload);

        //WHEN
        final Message message = messageBuilder.buildMessageContent(
                context, MessageType.JSON.name(), MessageDirection.OUTBOUND);

        //THEN
        assertEquals(message.getPayload(), resultingVariableTestPayload);
    }

    @Test
    public void testMessagePayloadWithGzipTargetIsConverted(){

        //GIVEN
        context.setVariable("name", "Frauke");
        messageBuilder.setPayloadData(initialVariableTestPayload);

        //prepared GZIP value of resultingVariableTestPayload
        byte[] expectedPayload = new byte[]{
                31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -85, 86, 80, 42, 72, 45, 42, -50, -49, 83, -78, 82, -88, 86, 80, -54,
                75, -52, 77, 5, -78, -108, -36, -118, 18, 75, -77, 83, -107, 116, 20, -108, 18, -45, 65, 2, 70, 6, -75,
                10, -75, 0, 4, 70, 73, 96, 44, 0, 0, 0};

        //WHEN
        final Message message = messageBuilder.buildMessageContent(
                context, MessageType.GZIP.name(), MessageDirection.OUTBOUND);

        System.out.print(Arrays.toString((byte[])message.getPayload()));

        //THEN
        assertEquals(message.getPayload(), expectedPayload);
    }
}
