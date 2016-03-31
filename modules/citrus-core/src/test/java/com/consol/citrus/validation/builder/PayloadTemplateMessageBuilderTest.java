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
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class PayloadTemplateMessageBuilderTest extends AbstractTestNGUnitTest {
    
    private PayloadTemplateMessageBuilder messageBuilder;
    
    @BeforeMethod
    public void prepareMessageBuilder() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("TestMessagePayload");
    }
    
    @Test
    public void testMessageBuilder() {
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
    }
    
    @Test
    public void testMessageBuilderVariableSupport() {
        messageBuilder.setPayloadData("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "payload data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithPayloadResource() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/validation/builder/payload-data-resource.txt");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessageData");
    }

    @Test
    public void testMessageBuilderWithPayloadResourceVariableSupport() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/validation/builder/variable-data-resource.txt");
        context.setVariable("placeholder", "payload data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }

    @Test
    public void testMessageBuilderWithPayloadResourceBinary() {
        messageBuilder = new PayloadTemplateMessageBuilder();

        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/validation/builder/button.png");

        Message resultingMessage = messageBuilder.buildMessageContent(context, MessageType.BINARY.name());

        Assert.assertEquals(resultingMessage.getPayload().getClass(), byte[].class);
    }
    
    @Test
    public void testMessageBuilderWithHeaders() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operation", "unitTesting");
        messageBuilder.setMessageHeaders(headers);
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertNotNull(resultingMessage.getHeader("operation"));
        Assert.assertEquals(resultingMessage.getHeader("operation"), "unitTesting");
    }
    
    @Test
    public void testMessageBuilderWithHeaderTypes() {
        Map<String, Object> headers = new HashMap<String, Object>();
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
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertNotNull(resultingMessage.getHeader("intValue"));
        Assert.assertEquals(resultingMessage.getHeader("intValue"), new Integer(5));
        Assert.assertNotNull(resultingMessage.getHeader("longValue"));
        Assert.assertEquals(resultingMessage.getHeader("longValue"), new Long(5));
        Assert.assertNotNull(resultingMessage.getHeader("floatValue"));
        Assert.assertEquals(resultingMessage.getHeader("floatValue"), new Float(5.0f));
        Assert.assertNotNull(resultingMessage.getHeader("doubleValue"));
        Assert.assertEquals(resultingMessage.getHeader("doubleValue"), new Double(5.0));
        Assert.assertNotNull(resultingMessage.getHeader("boolValue"));
        Assert.assertEquals(resultingMessage.getHeader("boolValue"), new Boolean(true));
        Assert.assertNotNull(resultingMessage.getHeader("shortValue"));
        Assert.assertEquals(resultingMessage.getHeader("shortValue"), new Short("5"));
        Assert.assertNotNull(resultingMessage.getHeader("byteValue"));
        Assert.assertEquals(resultingMessage.getHeader("byteValue"), new Byte("1"));
        Assert.assertNotNull(resultingMessage.getHeader("stringValue"));
        Assert.assertEquals(resultingMessage.getHeader("stringValue"), new String("5.0"));
    }
    
    @Test
    public void testMessageBuilderWithHeadersVariableSupport() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operation", "${operation}");
        messageBuilder.setMessageHeaders(headers);
        
        context.setVariable("operation", "unitTesting");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertNotNull(resultingMessage.getHeader("operation"));
        Assert.assertEquals(resultingMessage.getHeader("operation"), "unitTesting");
    }
    
    @Test
    public void testMessageBuilderWithHeaderData() {
        messageBuilder.getHeaderData().add("MessageHeaderData");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertEquals(resultingMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }

    @Test
    public void testMessageBuilderWithMultipleHeaderData() {
        messageBuilder.getHeaderData().add("MessageHeaderData1");
        messageBuilder.getHeaderData().add("MessageHeaderData2");

        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);

        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertEquals(resultingMessage.getHeaderData().size(), 2L);
        Assert.assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData1");
        Assert.assertEquals(resultingMessage.getHeaderData().get(1), "MessageHeaderData2");
    }
    
    @Test
    public void testMessageBuilderWithHeaderDataVariableSupport() {
        messageBuilder.getHeaderData().add("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "header data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertEquals(resultingMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResource() {
        messageBuilder.getHeaderResources().add("classpath:com/consol/citrus/validation/builder/header-data-resource.txt");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertEquals(resultingMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(resultingMessage.getHeaderData().get(0), "MessageHeaderData");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResourceVariableSupport() {
        messageBuilder.getHeaderResources().add("classpath:com/consol/citrus/validation/builder/variable-data-resource.txt");
        context.setVariable("placeholder", "header data");
        
        Message resultingMessage = messageBuilder.buildMessageContent(context, Citrus.DEFAULT_MESSAGE_TYPE);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertEquals(resultingMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(resultingMessage.getHeaderData().get(0), "This header data contains variables!");
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
        
        Assert.assertEquals(resultingMessage.getPayload(), "InterceptedMessagePayload");
        Assert.assertNotNull(resultingMessage.getHeader("NewHeader"));
    }
}
