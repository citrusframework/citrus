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

package org.citrusframework.config.xml;

import java.io.IOException;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.DefaultScriptValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() throws IOException {
        assertActionCount(16);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 0);

        // 2nd action
        action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<Header xmlns=\"http://citrusframework.org/test\">"+System.getProperty("line.separator")+"  <operation>hello</operation>"+System.getProperty("line.separator")+"</Header>");
        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 0);

        // 3rd action
        action = getNextTestActionFromTest();
        assertEquals(action.getMessageSelectorMap().size(), 1);
        assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
        Assert.assertNull(action.getMessageSelector());
        assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/actions/test-request-payload.xml")));
        assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 0);

        // 4th action
        action = getNextTestActionFromTest();
        assertTrue(action.getMessageSelectorMap().isEmpty());
        assertEquals(action.getMessageSelector(), "operation = 'Test'");
        assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 5th action
        action = getNextTestActionFromTest();
        assertEquals(action.getVariableExtractors().size(), 2);
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor variableExtractor = (DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(1);

        assertEquals(variableExtractor.getNamespaces().size(), 0L);
        assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        assertEquals(variableExtractor.getPathExpressions().size(), 1);
        assertEquals(variableExtractor.getPathExpressions().get("/TestMessage/text()"), "text");

        Assert.assertNotNull(action.getDataDictionary());

        // 6th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>");

        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 1);
        assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        assertEquals(messageProcessor.getPathExpressions().size(), 1);
        assertEquals(messageProcessor.getPathExpressions().get("/ns:TestMessage/"), "newValue");

        Assert.assertFalse(xmlValidationContext.isSchemaValidationEnabled());

        assertEquals(xmlValidationContext.getIgnoreExpressions().size(), 1);
        assertEquals(xmlValidationContext.getIgnoreExpressions().iterator().next(), "/ns:TestMessage/ns:ignore");
        assertEquals(xmlValidationContext.getNamespaces().size(), 1);
        assertEquals(xmlValidationContext.getNamespaces().get("ns"), "http://citrusframework.org");

        // 7th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 8th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 9th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 3);
        assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof ScriptValidationContext);
        assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        assertEquals(xPathValidationContext.getXpathExpressions().size(), 1);
        assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        assertEquals(scriptValidationContext.getScriptType(), "groovy");
        assertEquals(scriptValidationContext.getValidationScript().trim(), "assert true");

        // 10th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof ScriptValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertEquals(scriptValidationContext.getScriptType(), "groovy");
        assertEquals(scriptValidationContext.getValidationScriptResourcePath(), "classpath:org/citrusframework/actions/test-validation-script.groovy");

        // 11th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 12th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 13th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().get(0) instanceof DefaultMessageValidationContext);
        assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        MessageValidationContext jsonValidationContext = (MessageValidationContext)action.getValidationContexts().get(0);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        assertEquals(action.getMessageProcessors().size(), 0);
        assertEquals(action.getControlMessageProcessors().size(), 1);
        assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        assertEquals(jsonMessageProcessor.getPathExpressions().size(), 1);
        assertEquals(jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"), "newValue");

        assertEquals(jsonValidationContext.getIgnoreExpressions().size(), 1);
        assertEquals(jsonValidationContext.getIgnoreExpressions().iterator().next(), "$.FooMessage.bar");

        // 14th action
        action = getNextTestActionFromTest();
        assertEquals(action.getVariableExtractors().size(), 2);
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor jsonVariableExtractor = (DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(1);

        assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        assertEquals(jsonVariableExtractor.getPathExpressions().size(), 1);
        assertEquals(jsonVariableExtractor.getPathExpressions().get("$.message.text"), "text");

        // 15th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidators().size(), 1);
        assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        assertEquals(headerValidationContext.getValidatorNames().size(), 1);
        assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");

        // 16th action
        action = getNextTestActionFromTest();
        assertEquals(action.getValidators().size(), 2);
        assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        assertEquals(action.getValidators().get(1), beanDefinitionContext.getBean("defaultMessageValidator", MessageValidator.class));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        assertEquals(headerValidationContext.getValidatorNames().size(), 2);
        assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");
        assertEquals(headerValidationContext.getValidatorNames().get(1), "defaultHeaderValidator");
    }
}
