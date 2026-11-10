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

package org.citrusframework.ws.xml;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapAttachmentValidator;
import org.citrusframework.ws.validation.SoapAttachmentValidator;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

public class SoapClientTest extends AbstractXmlActionTest {

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private WebServiceServer soapServer;
    private WebServiceClient soapClient;

    private final Queue<SoapMessage> responses = new ArrayBlockingQueue<>(4);

    @BeforeEach
    public void setupEndpoints() {
        EndpointAdapter endpointAdapter = new DirectEndpointAdapter(new DirectSyncEndpointConfiguration()) {
            @Override
            public Message handleMessageInternal(Message request) {
                return responses.isEmpty() ? new SoapMessage() : responses.remove();
            }
        };

        soapServer = soap().server()
                .port(port)
                .timeout(500L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("soapServer")
                .build();
        soapServer.initialize();

        soapClient = soap().client()
                .defaultUri(uri)
                .name("soapClient")
                .build();
    }

    @AfterEach
    public void cleanupEndpoints() {
        if (soapServer != null) {
            soapServer.stop();
        }
    }

    @Test
    public void shouldLoadSoapClientActions() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/xml/soap-client.citrus.it.xml");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("soapClient", soapClient);
        context.getReferenceResolver().bind("soapServer", soapServer);

        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());
        context.getReferenceResolver().bind("soapAttachmentValidator", new SimpleSoapAttachmentValidator());
        context.getReferenceResolver().bind("mySoapAttachmentValidator", new SimpleSoapAttachmentValidator());

        responses.add(createSoapMessage(new SoapAttachment("MySoapAttachment", "text/plain", "This is an attachment!")));

        responses.add(createSoapMessage(new SoapAttachment("MySoapAttachment", "application/xml",
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));

        responses.add(createSoapMessage(new SoapAttachment("FirstSoapAttachment", "text/plain", "This is an attachment!"),
                new SoapAttachment("SecondSoapAttachment", "application/xml", FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));

        responses.add(createSoapMessage());

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SoapClientTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(8L, result.getActionCount());
        Assertions.assertEquals(SendSoapMessageAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("soap:send-request", result.getTestAction(0).getName());

        Assertions.assertEquals(ReceiveSoapMessageAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("soap:receive-response", result.getTestAction(1).getName());

        int actionIndex = 0;

        SendSoapMessageAction send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals("myAction", send.getMessageBuilder().build(context, MessageType.XML.name()).getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assertions.assertEquals(1L, send.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", send.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", send.getAttachments().get(0).getContentType());

        ReceiveSoapMessageAction receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse>Hello User</TestResponse>", receive.getMessageBuilder().build(context, MessageType.XML.name()).getPayload());
        Assertions.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator", SoapAttachmentValidator.class));
        Assertions.assertEquals(1L, receive.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", receive.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(receive.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", receive.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", receive.getAttachments().get(0).getContentType());

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals(1L, send.getAttachments().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-attachment.xml", send.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("application/xml", send.getAttachments().get(0).getContentType());
        Assertions.assertEquals("UTF-8", send.getAttachments().get(0).getCharsetName());

        receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("mySoapAttachmentValidator", SoapAttachmentValidator.class));
        Assertions.assertEquals(1L, receive.getAttachments().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-attachment.xml", receive.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", receive.getAttachments().get(0).getContentId());
        Assertions.assertEquals("application/xml", receive.getAttachments().get(0).getContentType());
        Assertions.assertEquals("UTF-8", receive.getAttachments().get(0).getCharsetName());

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals(2L, send.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", send.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("FirstSoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", send.getAttachments().get(0).getContentType());
        Assertions.assertNotNull(send.getAttachments().get(1).getContentResourcePath());
        Assertions.assertEquals("SecondSoapAttachment", send.getAttachments().get(1).getContentId());
        Assertions.assertEquals("application/xml", send.getAttachments().get(1).getContentType());
        Assertions.assertEquals("UTF-8", send.getAttachments().get(1).getCharsetName());

        receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assertions.assertEquals(2L, receive.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", receive.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(receive.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("FirstSoapAttachment", receive.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", receive.getAttachments().get(0).getContentType());
        Assertions.assertNotNull(receive.getAttachments().get(1).getContentResourcePath());
        Assertions.assertEquals("SecondSoapAttachment", receive.getAttachments().get(1).getContentId());
        Assertions.assertEquals("application/xml", receive.getAttachments().get(1).getContentType());
        Assertions.assertEquals("UTF-8", receive.getAttachments().get(1).getCharsetName());

        send = (SendSoapMessageAction) result.getTestAction(actionIndex);
        Assertions.assertTrue(send.isMtomEnabled());
        Assertions.assertEquals(0L, send.getAttachments().size());
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("soap").isPresent());
        Assertions.assertEquals(Soap.class, XmlTestActionBuilder.lookup("soap").get().getClass());
    }

    private SoapMessage createSoapMessage(SoapAttachment... attachments) {
        SoapMessage message = new SoapMessage("<TestResponse>Hello User</TestResponse>")
                .contentType("application/xml");

        Arrays.stream(attachments).forEach(message::addAttachment);

        return message;
    }
}
