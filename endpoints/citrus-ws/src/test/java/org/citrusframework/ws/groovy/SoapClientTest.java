/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapAttachmentValidator;
import org.citrusframework.ws.validation.SoapAttachmentValidator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

/**
 * @author Christoph Deppisch
 */
public class SoapClientTest extends AbstractGroovyActionDslTest {

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private WebServiceServer soapServer;
    private WebServiceClient soapClient;

    private final Queue<SoapMessage> responses = new ArrayBlockingQueue<>(4);

    @BeforeClass
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

    @AfterClass(alwaysRun = true)
    public void cleanupEndpoints() {
        if (soapServer != null) {
            soapServer.stop();
        }
    }

    @Test
    public void shouldLoadSoapClientActions() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/groovy/soap-client.test.groovy");

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
        Assert.assertEquals(result.getName(), "SoapClientTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 8L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "soap:send-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "soap:receive-response");

        int actionIndex = 0;

        SendSoapMessageAction send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 1L);
        Assert.assertEquals(send.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "text/plain");

        ReceiveSoapMessageAction receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator", SoapAttachmentValidator.class));
        Assert.assertEquals(receive.getAttachments().size(), 1L);
        Assert.assertEquals(receive.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(receive.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(receive.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(receive.getAttachments().get(0).getContentType(), "text/plain");

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 1L);
        Assert.assertEquals(send.getAttachments().get(0).getContent(), FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")));
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "application/xml");
        Assert.assertEquals(send.getAttachments().get(0).getCharsetName(), "UTF-8");

        receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("mySoapAttachmentValidator", SoapAttachmentValidator.class));
        Assert.assertEquals(receive.getAttachments().size(), 1L);
        Assert.assertEquals(receive.getAttachments().get(0).getContent(), FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")));
        Assert.assertEquals(receive.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(receive.getAttachments().get(0).getContentType(), "application/xml");
        Assert.assertEquals(receive.getAttachments().get(0).getCharsetName(), "UTF-8");

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 2L);
        Assert.assertEquals(send.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "FirstSoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "text/plain");
        Assert.assertNull(send.getAttachments().get(1).getContentResourcePath());
        Assert.assertNotNull(send.getAttachments().get(1).getContent());
        Assert.assertEquals(send.getAttachments().get(1).getContentId(), "SecondSoapAttachment");
        Assert.assertEquals(send.getAttachments().get(1).getContentType(), "application/xml");
        Assert.assertEquals(send.getAttachments().get(1).getCharsetName(), "UTF-8");

        receive = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receive.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assert.assertEquals(receive.getAttachments().size(), 2L);
        Assert.assertEquals(receive.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(receive.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(receive.getAttachments().get(0).getContentId(), "FirstSoapAttachment");
        Assert.assertEquals(receive.getAttachments().get(0).getContentType(), "text/plain");
        Assert.assertNull(receive.getAttachments().get(1).getContentResourcePath());
        Assert.assertNotNull(receive.getAttachments().get(1).getContent());
        Assert.assertEquals(receive.getAttachments().get(1).getContentId(), "SecondSoapAttachment");
        Assert.assertEquals(receive.getAttachments().get(1).getContentType(), "application/xml");
        Assert.assertEquals(receive.getAttachments().get(1).getCharsetName(), "UTF-8");

        send = (SendSoapMessageAction) result.getTestAction(actionIndex);
        Assert.assertTrue(send.isMtomEnabled());
        Assert.assertEquals(send.getAttachments().size(), 0L);
    }

    private SoapMessage createSoapMessage(SoapAttachment... attachments) {
        SoapMessage message = new SoapMessage("<TestResponse>Hello User</TestResponse>")
                .contentType("application/xml");

        Arrays.stream(attachments).forEach(message::addAttachment);

        return message;
    }
}
