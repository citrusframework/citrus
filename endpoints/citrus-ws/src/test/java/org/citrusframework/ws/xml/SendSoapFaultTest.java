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

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

public class SendSoapFaultTest extends AbstractXmlActionTest {

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
    public void shouldLoadSoapServerActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/xml/send-soap-fault.citrus.it.xml");

        context.getReferenceResolver().bind("soapServer", soapServer);
        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());

        endpointAdapter.handleMessage(new SoapMessage("<TestMessage>Hello Citrus</TestMessage>").contentType("application/xml"));
        endpointAdapter.handleMessage(new SoapMessage("<TestMessage>Hello Citrus</TestMessage>").contentType("application/xml"));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SendSoapFaultTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(ReceiveSoapMessageAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("soap:receive-request", result.getTestAction(0).getName());

        Assertions.assertEquals(SendSoapFaultAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("soap:send-fault", result.getTestAction(1).getName());

        int actionIndex = 0;

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload());

        SendSoapFaultAction send = (SendSoapFaultAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("soapServer", send.getEndpointUri());
        Assertions.assertNotNull(send.getMessageBuilder());
        Assertions.assertEquals(StaticMessageBuilder.class, send.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder)send.getMessageBuilder();
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sendFault", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("{http://citrusframework.org/faults}citrus-ns:FAULT-1000", send.getFaultCode());
        Assertions.assertEquals("FaultString", send.getFaultString());
        Assertions.assertEquals(1, send.getFaultDetails().size());
        Assertions.assertTrue(send.getFaultDetails().get(0).startsWith("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">"));
        Assertions.assertNull(send.getFaultActor());

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload());

        send = (SendSoapFaultAction) result.getTestAction(actionIndex);
        Assertions.assertEquals("soapServer", send.getEndpointUri());
        Assertions.assertNotNull(send.getMessageBuilder());
        Assertions.assertEquals(StaticMessageBuilder.class, send.getMessageBuilder().getClass());

        messageBuilder = (StaticMessageBuilder)send.getMessageBuilder();
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sendFault", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("{http://citrusframework.org/faults}citrus-ns:FAULT-1001", send.getFaultCode());
        Assertions.assertEquals("FaultString", send.getFaultString());
        Assertions.assertEquals(0, send.getFaultDetails().size());
        Assertions.assertEquals(1, send.getFaultDetailResourcePaths().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-fault-detail.xml", send.getFaultDetailResourcePaths().get(0));
        Assertions.assertEquals("FaultActor", send.getFaultActor());
    }
}
