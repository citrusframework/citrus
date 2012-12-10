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

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.Message;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;

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
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
    }
    
    @Test
    public void testMessageBuilderVariableSupport() {
        messageBuilder.setPayloadData("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "payload data");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithPayloadResource() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        
        messageBuilder.setPayloadResource("classpath:com/consol/citrus/validation/builder/payload-data-resource.txt");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessageData");
    }
    
    @Test
    public void testMessageBuilderWithPayloadResourceVariableSupport() {
        messageBuilder = new PayloadTemplateMessageBuilder();
        
        messageBuilder.setPayloadResource("classpath:com/consol/citrus/validation/builder/variable-data-resource.txt");
        context.setVariable("placeholder", "payload data");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "This payload data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithHeaders() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operation", "unitTesting");
        messageBuilder.setMessageHeaders(headers);
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("operation"));
        Assert.assertEquals(resultingMessage.getHeaders().get("operation"), "unitTesting");
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
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("intValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("intValue"), new Integer(5));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("longValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("longValue"), new Long(5));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("floatValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("floatValue"), new Float(5.0f));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("doubleValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("doubleValue"), new Double(5.0));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("boolValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("boolValue"), new Boolean(true));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("shortValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("shortValue"), new Short("5"));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("byteValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("byteValue"), new Byte("1"));
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("stringValue"));
        Assert.assertEquals(resultingMessage.getHeaders().get("stringValue"), new String("5.0"));
    }
    
    @Test
    public void testMessageBuilderWithHeadersVariableSupport() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operation", "${operation}");
        messageBuilder.setMessageHeaders(headers);
        
        context.setVariable("operation", "unitTesting");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey("operation"));
        Assert.assertEquals(resultingMessage.getHeaders().get("operation"), "unitTesting");
    }
    
    @Test
    public void testMessageBuilderWithHeaderData() {
        messageBuilder.setMessageHeaderData("MessageHeaderData");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey(CitrusMessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(resultingMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), "MessageHeaderData");
    }
    
    @Test
    public void testMessageBuilderWithHeaderDataVariableSupport() {
        messageBuilder.setMessageHeaderData("This ${placeholder} contains variables!");
        context.setVariable("placeholder", "header data");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey(CitrusMessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(resultingMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), "This header data contains variables!");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResource() {
        messageBuilder.setMessageHeaderResource("classpath:com/consol/citrus/validation/builder/header-data-resource.txt");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey(CitrusMessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(resultingMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), "MessageHeaderData");
    }
    
    @Test
    public void testMessageBuilderWithHeaderResourceVariableSupport() {
        messageBuilder.setMessageHeaderResource("classpath:com/consol/citrus/validation/builder/variable-data-resource.txt");
        context.setVariable("placeholder", "header data");
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "TestMessagePayload");
        Assert.assertTrue(resultingMessage.getHeaders().containsKey(CitrusMessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(resultingMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), "This header data contains variables!");
    }
    
    @Test
    public void testMessageBuilderInterceptor() {
        MessageConstructionInterceptor<String> interceptor = new MessageConstructionInterceptor<String>() {
            public Message<String> interceptMessageConstruction(
                    Message<String> message, TestContext context) {
                return message;
            }

            public String interceptMessageConstruction(String messagePayload,
                    TestContext context) {
                return "InterceptedMessagePayload";
            }
        };
        messageBuilder.addMessageConstructingInterceptor(interceptor);
        
        Message<String> resultingMessage = messageBuilder.buildMessageContent(context);
        
        Assert.assertEquals(resultingMessage.getPayload(), "InterceptedMessagePayload");
    }
}
