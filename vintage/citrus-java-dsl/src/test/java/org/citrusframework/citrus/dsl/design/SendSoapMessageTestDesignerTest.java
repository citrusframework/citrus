/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.dsl.design;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.citrusframework.citrus.ws.actions.SendSoapMessageAction;
import org.citrusframework.citrus.ws.client.WebServiceClient;
import org.citrusframework.citrus.ws.message.SoapAttachment;
import org.citrusframework.citrus.ws.message.SoapMessageHeaders;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageTestDesignerTest extends UnitTestSupport {

    private WebServiceClient soapClient = Mockito.mock(WebServiceClient.class);

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Resource resource = Mockito.mock(Resource.class);

    private SoapAttachment testAttachment = new SoapAttachment();

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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(soapClient)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .header("additional", "additionalValue");

                send(soapClient)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                    .fork(true);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");

        Assert.assertFalse(action.isForkMode());

        action = (SendMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testSoapAction() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                        .send()
                        .soapAction("TestService/sayHello")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(SoapMessageHeaders.SOAP_ACTION), "TestService/sayHello");
    }

    @Test
    public void testSoapAttachment() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

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
    public void testMtomSoapAttachment() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .mtomEnabled(true)
                    .payload("<TestRequest><data>cid:attachment01</data></TestRequest>")
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

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
    public void testSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

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
    public void testMultipleSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                        .send()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

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
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().client("soapClient")
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "soapOperation")
                    .attachment(testAttachment);

                send("otherClient")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertTrue(messageBuilder.buildMessageHeaders(context).containsKey("operation"));

        action = (SendMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "otherClient");
    }

}
