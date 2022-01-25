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

package com.consol.citrus.actions;

import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.builder.script.GroovyFileResourcePayloadBuilder;
import com.consol.citrus.message.builder.script.GroovyScriptPayloadBuilder;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.TestUtils;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends AbstractTestNGUnitTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private SelectiveConsumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Mock
    private MessageValidator<?> validator;
    @Mock
    private MessageQueue mockQueue;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        when(validator.supportsMessageType(any(String.class), any(Message.class))).thenReturn(true);

        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("validator", validator);

        factory.getReferenceResolver().bind("mockQueue", mockQueue);
        return factory;
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReceiveMessageWithMessageBuilderScriptData() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        String markup = "markupBuilder.TestRequest(){\n" +
                "Message('Hello World!')\n" +
                "}";
        controlMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(markup));

        Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
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

            Assert.assertEquals(TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()), TestUtils.normalizeLineEndings(control.getPayload(String.class).trim()));
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
    public void testReceiveMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        String markup = "markupBuilder.TestRequest(){\n" +
                "Message('${text}')\n" +
                "}";
        controlMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(markup));

        Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
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

            Assert.assertEquals(TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()), TestUtils.normalizeLineEndings(control.getPayload(String.class).trim()));
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
    public void testReceiveMessageWithMessageBuilderScriptResource() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder("classpath:com/consol/citrus/actions/test-request-payload.groovy"));

        final Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
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

            Assert.assertEquals(TestUtils.normalizeLineEndings(received.getPayload(String.class).trim()), TestUtils.normalizeLineEndings(control.getPayload(String.class).trim()));
            new DefaultMessageHeaderValidator().validateMessage(received, control, context, validationContextList);
            return null;
        }).when(validator).validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }
}
