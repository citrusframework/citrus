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

import java.io.IOException;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.context.script.ScriptValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() throws IOException {
        assertActionCount(16);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("operation"));
        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(0, action.getControlMessageProcessors().size());

        Assertions.assertNull(action.getDataDictionary());
        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(0, action.getControlMessageProcessors().size());

        // 2nd action
        action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.lineSeparator()+"<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("operation"));
        assertEquals(1, messageBuilder.buildMessageHeaderData(context).size());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.lineSeparator()+"<Header xmlns=\"http://citrusframework.org/test\">"+System.lineSeparator()+"  <operation>hello</operation>"+System.lineSeparator()+"</Header>", messageBuilder.buildMessageHeaderData(context).get(0).trim());
        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(0, action.getControlMessageProcessors().size());

        Assertions.assertNull(action.getDataDictionary());
        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(0, action.getControlMessageProcessors().size());

        // 3rd action
        action = getNextTestActionFromTest();
        assertEquals(1, action.getMessageSelectorMap().size());
        assertEquals("Test", action.getMessageSelectorMap().get("operation"));
        Assertions.assertNull(action.getMessageSelector());
        assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/spring/actions/test-request-payload.xml")),
                messageBuilder.buildMessagePayload(context, action.getMessageType()));
        assertEquals(0, messageBuilder.buildMessageHeaders(context).size());
        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(0, action.getControlMessageProcessors().size());

        // 4th action
        action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        assertEquals("operation = 'Test'", action.getMessageSelector());
        assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        // 5th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getVariableExtractors().size());
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor variableExtractor = (DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(1);

        assertEquals(0L, variableExtractor.getNamespaces().size());
        assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        assertEquals(1, variableExtractor.getPathExpressions().size());
        assertEquals("text", variableExtractor.getPathExpressions().get("/TestMessage/text()"));

        Assertions.assertNotNull(action.getDataDictionary());

        // 6th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals("<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(1, action.getControlMessageProcessors().size());
        assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        assertEquals(1, messageProcessor.getPathExpressions().size());
        assertEquals("newValue", messageProcessor.getPathExpressions().get("/ns:TestMessage/"));

        Assertions.assertFalse(xmlValidationContext.isSchemaValidationEnabled());

        assertEquals(1, xmlValidationContext.getIgnoreExpressions().size());
        assertEquals("/ns:TestMessage/ns:ignore", xmlValidationContext.getIgnoreExpressions().iterator().next());
        assertEquals(1, xmlValidationContext.getNamespaces().size());
        assertEquals("http://citrusframework.org", xmlValidationContext.getNamespaces().get("ns"));

        // 7th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertEquals(2, xPathValidationContext.getXpathExpressions().size());
        assertEquals("Hello Citrus", xPathValidationContext.getXpathExpressions().get("/TestMessage/text"));
        assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        // 8th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertEquals(2, xPathValidationContext.getXpathExpressions().size());
        assertEquals("Hello Citrus", xPathValidationContext.getXpathExpressions().get("/TestMessage/text"));
        assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        // 9th action
        action = getNextTestActionFromTest();
        assertEquals(3, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof ScriptValidationContext);
        assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(1);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        assertEquals(1, xPathValidationContext.getXpathExpressions().size());
        assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        assertEquals("groovy", scriptValidationContext.getScriptType());
        assertEquals("assert true", scriptValidationContext.getValidationScript().trim());

        // 10th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof ScriptValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertEquals("groovy", scriptValidationContext.getScriptType());
        assertEquals("classpath:org/citrusframework/spring/actions/test-validation-script.groovy", scriptValidationContext.getValidationScriptResourcePath());

        // 11th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        assertEquals(2, jsonPathValidationContext.getJsonPathExpressions().size());
        assertEquals("Hello Citrus", jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"));
        assertEquals("true", jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"));

        // 12th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        assertEquals(2, jsonPathValidationContext.getJsonPathExpressions().size());
        assertEquals("Hello Citrus", jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"));
        assertEquals("true", jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"));

        // 13th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().get(0) instanceof DefaultMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        MessageValidationContext jsonValidationContext = (MessageValidationContext)action.getValidationContexts().get(0);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        assertEquals(0, action.getMessageProcessors().size());
        assertEquals(1, action.getControlMessageProcessors().size());
        assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        assertEquals(1, jsonMessageProcessor.getPathExpressions().size());
        assertEquals("newValue", jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"));

        assertEquals(1, jsonValidationContext.getIgnoreExpressions().size());
        assertEquals("$.FooMessage.bar", jsonValidationContext.getIgnoreExpressions().iterator().next());

        // 14th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getVariableExtractors().size());
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor jsonVariableExtractor = (DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(1);

        assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        assertEquals(1, jsonVariableExtractor.getPathExpressions().size());
        assertEquals("text", jsonVariableExtractor.getPathExpressions().get("$.message.text"));

        // 15th action
        action = getNextTestActionFromTest();
        assertEquals(1, action.getValidators().size());
        assertEquals(beanDefinitionContext.getBean("myValidator", MessageValidator.class), action.getValidators().get(0));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        assertEquals(1, headerValidationContext.getValidatorNames().size());
        assertEquals("myHeaderValidator", headerValidationContext.getValidatorNames().get(0));

        // 16th action
        action = getNextTestActionFromTest();
        assertEquals(2, action.getValidators().size());
        assertEquals(beanDefinitionContext.getBean("myValidator", MessageValidator.class), action.getValidators().get(0));
        assertEquals(beanDefinitionContext.getBean("defaultMessageValidator", MessageValidator.class), action.getValidators().get(1));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        assertEquals(2, headerValidationContext.getValidatorNames().size());
        assertEquals("myHeaderValidator", headerValidationContext.getValidatorNames().get(0));
        assertEquals("defaultHeaderValidator", headerValidationContext.getValidatorNames().get(1));
    }
}
