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
import java.util.concurrent.atomic.AtomicBoolean;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Producer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.setMessageValidatorRegistry(new MessageValidatorRegistry());
        return factory;
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsJsonPath() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("{ \"TestRequest\": { \"Message\": \"?\" }}"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(overwriteElements)
                .build();

        final Message controlMessage = new DefaultMessage("{\"TestRequest\":{\"Message\":\"Hello World!\"}}");

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
                .type(MessageType.JSON)
                .process(processor)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testSendJsonMessageWithValidation() {

        AtomicBoolean  validated = new AtomicBoolean(false);

        SchemaValidator<?> schemaValidator = mock(SchemaValidator.class);
        when(schemaValidator.supportsMessageType(eq("JSON"), any())).thenReturn(true);
        doAnswer(invocation-> {
            JsonMessageValidationContext argument = invocation.getArgument(2, JsonMessageValidationContext.class);

            Assert.assertEquals(argument.getSchema(), "fooSchema");
            Assert.assertEquals(argument.getSchemaRepository(), "fooRepository");

            validated.set(true);
            return null;
        }).when(schemaValidator).validate(any(), any(), any());

        context.getMessageValidatorRegistry().addSchemaValidator("JSON", schemaValidator);

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("{ \"TestRequest\": { \"Message\": \"?\" }}"));

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .message(messageBuilder)
                .schemaValidation(true)
                .schema("fooSchema")
                .schemaRepository("fooRepository")
                .type(MessageType.JSON)
                .build();
        sendAction.execute(context);

        Assert.assertTrue(validated.get());
    }

    private void validateMessageToSend(Message toSend, Message controlMessage) {
        Assert.assertEquals(toSend.getPayload(String.class).trim(), controlMessage.getPayload(String.class).trim());
        DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
        validator.validateMessage(toSend, controlMessage, context, new HeaderValidationContext());
    }

}
