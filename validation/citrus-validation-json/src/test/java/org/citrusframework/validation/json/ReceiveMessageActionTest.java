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

package org.citrusframework.validation.json;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
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
    private MessageQueue mockQueue;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("json", new JsonTextMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("jsonPath", new JsonPathMessageValidator());

        factory.getReferenceResolver().bind("mockQueue", mockQueue);
        return factory;
    }

    @Test
    public void testReceiveMessageOverwriteMessageElementsJsonPath() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("{ \"TestRequest\": { \"Message\": \"?\" }}"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(overwriteElements)
                .build();

        Message controlMessage = new DefaultMessage("{ \"TestRequest\": { \"Message\": \"Hello World!\" }}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .validate(validationContext)
                .process(processor)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveMessageWithExtractVariablesFromMessageJsonPath() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}"));

        Map<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("$.text", "messageVar");
        extractMessageElements.put("$.person", "person");

        JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor.Builder()
                .expressions(extractMessageElements)
                .build();

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

    }

    @Test
    public void testReceiveMessageWithJsonPathValidation() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello World!");
        jsonPathExpressions.put("$.person.name", "John");
        jsonPathExpressions.put("$.person.surname", "Doe");
        jsonPathExpressions.put("$.index", "5");

        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(jsonPathExpressions)
                .build();

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test(expectedExceptions = ValidationException.class)
    public void testReceiveMessageWithJsonPathValidationFailure() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello Citrus!");
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(jsonPathExpressions)
                .build();

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testReceiveMessageWithJsonPathValidationNoPathResult() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.person.age", "50");
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(jsonPathExpressions)
                .build();

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testReceiveEmptyMessagePayloadAsExpected() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveEmptyMessagePayloadUnexpected() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("{\"text\":\"Hello World!\"}"));

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.JSON)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Validation failed - expected message contents, but received empty message!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving unexpected empty message payload");
    }

}
