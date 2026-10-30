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

package org.citrusframework.spring.config.xml;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SendMessageActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() throws IOException {
        assertActionCount(8);
        assertActionClassAndName(SendMessageAction.class, "send");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        Assertions.assertNull(action.getDataDictionary());

        //2nd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.lineSeparator()+"<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.lineSeparator()+"<Header xmlns=\"http://citrusframework.org/test\">"+System.lineSeparator()+"  <operation>hello</operation>"+System.lineSeparator()+"</Header>", messageBuilder.buildMessageHeaderData(context).get(0).trim());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        Assertions.assertNull(action.getDataDictionary());

        // 3rd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals(FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/spring/actions/test-request-payload.xml")),
                messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        // 4th action
        action = getNextTestActionFromTest();
        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);

        Assertions.assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        Assertions.assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assertions.assertEquals(1, messageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", messageProcessor.getPathExpressions().get("/TestMessage/text()"));

        Assertions.assertNotNull(action.getDataDictionary());

        // 5th action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(8, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(5, messageBuilder.buildMessageHeaders(context).get("intValue"));
        Assertions.assertEquals(10L, messageBuilder.buildMessageHeaders(context).get("longValue"));
        Assertions.assertEquals(10.0F, messageBuilder.buildMessageHeaders(context).get("floatValue"));
        Assertions.assertEquals(10.0D, messageBuilder.buildMessageHeaders(context).get("doubleValue"));
        Assertions.assertEquals((byte) 1, messageBuilder.buildMessageHeaders(context).get("byteValue"));
        Assertions.assertEquals((short) 10, messageBuilder.buildMessageHeaders(context).get("shortValue"));
        Assertions.assertEquals(true, messageBuilder.buildMessageHeaders(context).get("boolValue"));
        Assertions.assertEquals("Hello Citrus", messageBuilder.buildMessageHeaders(context).get("stringValue"));

        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        Assertions.assertEquals(0, action.getMessageProcessors().size());

        // 6th action
        action = getNextTestActionFromTest();
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assertions.assertEquals(1, jsonMessageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"));

        // 7th action
        action = getNextTestActionFromTest();
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());

        // 8th action
        action = getNextTestActionFromTest();
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());

    }
}
