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

package com.consol.citrus.dsl.design;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.dsl.UnitTestSupport;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.server.WebServiceServer;
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
public class ReceiveSoapMessageTestDesignerTest extends UnitTestSupport {

    private WebServiceServer server = Mockito.mock(WebServiceServer.class);

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
    public void testWebServiceServerReceive() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().server(server)
                    .receive()
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachment() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().server(server)
                        .receive()
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));

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
                soap().server(server)
                    .receive()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentResource() throws IOException {
        final Resource attachmentResource = Mockito.mock(Resource.class);

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().server(server)
                    .receive()
                    .payload(resource)
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource, Charset.forName("UTF-8"));
            }
        };

        reset(resource, attachmentResource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("somePayloadData".getBytes()));
        when(attachmentResource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "somePayloadData");

        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                soap().server(server)
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
    public void testReceiveBuilderWithEndpointName() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive("replyMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");

                receive("fooMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "replyMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        action = (ReceiveMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

    }

}
