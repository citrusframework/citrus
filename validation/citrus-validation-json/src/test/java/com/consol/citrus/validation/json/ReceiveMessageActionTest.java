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

package com.consol.citrus.validation.json;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.SimpleReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.mockito.Mockito;
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

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private SelectiveConsumer consumer = Mockito.mock(SelectiveConsumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    private MessageQueue mockQueue = Mockito.mock(MessageQueue.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().getFunctionLibraries().add(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().getValidationMatcherLibraries().add(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().getMessageValidators().put("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("json", new JsonTextMessageValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("jsonPath", new JsonPathMessageValidator());

        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("mockQueue", mockQueue);
        factory.setReferenceResolver(referenceResolver);
        return factory;
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsJsonPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("{ \"TestRequest\": { \"Message\": \"?\" }}");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("{ \"TestRequest\": { \"Message\": \"Hello World!\" }}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageType(MessageType.JSON)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageJsonPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("$.text", "messageVar");
        extractMessageElements.put("$.person", "person");

        JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor();
        variableExtractor.setJsonPathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

    }

    @Test
    public void testReceiveMessageWithJsonPathValidation() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello World!");
        jsonPathExpressions.put("$.person.name", "John");
        jsonPathExpressions.put("$.person.surname", "Doe");
        jsonPathExpressions.put("$.index", "5");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test(expectedExceptions = ValidationException.class)
    public void testReceiveMessageWithJsonPathValidationFailure() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello Citrus!");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testReceiveMessageWithJsonPathValidationNoPathResult() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.person.age", "50");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadAsExpected() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("");

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadUnexpected() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("{\"text\":\"Hello World!\"}");

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to validate JSON text:" + System.lineSeparator() +
                    " Validation failed - expected message contents, but received empty message!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving unexpected empty message payload");
    }
}
