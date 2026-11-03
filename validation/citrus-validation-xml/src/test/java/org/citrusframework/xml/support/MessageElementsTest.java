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

package org.citrusframework.xml.support;

import java.util.HashMap;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XpathMessageProcessor;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.xml.UnitTestSupport;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageElementsTest extends UnitTestSupport {
    @Mock
    private Endpoint endpoint;
    @Mock
    private Consumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Override
    protected TestContextFactory createTestContextFactory() {
        return super.createTestContextFactory();
    }

    @Test
    public void testValidateMessageElements() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "text-value");
        validateMessageElements.put("//sub-elementB", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testValidateEmptyMessageElements() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'></sub-elementA>"
                            + "<sub-elementB attribute='B'></sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "");
        validateMessageElements.put("//sub-elementB", "");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testValidateEmptyMessageAttributes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute=''>text-value</sub-elementA>"
                            + "<sub-elementB attribute=''>text-value</sub-elementB>"
                            + "<sub-elementC attribute=''>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "");
        validateMessageElements.put("//root/element/sub-elementB/@attribute", "");
        validateMessageElements.put("//root/element/sub-elementC/@attribute", "");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testValidateNullElements() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'></sub-elementA>"
        + "<sub-elementB attribute='B'></sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "null");
        validateMessageElements.put("//sub-elementB", "null");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
        .expressions(validateMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .validate(validationContext)
        .build();
        assertThrows(ValidationException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testValidateMessageElementAttributes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "A");
        validateMessageElements.put("//sub-elementB/@attribute", "B");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testValidateMessageElementsWrongExpectedElement() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-element-wrong", "text-value");
        validateMessageElements.put("//sub-element-wrong", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
        .expressions(validateMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .validate(validationContext)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testValidateMessageElementsWrongExpectedValue() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "text-value-wrong");
        validateMessageElements.put("//sub-elementB", "text-value-wrong");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
        .expressions(validateMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .validate(validationContext)
        .build();
        assertThrows(ValidationException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testValidateMessageElementAttributesWrongExpectedValue() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "wrong-value");
        validateMessageElements.put("//sub-elementB/@attribute", "wrong-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
        .expressions(validateMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .validate(validationContext)
        .build();
        assertThrows(ValidationException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testValidateMessageElementAttributesWrongExpectedAttribute() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute-wrong", "A");
        validateMessageElements.put("//sub-elementB/@attribute-wrong", "B");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
        .expressions(validateMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .validate(validationContext)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testSetMessageElements() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA", "text-value");
        messageElements.put("//sub-elementB", "text-value");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(messageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(processor)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testSetMessageElementsUsingEmptyString() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'></sub-elementA>"
                            + "<sub-elementB attribute='B'></sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA", "");
        messageElements.put("//sub-elementB", "");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(messageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(processor)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testSetMessageElementsAndValidate() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA", "text-value");
        messageElements.put("//sub-elementB", "text-value");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(messageElements)
                .build();

        HashMap<String, Object> validateElements = new HashMap<>();
        validateElements.put("//root/element/sub-elementA", "text-value");
        validateElements.put("//sub-elementB", "text-value");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validateElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .process(processor)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testSetMessageElementAttributes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA/@attribute", "A");
        messageElements.put("//sub-elementB/@attribute", "B");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(messageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(processor)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testSetMessageElementsError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
        + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-element-wrong", "text-value");
        messageElements.put("//sub-element-wrong", "text-value");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
        .expressions(messageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .process(processor)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testSetMessageElementAttributesError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
        + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA/@attribute-wrong", "A");
        messageElements.put("//sub-elementB/@attribute-wrong", "B");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
        .expressions(messageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .process(processor)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testSetMessageElementAttributesErrorWrongElement() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
        + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>"));

        HashMap<String, Object> messageElements = new HashMap<>();
        messageElements.put("//root/element/sub-elementA-wrong/@attribute", "A");
        messageElements.put("//sub-elementB-wrong/@attribute", "B");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
        .expressions(messageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .process(processor)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testExtractMessageElements() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        HashMap<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("//root/element/sub-elementA", "${valueA}");
        extractMessageElements.put("//root/element/sub-elementB", "${valueB}");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
                .expressions(extractMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assertions.assertTrue(context.getVariables().containsKey("valueA"));
        Assertions.assertEquals("text-value", context.getVariables().get("valueA"));
        Assertions.assertTrue(context.getVariables().containsKey("valueB"));
        Assertions.assertEquals("text-value", context.getVariables().get("valueB"));
    }

    @Test
    public void testExtractMessageAttributes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        HashMap<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("//root/element/sub-elementA/@attribute", "${valueA}");
        extractMessageElements.put("//root/element/sub-elementB/@attribute", "${valueB}");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
                .expressions(extractMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assertions.assertTrue(context.getVariables().containsKey("valueA"));
        Assertions.assertEquals("A", context.getVariables().get("valueA"));
        Assertions.assertTrue(context.getVariables().containsKey("valueB"));
        Assertions.assertEquals("B", context.getVariables().get("valueB"));
    }

    @Test
    public void testExtractMessageElementsForWrongElement() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>"));

        HashMap<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("//root/element/sub-element-wrong", "${valueA}");
        extractMessageElements.put("//element/sub-element-wrong", "${valueB}");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
        .expressions(extractMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .process(variableExtractor)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }

    @Test
    public void testExtractMessageElementsForWrongAtribute() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
        + "<sub-elementA attribute='A'>text-value</sub-elementA>"
        + "<sub-elementB attribute='B'>text-value</sub-elementB>"
        + "<sub-elementC attribute='C'>text-value</sub-elementC>"
        + "</element>"
        + "</root>"));

        HashMap<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("//root/element/sub-elementA/@attribute-wrong", "${attributeA}");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
        .expressions(extractMessageElements)
        .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
        .endpoint(endpoint)
        .message(controlMessageBuilder)
        .process(variableExtractor)
        .build();
        assertThrows(CitrusRuntimeException.class, () -> receiveAction.execute(context));
    }
}
