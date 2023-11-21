/*
 * Copyright 2006-2015 the original author or authors.
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
import java.io.IOException;
import java.util.HashMap;

import org.citrusframework.DefaultTestCaseRunner;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.ws.actions.SoapActionBuilder.soap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageTestActionBuilderTest extends UnitTestSupport {

    private final WebServiceClient soapClient = Mockito.mock(WebServiceClient.class);
    private final Producer messageProducer = Mockito.mock(Producer.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private final Resource resource = Mockito.mock(Resource.class);

    private final SoapAttachment testAttachment = new SoapAttachment();

    /**
     * Setup test attachment.
     */
    @BeforeClass
    public void setup() {
        testAttachment.setContentId("attachment01");
        testAttachment.setContent("This is an attachment");
        testAttachment.setContentType("text/plain");
        testAttachment.setCharsetName("UTF-8");
    }

    @Test
    public void testFork() {
        reset(soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 3L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");

        Assert.assertFalse(action.isForkMode());

        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testSoapAction() {
        reset(soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getSoapAction(), "TestService/sayHello");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("TestService/sayHello")
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(SoapMessageHeaders.SOAP_ACTION), "TestService/sayHello");
    }

    @Test
    public void testSoapAttachment() {
        reset(soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 1L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentData() {
        reset(soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 1L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().client(soapClient)
                        .send()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent()));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMtomSoapAttachmentData() {
        reset(soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><data><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:attachment01\"/></data></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 1L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><data>cid:attachment01</data></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

        Assert.assertTrue(action.getMtomEnabled());

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        reset(soapClient, messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 2L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
            Assert.assertEquals(message.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId() + 1);
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
        Assert.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), testAttachment.getContentId() + 2);
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentResource() throws IOException {
        reset(resource, soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 1L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), "someAttachmentData");
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSendBuilderWithEndpointName() {
        reset(referenceResolver, soapClient, messageProducer);
        when(soapClient.createProducer()).thenReturn(messageProducer);
        when(soapClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getAttachments().size(), 1L);
            Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        doAnswer(invocation -> {
            SoapMessage message = (SoapMessage) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("soapClient", Endpoint.class)).thenReturn(soapClient);
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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendSoapMessageAction.class);

        SendMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:send-request");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertTrue(messageBuilder.buildMessageHeaders(context).containsKey("operation"));

        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "soap:send-request");
        Assert.assertEquals(action.getEndpointUri(), "otherClient");
    }
}
