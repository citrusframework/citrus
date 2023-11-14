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

package org.citrusframework.groovy.dsl;

import java.io.IOException;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.NoopMessageProcessor;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

/**
 * @author Christoph Deppisch
 */
public class SendTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    final DataDictionary<?> myDataDictionary = Mockito.mock(DataDictionary.class);

    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim().normalizeLineEndings();

    @Test
    public void shouldLoadSend() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/send.test.groovy");

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getReferenceResolver().bind("helloQueue", helloQueue);
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());

        context.getReferenceResolver().bind("jsonPathMessageProcessorBuilder", new NoopMessageProcessor.Builder());
        context.getReferenceResolver().bind("xpathMessageProcessorBuilder", new NoopMessageProcessor.Builder());

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SendTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 9L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SendMessageAction.class);

        int actionIndex = 0;

        SendMessageAction action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.build(context, MessageType.PLAINTEXT.name()).getPayload(String.class), "Hello from Citrus!");

        Message controlMessage = new DefaultMessage("Hello from Citrus!")
                                        .setHeader("operation", "sayHello");
        Message receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sayHello");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sayHello");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <Header xmlns=\"http://citrusframework.org/test\"><operation>hello</operation></Header>");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/groovy/test-request-payload.xml")).trim());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TestRequest>\n" +
                "    <Message>Hello World!</Message>\n" +
                "</TestRequest>");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");

        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assert.assertEquals(messageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(messageProcessor.getPathExpressions().get("/TestMessage/text()"), "newValue");

        Assert.assertNotNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
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
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        Assert.assertEquals(action.getMessageProcessors().size(), 0);

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("intValue" ,5)
                .setHeader("longValue" ,10L)
                .setHeader("floatValue" ,10.0F)
                .setHeader("doubleValue" ,10.0D)
                .setHeader("byteValue" , (byte) 1)
                .setHeader("shortValue" , (short) 10)
                .setHeader("boolValue" ,true)
                .setHeader("stringValue" ,"Hello Citrus");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"), "newValue");

        controlMessage = new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");

        controlMessage = new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex);
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());
    }
}
