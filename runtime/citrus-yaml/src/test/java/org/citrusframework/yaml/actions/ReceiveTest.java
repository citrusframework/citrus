/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.yaml.actions;

import java.io.IOException;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
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
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.yaml.NoopMessageProcessor;
import org.citrusframework.yaml.NoopVariableExtractor;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

/**
 * @author Christoph Deppisch
 */
public class ReceiveTest extends AbstractYamlActionTest {

    @BindToRegistry
    final DataDictionary<?> myDataDictionary = Mockito.mock(DataDictionary.class);

    @BindToRegistry
    final MessageValidator<?> myValidator = Mockito.mock(MessageValidator.class);

    @BindToRegistry
    final MessageValidator<?> defaultMessageValidator = Mockito.mock(MessageValidator.class);

    @Test
    public void shouldLoadReceive() throws IOException {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/receive-test.yaml");

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getMessageValidatorRegistry().addMessageValidator("textEqualsMessageValidator", new DefaultTextEqualsMessageValidator().enableTrim().normalizeLineEndings());
        context.getReferenceResolver().bind("helloQueue", helloQueue);
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());

        context.getReferenceResolver().bind("jsonPathVariableExtractorBuilder", new NoopVariableExtractor.Builder());
        context.getReferenceResolver().bind("xpathVariableExtractorBuilder", new NoopVariableExtractor.Builder());

        context.getReferenceResolver().bind("jsonPathMessageProcessorBuilder", new NoopMessageProcessor.Builder());
        context.getReferenceResolver().bind("xpathMessageProcessorBuilder", new NoopMessageProcessor.Builder());

        helloQueue.send(new DefaultMessage("Hello from Citrus!").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>")
                                .addHeaderData("<Header xmlns=\"http://citrusframework.org/test\"><operation>hello</operation></Header>")
                                .setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TestRequest>\n" +
                "    <Message>Hello World!</Message>\n" +
                "</TestRequest>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("{ \"message\": { \"text\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ReceiveTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 17L);

        int actionIndex = 0;

        ReceiveMessageAction action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(action.getReceiveTimeout(), 10000L);
        Assert.assertEquals(action.getMessageBuilder().build(context, MessageType.PLAINTEXT.name()).getPayload(String.class), "Hello from Citrus!");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sayHello");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sayHello");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<Header xmlns=\"http://citrusframework.org/test\"><operation>hello</operation></Header>");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "sayHello");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/yaml/test-request-payload.xml")));
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
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

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
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
        Assert.assertEquals(xmlValidationContext.getNamespaces().get("ctx"), "http://citrusframework.org/test");
        Assert.assertEquals(xmlValidationContext.getControlNamespaces().size(), 1);
        Assert.assertEquals(xmlValidationContext.getControlNamespaces().get("ns"), "http://citrusframework.org");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof ScriptValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        ScriptValidationContext scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 1);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScript().trim(), "assert true");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof ScriptValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof HeaderValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(xmlValidationContext.isSchemaValidationEnabled());

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScriptResourcePath(), "classpath:org/citrusframework/yaml/test-validation-script.groovy");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonPathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof HeaderValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonPathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof HeaderValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        JsonMessageValidationContext jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(1);

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

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor jsonVariableExtractor = (DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(1);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().get("$.message.text"), "text");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getValidators().size(), 1);
        Assert.assertEquals(action.getValidators().get(0), context.getReferenceResolver().resolve("myValidator", MessageValidator.class));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 1);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");

        action = (ReceiveMessageAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getValidators().size(), 2);
        Assert.assertEquals(action.getValidators().get(0), context.getReferenceResolver().resolve("myValidator", MessageValidator.class));
        Assert.assertEquals(action.getValidators().get(1), context.getReferenceResolver().resolve("defaultMessageValidator", MessageValidator.class));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 2);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(1), "defaultHeaderValidator");
    }
}
