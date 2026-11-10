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

package org.citrusframework.ws.groovy;

import java.io.IOException;
import java.util.Arrays;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapAttachmentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

public class SoapServerTest extends AbstractGroovyActionDslTest {

    private WebServiceServer soapServer;

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final EndpointAdapter endpointAdapter = new DirectEndpointAdapter(direct()
            .synchronous()
            .timeout(100L)
            .queue(inboundQueue)
            .build());

    @BeforeEach
    public void setupEndpoints() {
        ((AbstractEndpointAdapter) endpointAdapter).setTestContextFactory(testContextFactory);

        soapServer = soap().server()
                .timeout(100L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("soapServer")
                .build();
    }

    @Test
    public void shouldLoadSoapServerActions() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/groovy/soap-server.citrus.test.groovy");

        context.getReferenceResolver().bind("soapServer", soapServer);
        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());
        context.getReferenceResolver().bind("soapAttachmentValidator", new SimpleSoapAttachmentValidator());
        context.getReferenceResolver().bind("mySoapAttachmentValidator", new SimpleSoapAttachmentValidator());

        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("MySoapAttachment", "text/plain", "This is an attachment!")).soapAction("myAction"));
        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("MySoapAttachment", "application/xml",
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));
        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("FirstSoapAttachment", "text/plain", "This is an attachment!"),
                new SoapAttachment("SecondSoapAttachment", "application/xml", FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SoapServerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(6L, result.getActionCount());
        Assertions.assertEquals(ReceiveSoapMessageAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("soap:receive-request", result.getTestAction(0).getName());

        Assertions.assertEquals(SendSoapMessageAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("soap:send-response", result.getTestAction(1).getName());

        int actionIndex = 0;

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload());
        Assertions.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", action.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", action.getAttachments().get(0).getContentType());

        SendSoapMessageAction send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals(1L, send.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", send.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", send.getAttachments().get(0).getContentType());

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("mySoapAttachmentValidator"));
        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertNotNull(action.getAttachments().get(0).getContent());
        Assertions.assertEquals("MySoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("application/xml", action.getAttachments().get(0).getContentType());
        Assertions.assertEquals("UTF-8", action.getAttachments().get(0).getCharsetName());

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals(1L, send.getAttachments().size());
        Assertions.assertEquals(send.getAttachments().get(0).getContent(), FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")));
        Assertions.assertEquals("MySoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("application/xml", send.getAttachments().get(0).getContentType());
        Assertions.assertEquals("UTF-8", send.getAttachments().get(0).getCharsetName());

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assertions.assertEquals(2L, action.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", action.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("FirstSoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", action.getAttachments().get(0).getContentType());
        Assertions.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assertions.assertNotNull(action.getAttachments().get(1).getContent());
        Assertions.assertEquals("SecondSoapAttachment", action.getAttachments().get(1).getContentId());
        Assertions.assertEquals("application/xml", action.getAttachments().get(1).getContentType());
        Assertions.assertEquals("UTF-8", action.getAttachments().get(1).getCharsetName());

        send = (SendSoapMessageAction) result.getTestAction(actionIndex);
        Assertions.assertFalse(send.isForkMode());
        Assertions.assertEquals(2L, send.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", send.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("FirstSoapAttachment", send.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", send.getAttachments().get(0).getContentType());
        Assertions.assertNull(send.getAttachments().get(1).getContentResourcePath());
        Assertions.assertNotNull(send.getAttachments().get(1).getContent());
        Assertions.assertEquals("SecondSoapAttachment", send.getAttachments().get(1).getContentId());
        Assertions.assertEquals("application/xml", send.getAttachments().get(1).getContentType());
        Assertions.assertEquals("UTF-8", send.getAttachments().get(1).getCharsetName());
    }

    private SoapMessage createSoapMessage(SoapAttachment... attachments) {
        SoapMessage message = new SoapMessage("<TestMessage>Hello Citrus</TestMessage>")
                .contentType("application/xml");

        Arrays.stream(attachments).forEach(message::addAttachment);

        return message;
    }
}
