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

package org.citrusframework.config.xml;

import java.io.IOException;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() throws IOException {
        assertActionCount(8);
        assertActionClassAndName(SendMessageAction.class, "send");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertNull(action.getDataDictionary());

        //2nd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<Header xmlns=\"http://citrusframework.org/test\">"+System.getProperty("line.separator")+"  <operation>hello</operation>"+System.getProperty("line.separator")+"</Header>");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertNull(action.getDataDictionary());

        // 3rd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/actions/test-request-payload.xml")));
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");

        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assert.assertEquals(messageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(messageProcessor.getPathExpressions().get("/TestMessage/text()"), "newValue");

        Assert.assertNotNull(action.getDataDictionary());

        // 5th action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 8);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("intValue"), 5);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("longValue"), 10L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("floatValue"), 10.0F);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("doubleValue"), 10.0D);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("byteValue"), (byte) 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("shortValue"), (short) 10);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("boolValue"), true);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("stringValue"), "Hello Citrus");

        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(action.getMessageProcessors().size(), 0);

        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"), "newValue");

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");

        // 8th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");

    }
}
