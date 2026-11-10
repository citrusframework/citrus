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

package org.citrusframework.ws.actions.dsl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Producer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class SendSoapMessageTestActionBuilderTest extends UnitTestSupport {

    private WebServiceClient soapClient;
    private Producer messageProducer;
    private ReferenceResolver referenceResolver;
    private Resource resource;

    private final SoapAttachment testAttachment = new SoapAttachment();

    /**
     * Setup test attachment.
     */
    @BeforeEach
    public void setup() {
        testAttachment.setContentId("attachment01");
        testAttachment.setContent("This is an attachment");
        testAttachment.setContentType("text/plain");
        testAttachment.setCharsetName("UTF-8");
    }

    @Override
    @BeforeEach
    public void prepareTest() {
        super.prepareTest();
        soapClient = Mockito.mock(WebServiceClient.class);
        messageProducer = Mockito.mock(Producer.class);
        referenceResolver = Mockito.mock(ReferenceResolver.class);
        resource = Mockito.mock(Resource.class);
    }

    @Test
    public void testFork() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("Foo", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                            .header("additional", "additionalValue"));

        builder.$(soap().client(soapClient)
                .send()
                .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                .fork(true));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(1).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("Foo", messageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals("foo", messageBuilder.getMessage().getHeader("operation"));
        Assertions.assertEquals(3L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(MessageType.PLAINTEXT.name(), messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE));
        Assertions.assertEquals("additionalValue", messageBuilder.buildMessageHeaders(context).get("additional"));
        Assertions.assertEquals("foo", messageBuilder.buildMessageHeaders(context).get("operation"));

        Assertions.assertFalse(action.isForkMode());

        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        Assertions.assertTrue(action.isForkMode());
    }

    @Test
    public void testSoapAction() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals("TestService/sayHello", message.getSoapAction());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("TestService/sayHello")
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, messageBuilder.getMessage().getHeaders().size());
        Assertions.assertEquals("TestService/sayHello", messageBuilder.getMessage().getHeaders().get(SoapMessageHeaders.SOAP_ACTION));
    }

    @Test
    public void testSoapAttachment() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(1L, message.getAttachments().size());
            Assertions.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assertions.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assertions.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentData() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(1L, message.getAttachments().size());
            Assertions.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent()));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assertions.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assertions.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMtomSoapAttachmentData() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><data><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:attachment01\"/></data></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(1L, message.getAttachments().size());
            Assertions.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .mtomEnabled(true)
                        .body("<TestRequest><data>cid:attachment01</data></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent()));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><data>cid:attachment01</data></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

        Assertions.assertTrue(action.getMtomEnabled());

        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assertions.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assertions.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(2L, message.getAttachments().size());
            Assertions.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
            Assertions.assertEquals(message.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        when(soapClient.createProducer()).thenReturn(messageProducer);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

        Assertions.assertEquals(2L, action.getAttachments().size());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
        Assertions.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId() + 1);
        Assertions.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
        Assertions.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assertions.assertEquals(action.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
        Assertions.assertEquals(action.getAttachments().get(1).getContentId(), testAttachment.getContentId() + 2);
        Assertions.assertEquals(action.getAttachments().get(1).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(1).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentResource() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(1L, message.getAttachments().size());
            Assertions.assertEquals("someAttachmentData", message.getAttachments().get(0).getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), resource));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());

        Assertions.assertEquals(soapClient, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

        Assertions.assertEquals("someAttachmentData", action.getAttachments().get(0).getContent());
        Assertions.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assertions.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assertions.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSendBuilderWithEndpointName() {
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertEquals(1L, message.getAttachments().size());
            Assertions.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable("soapClient", Endpoint.class)).thenReturn(true);
        when(referenceResolver.resolve("soapClient", Endpoint.class)).thenReturn(soapClient);
        when(referenceResolver.isResolvable("otherClient", Endpoint.class)).thenReturn(true);
        when(referenceResolver.resolve("otherClient", Endpoint.class)).thenReturn(soapClient);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client("soapClient")
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("operation", "soapOperation")
                        .attachment(testAttachment));

        builder.$(soap().client("otherClient")
                .send()
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(SendSoapMessageAction.class, test.getActions().get(1).getClass());

        SendMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assertions.assertEquals("soap:send-request", action.getName());
        Assertions.assertEquals("soapClient", action.getEndpointUri());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertTrue(messageBuilder.buildMessageHeaders(context).containsKey("operation"));

        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assertions.assertEquals("soap:send-request", action.getName());
        Assertions.assertEquals("otherClient", action.getEndpointUri());
    }
}
