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

package com.consol.citrus.xml.actions;

import java.io.IOException;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.DefaultMessageQueue;
import com.consol.citrus.message.DelegatingPathExpressionProcessor;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.TextEqualsMessageValidator;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.consol.citrus.xml.NoopMessageProcessor;
import com.consol.citrus.xml.XmlTestLoader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.endpoint.direct.DirectEndpoints.direct;

/**
 * @author Christoph Deppisch
 */
public class SendTest extends AbstractXmlActionTest {

    @BindToRegistry
    final DataDictionary<?> myDataDictionary = Mockito.mock(DataDictionary.class);

    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();
    private final TextEqualsMessageValidator validator = new TextEqualsMessageValidator().enableTrim();

    @Test
    public void shouldLoadSend() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/xml/actions/send-test.xml");

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

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + System.lineSeparator() + "<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sayHello");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + System.lineSeparator() + "<Header xmlns=\"http://citrusframework.org/test\">" + System.lineSeparator() + "  <operation>hello</operation>" + System.lineSeparator() + "</Header>");
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        Assert.assertNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + System.lineSeparator() + "<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()),
                FileUtils.readToString(FileUtils.getFileResource("classpath:com/consol/citrus/xml/test-request-payload.xml")));
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0);
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");

        controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
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
