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

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() throws IOException {
        assertActionCount(16);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "Test");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+System.getProperty("line.separator")+"<Header xmlns=\"http://citrusframework.org/test\">"+System.getProperty("line.separator")+"  <operation>hello</operation>"+System.getProperty("line.separator")+"</Header>");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/actions/test-request-payload.xml")));
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertEquals(action.getMessageSelector(), "operation = 'Test'");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor variableExtractor = (DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(1);

        Assert.assertEquals(variableExtractor.getNamespaces().size(), 0L);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(variableExtractor.getPathExpressions().size(), 1);
        Assert.assertEquals(variableExtractor.getPathExpressions().get("/TestMessage/text()"), "text");

        Assert.assertNotNull(action.getDataDictionary());

        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>");

        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 1);
        Assert.assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        Assert.assertEquals(messageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(messageProcessor.getPathExpressions().get("/ns:TestMessage/"), "newValue");

        Assert.assertFalse(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().size(), 1);
        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().iterator().next(), "/ns:TestMessage/ns:ignore");
        Assert.assertEquals(xmlValidationContext.getNamespaces().size(), 1);
        Assert.assertEquals(xmlValidationContext.getNamespaces().get("ns"), "http://citrusframework.org");

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 8th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 9th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 5);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(4) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        ScriptValidationContext scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(4);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 1);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScript().trim(), "assert true");

        // 10th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScriptResourcePath(), "classpath:org/citrusframework/actions/test-validation-script.groovy");

        // 11th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonPathMessageValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 12th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonPathMessageValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 13th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        JsonMessageValidationContext jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(2);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 1);
        Assert.assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"), "newValue");

        Assert.assertEquals(jsonValidationContext.getIgnoreExpressions().size(), 1);
        Assert.assertEquals(jsonValidationContext.getIgnoreExpressions().iterator().next(), "$.FooMessage.bar");

        // 14th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor jsonVariableExtractor = (DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(1);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().get("$.message.text"), "text");

        // 15th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidators().size(), 1);
        Assert.assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 1);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");

        // 16th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidators().size(), 2);
        Assert.assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        Assert.assertEquals(action.getValidators().get(1), beanDefinitionContext.getBean("defaultMessageValidator", MessageValidator.class));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 2);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(1), "defaultHeaderValidator");
    }
}
