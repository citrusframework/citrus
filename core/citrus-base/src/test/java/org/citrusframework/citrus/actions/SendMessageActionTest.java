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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.DefaultTestCase;
import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageDirection;
import org.citrusframework.citrus.message.MessageHeaders;
import org.citrusframework.citrus.message.MessageProcessor;
import org.citrusframework.citrus.message.builder.DefaultHeaderBuilder;
import org.citrusframework.citrus.message.builder.DefaultPayloadBuilder;
import org.citrusframework.citrus.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.citrus.messaging.Producer;
import org.citrusframework.citrus.validation.DefaultMessageHeaderValidator;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.variable.MessageHeaderVariableExtractor;
import org.citrusframework.citrus.variable.dictionary.DataDictionary;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends UnitTestSupport {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessagePayloadData() {
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

		DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
		messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

		final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(testActor)
                .message(messageBuilder)
                .build();
		sendAction.execute(context);

	}

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResource() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload.xml"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadDataVariablesSupport() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>${myText}</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceVariablesSupport() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload-with-variables.xml"));

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceFunctionsSupport() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-payload-with-functions.xml"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElements() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        MessageProcessor processor = (message, context) -> {
            message.setPayload(message.getPayload(String.class).replaceAll("\\?", "Hello World!"));
        };

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .process(processor)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageHeaders() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithHeaderValuesVariableSupport() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myOperation", "sayHello");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testSendMessageWithUnknownVariableInMessagePayload() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>${myText}</Message></TestRequest>"));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    public void testSendMessageWithUnknownVariableInHeaders() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithExtractHeaderValues() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));

        Map<String, String> extractVars = new HashMap<String, String>();
        extractVars.put("Operation", "myOperation");
        extractVars.put(MessageHeaders.ID, "correlationId");

        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(extractVars)
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .process(variableExtractor)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertNotNull(context.getVariable("correlationId"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMissingMessagePayload() {
        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), new DefaultMessage(""));
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUTF16Encoding() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithISOEncoding() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUnsupportedEncoding() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<?xml version=\"1.0\" encoding=\"MyUnsupportedEncoding\"?><TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }

        verify(producer).send(any(Message.class), any(TestContext.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceISOEncoding() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/citrus/actions/test-request-iso-encoding.xml"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testDisabledSendMessage() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(disabledActor)
                .message(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

    @Test
    public void testDisabledSendMessageByEndpointActor() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(disabledActor);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

    @Test
    public void testWithExplicitDataDictionary() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        DataDictionary<String> dictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(dictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(dictionary.isGlobalScope()).thenReturn(false);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(dictionary).process(any(Message.class), eq(context));

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .dictionary(dictionary)
                .build();
        sendAction.execute(context);
    }

    @Test
    public void testWithExplicitAndGlobalDataDictionary() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        DataDictionary<String> dictionary = Mockito.mock(DataDictionary.class);
        DataDictionary<String> globalDictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(dictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(globalDictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
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

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        context.getMessageProcessors().setMessageProcessors(Collections.singletonList(globalDictionary));

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .dictionary(dictionary)
                .build();
        sendAction.execute(context);
    }

    @Test
    public void testWithGlobalDataDictionary() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        DataDictionary<String> inboundDictionary = Mockito.mock(DataDictionary.class);
        DataDictionary<String> outboundDictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(inboundDictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(outboundDictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(inboundDictionary.isGlobalScope()).thenReturn(true);
        when(outboundDictionary.isGlobalScope()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(outboundDictionary).process(any(Message.class), eq(context));

        doThrow(new CitrusRuntimeException("Unexpected call of inbound data dictionary"))
                .when(inboundDictionary).process(any(Message.class), eq(context));

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        context.getMessageProcessors().setMessageProcessors(Arrays.asList(inboundDictionary, outboundDictionary));

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .build();
        sendAction.execute(context);
    }

    private void validateMessageToSend(Message toSend, Message controlMessage) {
        Assert.assertEquals(toSend.getPayload(String.class).trim(), controlMessage.getPayload(String.class).trim());
        DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
        validator.validateMessage(toSend, controlMessage, context, new HeaderValidationContext());
    }
}
