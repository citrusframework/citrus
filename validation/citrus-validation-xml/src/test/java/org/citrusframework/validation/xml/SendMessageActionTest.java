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

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
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

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TestRequest>\n" +
                "    <Message>Hello World!</Message>\n" +
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

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TestRequest>\n" +
                "    <Message>Hello World!</Message>\n" +
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

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">\n" +
                "    <ns0:Message>Hello World!</ns0:Message>\n" +
                "</ns0:TestRequest>");

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

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TestRequest xmlns=\"http://citrusframework.org/unittest\">\n" +
                "    <Message>Hello World!</Message>\n" +
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

            Object argument = invocation.getArgument(2);

            Assert.assertTrue(argument instanceof XmlMessageValidationContext);
            Assert.assertEquals(((XmlMessageValidationContext)argument).getSchema(), "fooSchema");
            Assert.assertEquals(((XmlMessageValidationContext)argument).getSchemaRepository(), "fooRepository");

            validated.set(true);
            return null;
        }).when(schemaValidator).validate(any(), any(), any());

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
        validator.validateMessage(toSend, controlMessage, context, new HeaderValidationContext());
    }
}
