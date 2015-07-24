/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() {
        assertActionCount(7);
        assertActionClassAndName(SendMessageAction.class, "send");
        
        PayloadTemplateMessageBuilder messageBuilder;
        GroovyScriptMessageBuilder groovyMessageBuilder;
        
        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "Test");
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertNull(action.getDataDictionary());

        // 2nd action
        action = getNextTestActionFromTest();
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNotNull(messageBuilder.getPayloadResourcePath());
        Assert.assertEquals(messageBuilder.getPayloadResourcePath(), "classpath:com/consol/citrus/actions/test-request-payload.xml");
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0);
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 3rd action
        action = getNextTestActionFromTest();
        groovyMessageBuilder = (GroovyScriptMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertNotNull(groovyMessageBuilder.getScriptData());
        Assert.assertEquals(groovyMessageBuilder.getScriptData().trim(), "println '<TestMessage>Hello Citrus</TestMessage>'");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().size(), 2);
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header1"), "Test");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header2"), "Test");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 4th action
        action = getNextTestActionFromTest();
        groovyMessageBuilder = (GroovyScriptMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNotNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertEquals(groovyMessageBuilder.getScriptResourcePath(), "classpath:com/consol/citrus/script/example.groovy");
        Assert.assertNull(groovyMessageBuilder.getScriptData());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();

        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 1);
        Assert.assertTrue(messageBuilder.getMessageInterceptors().get(0) instanceof XpathMessageConstructionInterceptor);
        XpathMessageConstructionInterceptor messageConstructionInterceptor = (XpathMessageConstructionInterceptor)messageBuilder.getMessageInterceptors().get(0);
        
        Assert.assertEquals(messageConstructionInterceptor.getXPathExpressions().size(), 1);
        Assert.assertEquals(messageConstructionInterceptor.getXPathExpressions().get("/TestMessage/text()"), "newValue");

        Assert.assertNotNull(action.getDataDictionary());

        // 6th action
        action = getNextTestActionFromTest();
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 8);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("intValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.INTEGER.getName() + MessageHeaderType.TYPE_SUFFIX + "5");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("longValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.LONG.getName() + MessageHeaderType.TYPE_SUFFIX + "10");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("floatValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.FLOAT.getName() + MessageHeaderType.TYPE_SUFFIX + "10.0");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("doubleValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.DOUBLE.getName() + MessageHeaderType.TYPE_SUFFIX + "10.0");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("byteValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.BYTE.getName() + MessageHeaderType.TYPE_SUFFIX + "1");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("shortValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.SHORT.getName() + MessageHeaderType.TYPE_SUFFIX + "10");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("boolValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.BOOLEAN.getName() + MessageHeaderType.TYPE_SUFFIX + "true");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("stringValue"), MessageHeaderType.TYPE_PREFIX + MessageHeaderType.STRING.getName() + MessageHeaderType.TYPE_SUFFIX + "Hello Citrus");

        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");
        
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();

        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 1);
        Assert.assertTrue(messageBuilder.getMessageInterceptors().get(0) instanceof JsonPathMessageConstructionInterceptor);
        JsonPathMessageConstructionInterceptor jsonMessageConstructionInterceptor = (JsonPathMessageConstructionInterceptor)messageBuilder.getMessageInterceptors().get(0);

        Assert.assertEquals(jsonMessageConstructionInterceptor.getJsonPathExpressions().size(), 1);
        Assert.assertEquals(jsonMessageConstructionInterceptor.getJsonPathExpressions().get("$.FooMessage.foo"), "newValue");
    }
}
