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

package org.citrusframework.actions;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.builder.script.GroovyFileResourcePayloadBuilder;
import org.citrusframework.message.builder.script.GroovyScriptPayloadBuilder;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.TestUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.ValidationContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        controlMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder("classpath:org/citrusframework/actions/test-request-payload.groovy"));

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
