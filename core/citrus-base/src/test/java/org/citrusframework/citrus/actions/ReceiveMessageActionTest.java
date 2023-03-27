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

package org.citrusframework.citrus.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.citrus.DefaultTestCase;
import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.context.TestContextFactory;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageDirection;
import org.citrusframework.citrus.message.MessageProcessor;
import org.citrusframework.citrus.message.MessageQueue;
import org.citrusframework.citrus.message.builder.DefaultHeaderBuilder;
import org.citrusframework.citrus.message.builder.DefaultPayloadBuilder;
import org.citrusframework.citrus.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.citrus.messaging.SelectiveConsumer;
import org.citrusframework.citrus.validation.DefaultMessageHeaderValidator;
import org.citrusframework.citrus.validation.MessageValidator;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.context.ValidationContext;
import org.citrusframework.citrus.validation.xml.XmlMessageValidationContext;
import org.citrusframework.citrus.variable.MessageHeaderVariableExtractor;
import org.citrusframework.citrus.variable.VariableExtractor;
import org.citrusframework.citrus.variable.dictionary.DataDictionary;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends UnitTestSupport {

    @Mock
    private Endpoint endpoint;
    @Mock
    private SelectiveConsumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;
    @Mock
    private DataDictionary<String> dictionary;
    @Mock
    private DataDictionary<String> globalDictionary;

    @Mock
    DataDictionary<String> inboundDictionary;
    @Mock
    DataDictionary<String> outboundDictionary;

    @Mock
    private MessageValidator<?> validator;
    @Mock
    private MessageQueue mockQueue;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        when(validator.supportsMessageType(any(String.class), any(Message.class))).thenReturn(true);

        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("validator", validator);

        factory.getReferenceResolver().bind("mockQueue", mockQueue);
        return factory;
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithEndpointUri() {
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("direct:mockQueue?timeout=15000")
                .actor(testActor)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithVariableEndpointName() {
        context.setVariable("varEndpoint", "direct:mockQueue");
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(5000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("${varEndpoint}")
                .actor(testActor)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked" })
	public void testReceiveMessageWithMessagePayloadData() {
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");


        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .actor(testActor)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
	}

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessagePayloadResource() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload.xml"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessagePayloadDataVariablesSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>${myText}</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessagePayloadResourceVariablesSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload-with-variables.xml"));

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessagePayloadResourceFunctionsSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload-with-functions.xml"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageOverwriteMessageElements() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>"));

        MessageProcessor processor = (message, context) ->
                message.setPayload(message.getPayload(String.class).replaceAll("\\?", "Hello World!"));

        Message controlMessage = new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(processor)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessageHeaders() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "sayHello");
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessageHeadersVariablesSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myOperation", "sayHello");

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithUnknownVariablesInMessageHeaders() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
        }
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithUnknownVariableInMessagePayload() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>${myText}</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
        }
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithExtractVariablesFromHeaders() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Map<String, String> headers = new HashMap<>();
        headers.put("Operation", "myOperation");

        MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(headers)
                .build();

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(headerVariableExtractor)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertEquals(context.getVariable("myOperation"), "sayHello");

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithExtractVariables() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        VariableExtractor variableExtractor = (message, context) ->
                context.setVariable("messageVar", "Hello World!");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(context, 3000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .timeout(3000L)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveSelectedWithMessageSelector() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        String messageSelector = "Operation = 'sayHello'";

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(messageSelector, context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .selector(messageSelector)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveSelectedWithMessageSelectorAndTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        String messageSelector = "Operation = 'sayHello'";

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(messageSelector, context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .timeout(5000L)
                .selector(messageSelector)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveSelectedWithMessageSelectorMap() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .selector(messageSelector)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveSelectedWithMessageSelectorMapAndTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");

        Map<String, Object> headers = new HashMap<>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .timeout(5000L)
                .message(controlMessageBuilder)
                .selector(messageSelector)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testMessageTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to receive message - message is not available");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving no message");
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveEmptyMessagePayloadAsExpected() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(""));

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testDisabledReceiveMessage() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .actor(disabledActor)
                .message(controlMessageBuilder)
                .build();
        testCase.addTestAction(receiveAction);
        testCase.execute(context);

    }

    @Test
    public void testDisabledReceiveMessageByEndpointActor() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(endpoint.getActor()).thenReturn(disabledActor);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        testCase.addTestAction(receiveAction);
        testCase.execute(context);
    }

    @Test
    public void testWithExplicitDataDictionary() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, dictionary);
        when(dictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(dictionary.isGlobalScope()).thenReturn(false);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(dictionary).process(any(Message.class), eq(context));

        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .dictionary(dictionary)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testWithExplicitAndGlobalDataDictionary() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, dictionary, globalDictionary);
        when(dictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(globalDictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(dictionary.isGlobalScope()).thenReturn(false);
        when(globalDictionary.isGlobalScope()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(globalDictionary).process(any(Message.class), eq(context));

        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(dictionary).process(any(Message.class), eq(context));

        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        context.getMessageProcessors().setMessageProcessors(Collections.singletonList(globalDictionary));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .dictionary(dictionary)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testWithGlobalDataDictionary() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, inboundDictionary, outboundDictionary);
        when(inboundDictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(outboundDictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(inboundDictionary.isGlobalScope()).thenReturn(true);
        when(outboundDictionary.isGlobalScope()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(inboundDictionary).process(any(Message.class), eq(context));

        doThrow(new CitrusRuntimeException("Unexpected call of outbound data dictionary"))
                .when(outboundDictionary).process(any(Message.class), eq(context));

        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message received = invocationOnMock.getArgument(0);
            Message control = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assert.assertEquals(received.getPayload(String.class).trim(), control.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        context.getMessageProcessors().setMessageProcessors(Arrays.asList(inboundDictionary, outboundDictionary));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }
}
