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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.ws.actions.SoapActionBuilder.soap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageTestActionBuilderTest extends UnitTestSupport {

    private final Consumer messageConsumer = Mockito.mock(Consumer.class);
    private final EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private final WebServiceServer server = Mockito.mock(WebServiceServer.class);
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
    public void testWebServiceServerReceive() {
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("Foo")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(server)
                        .receive()
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .attachment(testAttachment));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "Foo");
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
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(server)
                        .receive()
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>").setHeader("operation", "foo"))
                        .attachment(testAttachment));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
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
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(server)
                        .receive()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent()));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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

        reset(server, messageConsumer, configuration, resource, attachmentResource);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        when(attachmentResource.exists()).thenReturn(true);
        when(attachmentResource.getInputStream()).thenReturn(new ByteArrayInputStream("This is an attachment".getBytes()));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(server)
                        .receive()
                        .message()
                        .body(resource)
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource, StandardCharsets.UTF_8));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().get(0).getContent(), "This is an attachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        final SoapAttachment attachment1 = new SoapAttachment();
        attachment1.setContentId("attachment01");
        attachment1.setContent("This is an attachment");
        attachment1.setContentType("text/plain");
        attachment1.setCharsetName("UTF-8");

        final SoapAttachment attachment2 = new SoapAttachment();
        attachment2.setContentId("attachment02");
        attachment2.setContent("This is an attachment");
        attachment2.setContentType("text/plain");
        attachment2.setCharsetName("UTF-8");

        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(attachment1)
                .addAttachment(attachment2));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(server)
                        .receive()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(attachment1.getContentId(), attachment1.getContentType(), attachment1.getContent())
                        .attachment(attachment2.getContentId(), attachment2.getContentType(), attachment2.getContent()));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), attachment1.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), attachment1.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), attachment1.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), attachment1.getCharsetName());

        Assert.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(1).getContent(), attachment2.getContent());
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), attachment2.getContentId());
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), attachment2.getContentType());
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), attachment2.getCharsetName());
    }

    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(referenceResolver);
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo"));
        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("replyMessageEndpoint", Endpoint.class)).thenReturn(server);
        when(referenceResolver.resolve("fooMessageEndpoint", Endpoint.class)).thenReturn(server);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server("replyMessageEndpoint")
                        .receive()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        builder.$(soap().server("fooMessageEndpoint")
                        .receive()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "soap:receive-request");
        Assert.assertEquals(action.getEndpointUri(), "replyMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        action = ((ReceiveSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "soap:receive-request");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
    }

}
