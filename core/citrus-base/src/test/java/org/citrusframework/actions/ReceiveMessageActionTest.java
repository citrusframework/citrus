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

package org.citrusframework.actions;

import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageStore;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.util.TestUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.citrusframework.CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_ENV;
import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XHTML;
import static org.citrusframework.message.MessageType.XML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Isolated("Replaces the JVM-wide environment variables, which every CitrusSettings lookup reads")
@SuppressWarnings("unchecked")
@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @Override
    protected TestContextFactory createTestContextFactory() {
        when(validator.supportsMessageType(any(String.class), any(Message.class))).thenReturn(true);

        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("validator", validator);

        factory.getReferenceResolver().bind("mockQueue", mockQueue);
        return factory;
    }

    @Test
    public void testReceiveMessageWithEndpointUri() {
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveMessageWithVariableEndpointName() {
        context.setVariable("varEndpoint", "direct:mockQueue");
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(5000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveMessageWithMessagePayloadResource() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/actions/test-request-payload.xml"));

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()));
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testReceiveMessageWithMessagePayloadResourceVariablesSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/actions/test-request-payload-with-variables.xml"));

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()));
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testReceiveMessageWithMessagePayloadResourceFunctionsSupport() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:org/citrusframework/actions/test-request-payload-with-functions.xml"));

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()));
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assertions.assertEquals("Unknown variable 'myOperation'", e.getMessage());
        }
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assertions.assertEquals("Unknown variable 'myText'", e.getMessage());
        }
    }

    @Test
    public void testReceiveMessageWithExtractVariablesFromHeaders() {
        String headerKey = "Operation";
        String headerValue = "sayHello";

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of(headerKey, headerValue)));

        String extractionKey = "myOperation";
        MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(Map.of(headerKey, extractionKey))
                .build();

        Map<String, Object> controlHeaders = new HashMap<>();
        controlHeaders.put(headerKey, headerValue);
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(headerVariableExtractor)
                .build();
        receiveAction.execute(context);

        Assertions.assertNotNull(context.getVariable(extractionKey));
        Assertions.assertEquals(headerValue, context.getVariable(extractionKey));
    }

    @Test
    public void testReceiveMessageWithExtractVariablesFromHeadersInvalidKey() {
        String headerKey = "Operation";
        String headerValue = "sayHello";

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of(headerKey, headerValue)));

        String extractionKey = "myOperation";
        MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(Map.of("invalid-header-key", extractionKey))
                .build();

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(headerVariableExtractor)
                .build();

        UnknownElementException exception = assertThrows(UnknownElementException.class, () -> receiveAction.execute(context));
        assertEquals("Could not find header element invalid-header-key in received header", exception.getMessage());
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assertions.assertNotNull(context.getVariable("messageVar"));
        Assertions.assertEquals("Hello World!", context.getVariable("messageVar"));
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveSelectedWithMessageSelector() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of("Operation", "sayHello")));

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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveSelectedWithMessageSelectorAndTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of("Operation", "sayHello")));

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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveSelectedWithMessageSelectorMap() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of("Operation", "sayHello")));

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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
    public void testReceiveSelectedWithMessageSelectorMapAndTimeout() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of("Operation", "sayHello")));

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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Map.of("Operation", "sayHello")));

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        doAnswer(invocationOnMock -> {
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        try {
            receiveAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assertions.assertEquals("Failed to receive message - message is not available", e.getMessage());
            return;
        }

        Assertions.fail("Missing " + CitrusRuntimeException.class + " for receiving no message");
    }

    @Test
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
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
            Message control = invocationOnMock.getArgument(0);
            Message received = invocationOnMock.getArgument(1);
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);

            Assertions.assertEquals(control.getPayload(String.class).trim(), received.getPayload(String.class).trim());
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        context.getMessageProcessors().setMessageProcessors(Arrays.asList(inboundDictionary, outboundDictionary));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @DisplayName("""
                    With EXCLUSIVE message validation strategy,
                    default validators should still be invoked, whenever NO custom validator is present.

                    Requirement: https://github.com/citrusframework/citrus/issues/1419
                    """)
    public void testReceiveMessage_shouldInvokeValidationContexts_whenNoCustomValidatorIsPresent() {
        var testActor = new TestActor();
        testActor.setName("TESTACTOR");

        var controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);
            assertThat(validationContextList)
                    .satisfiesExactly(
                            validationContext -> assertThat(validationContext)
                                    .isInstanceOf(HeaderValidationContext.class)
                    );
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));

            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("direct:mockQueue?timeout=15000")
                .actor(testActor)
                .build();

        assertThatCode(() -> receiveAction.execute(context))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("""
                    With EXCLUSIVE message validation strategy,
                    default validators will be skipped, whenever a custom validator is present.

                    Requirement: https://github.com/citrusframework/citrus/issues/1419
                    """)
    public void testReceiveMessage_shouldIgnoreValidationContexts_whenCustomValidatorIsPresent_andValidationStrategyIsExclusive() {
        var testActor = new TestActor();
        testActor.setName("TESTACTOR");

        var controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        var customValidatorInvoked = new AtomicBoolean(false);
        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("direct:mockQueue?timeout=15000")
                .actor(testActor)
                .validate(
                        (message, testContext) -> customValidatorInvoked.set(true)
                )
                .build();

        assertThatCode(() -> receiveAction.execute(context))
                .doesNotThrowAnyException();

        assertThat(customValidatorInvoked)
                .isTrue();

        verifyNoInteractions(validator);
    }

    @Test
    @DisplayName("""
                    With COMBINED message validation strategy,
                    BOTH custom and default validators should be executed.

                    Requirement: https://github.com/citrusframework/citrus/issues/1419
                    """)
    public void testReceiveMessage_shouldInvokeBothCustomValidator_andValidationContexts_whenValidationStrategyIsCombined() {
        environmentVariables.set(CUSTOM_VALIDATOR_STRATEGY_ENV, "COMBINED");

        var testActor = new TestActor();
        testActor.setName("TESTACTOR");

        var controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        doAnswer(invocationOnMock -> {
            List<ValidationContext> validationContextList = invocationOnMock.getArgument(3);
            assertThat(validationContextList)
                    .satisfiesExactly(
                            validationContext -> assertThat(validationContext)
                                    .isInstanceOf(HeaderValidationContext.class),
                            validationContext -> assertThat(validationContext)
                                    .isInstanceOf(DefaultMessageValidationContext.class)
                    );
            validationContextList.forEach(vc -> vc.updateStatus(ValidationStatus.PASSED));

            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        var customValidatorInvoked = new AtomicBoolean(false);
        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("direct:mockQueue?timeout=15000")
                .actor(testActor)
                .getMessageBuilderSupport()
                .body("expected body")
                .validate(
                        (message, testContext) -> customValidatorInvoked.set(true)
                )
                .build();

        assertThatCode(() -> receiveAction.execute(context))
                .doesNotThrowAnyException();

        assertThat(customValidatorInvoked)
                .isTrue();
    }

    @Nested
    public class AssumeMessageTypeTest {

        @Mock
        private Message messageMock;

        @Mock
        private TestContext contextMock;

        @Mock
        private LogModifier logModifierMock;

        @Mock
        private MessageStore messageStoreMock;

        @Mock
        private MessageValidatorRegistry messageValidatorRegistryMock;

        @Mock
        private Endpoint endpointMock;

        private ReceiveMessageAction fixture;

        @BeforeEach
        public void setup() {
            doReturn(logModifierMock).when(contextMock).getLogModifier();
            doReturn(messageStoreMock).when(contextMock).getMessageStore();
            doReturn(messageValidatorRegistryMock).when(contextMock).getMessageValidatorRegistry();
            doReturn(Collections.singletonList(new DefaultMessageHeaderValidator()))
                    .when(messageValidatorRegistryMock).findMessageValidators(anyString(), any(Message.class), eq(false));
            doReturn(new DefaultTextEqualsMessageValidator()).when(messageValidatorRegistryMock).getDefaultMessageValidator();

            fixture = new ReceiveMessageAction.Builder()
                    .endpoint(endpointMock)
                    .build();
        }

        public static String[] jsonPayload() {
            return new String[]{"{", "[", " {", " ["};
        }

        @ParameterizedTest
        @MethodSource("jsonPayload")
        void shouldAssumeJSONMessageType_forJSONPayload(String payload) {
            shouldAssumeMessageTypeOnPayload(payload, JSON);
        }

        @ParameterizedTest
        @MethodSource("jsonPayload")
        void shouldAssumeJSONMessageType_forJSONPayload_andPreviouslyEmptyMessageType(String payload) {
            doReturn(payload).when(messageMock).getPayload(String.class);

            setField(fixture, "messageType", null);
            assertThat(fixture.getMessageType()).isNull();

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(JSON.name());
        }

        public static String[] xmlPayload() {
            return new String[]{"<", " <"};
        }

        @ParameterizedTest
        @MethodSource("xmlPayload")
        void shouldAssumeXMLMessageType_forXMLPayload(String payload) {
            shouldAssumeMessageTypeOnPayload(payload, XML);
        }

        @ParameterizedTest
        @MethodSource("xmlPayload")
        void shouldAssumeXMLMessageType_forXMLPayload_andPreviouslyEmptyMessageType(String payload) {
            doReturn(payload).when(messageMock).getPayload(String.class);

            setField(fixture, "messageType", null);
            assertThat(fixture.getMessageType()).isNull();

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(XML.name());
        }

        @Test
        void shouldNotAssumeAnything_forEverythingElse() {
            shouldAssumeMessageTypeOnPayload("foo", XML);
        }

        public static String[] emptyOrNullString() {
            return new String[]{null, "", " "};
        }

        @ParameterizedTest
        @MethodSource("emptyOrNullString")
        void shouldAssumePlaintext_forEmptyPayload(String payload) {
            shouldAssumeMessageTypeOnPayload(payload, PLAINTEXT);
        }

        private void shouldAssumeMessageTypeOnPayload(String payload, MessageType messageType) {
            doReturn(payload).when(messageMock).getPayload(String.class);

            assertThat(fixture.getMessageType()).isEqualTo(XML.name());

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(messageType.name());
        }

        @ParameterizedTest
        @MethodSource("emptyOrNullString")
        void shouldNotAssumeTypeOnEmptyControlPayload(String controlPayload) {
            shouldAssumeMessageTypeOnControlPayload("{}", controlPayload, JSON);
        }

        @ParameterizedTest
        @MethodSource("jsonPayload")
        void shouldAssumeMessageOnControlPayload_withJSONPayload(String jsonPayload) {
            shouldAssumeMessageTypeOnControlPayload("<>", jsonPayload, JSON);
        }

        @ParameterizedTest
        @MethodSource("xmlPayload")
        void shouldAssumeMessageOnControlPayload_withXMLPayload(String xmlPayload) {
            shouldAssumeMessageTypeOnControlPayload("{}", xmlPayload, XML);
        }

        private void shouldAssumeMessageTypeOnControlPayload(String messagePayload, String controlMessagePayload, MessageType messageType) {
            doReturn(messagePayload).when(messageMock).getPayload(String.class);

            assertThat(fixture.getMessageType()).isEqualTo(XML.name());

            var messageBuilderMock = mock(MessageBuilder.class);
            setField(fixture, "messageBuilder", messageBuilderMock);

            var controlMessage = mock(Message.class);
            doReturn(null).when(controlMessage).getPayload();
            doReturn(controlMessagePayload).when(controlMessage).getPayload(String.class);
            doReturn(controlMessage).when(messageBuilderMock).build(contextMock, XML.name());

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(messageType.name());
        }

        @ParameterizedTest
        @MethodSource("xmlPayload")
        void shouldAcceptXMLLikeMessageType_XHTML_andNotAlterIt(String xmlPayload) {
            doReturn(xmlPayload).when(messageMock).getPayload(String.class);

            var xhtmlMessageType = XHTML.name();

            setField(fixture, "messageType", xhtmlMessageType);
            assertThat(fixture.getMessageType()).isEqualTo(xhtmlMessageType);

            var messageBuilderMock = mock(MessageBuilder.class);
            setField(fixture, "messageBuilder", messageBuilderMock);

            var controlMessage = mock(Message.class);
            doReturn(null).when(controlMessage).getPayload();
            doReturn(xmlPayload).when(controlMessage).getPayload(String.class);
            doReturn(controlMessage).when(messageBuilderMock).build(contextMock, xhtmlMessageType);

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(xhtmlMessageType);
        }

        @ParameterizedTest
        @MethodSource("jsonPayload")
        void shouldNotOverrideExplicitMessageType(String jsonPayload) {
            doReturn(jsonPayload).when(messageMock).getPayload(String.class);

            setField(fixture, "messageType", PLAINTEXT.name());
            setField(fixture, "isExplicitMessageType", true);
            assertThat(fixture.getMessageType()).isEqualTo(PLAINTEXT.name());

            fixture.validateMessage(messageMock, contextMock);

            assertThat(fixture.getMessageType()).isEqualTo(PLAINTEXT.name());
        }
    }
}
