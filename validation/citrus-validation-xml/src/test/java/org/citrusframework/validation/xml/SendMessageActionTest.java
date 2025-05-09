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

package org.citrusframework.validation.xml;

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
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Producer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SendMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = mock(Endpoint.class);
    private Producer producer = mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = mock(EndpointConfiguration.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }

    @Test
    public void testSendMessageOverwriteMessageElementsXPath() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
            .expressions(overwriteElements)
            .build();

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");

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
    public void testSendMessageOverwriteMessageElementsDotNotation() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("TestRequest.Message", "Hello World!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(overwriteElements)
                .build();

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");

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
    public void testSendMessageOverwriteMessageElementsXPathWithNamespace() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(overwriteElements)
                .build();

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <ns0:TestRequest xmlns:ns0="http://citrusframework.org/unittest">
                    <ns0:Message>Hello World!</ns0:Message>
                </ns0:TestRequest>""");

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
    public void testSendMessageOverwriteMessageElementsXPathWithDefaultNamespace() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>"));

        Map<String, Object> overwriteElements = new HashMap<>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(overwriteElements)
                .build();

        final Message controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest xmlns="http://citrusframework.org/unittest">
                    <Message>Hello World!</Message>
                </TestRequest>""");

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
    public void testSendMessageWithXmlDeclaration() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>"));

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

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
    public void testSendXmlMessageWithValidation() {

        AtomicBoolean  validated = new AtomicBoolean(false);

        SchemaValidator<?> schemaValidator = mock(SchemaValidator.class);
        when(schemaValidator.supportsMessageType(eq("XML"), any())).thenReturn(true);
        doAnswer(invocation-> {

            Assert.assertEquals(invocation.getArgument(3, String.class), "fooSchema");
            Assert.assertEquals(invocation.getArgument(2, String.class), "fooRepository");

            validated.set(true);
            return null;
        }).when(schemaValidator)
            .validate(isA(Message.class), isA(TestContext.class), isA(String.class), isA(String.class));
        doReturn(true).when(schemaValidator).canValidate(isA(Message.class), isA(Boolean.class));

        context.getMessageValidatorRegistry().addSchemaValidator("XML", schemaValidator);

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>?</Message></TestRequest>"));

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
                .build();
        sendAction.execute(context);

        Assert.assertTrue(validated.get());
    }

    private void validateMessageToSend(Message toSend, Message controlMessage) {
        Assert.assertEquals(toSend.getPayload(String.class).trim(), controlMessage.getPayload(String.class).trim());
        DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
        validator.validateMessage(toSend, controlMessage, context, new HeaderValidationContext.Builder().build());
    }
}
