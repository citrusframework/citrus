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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapFaultValidator;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

public class AssertSoapFaultTest extends AbstractXmlActionTest {

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
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/xml/assert-soap-fault.citrus.it.xml");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("soapClient", soapClient);
        context.getReferenceResolver().bind("soapServer", soapServer);

        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());
        context.getReferenceResolver().bind("soapFaultValidator", new SimpleSoapFaultValidator());
        context.getReferenceResolver().bind("customSoapFaultValidator", new SimpleSoapFaultValidator());

        responses.add(createSoapFault("FAULT-1001"));
        responses.add(createSoapFault("FAULT-1002"));
        responses.add(createSoapFault("FAULT-1003").addFaultDetail("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<ns0:DetailId>1000</ns0:DetailId>" +
                "</ns0:FaultDetail>"));
        responses.add(createSoapFault("FAULT-1004").addFaultDetail(FileUtils.readToString(
                FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml"))));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AssertSoapFaultTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(AssertSoapFault.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("soap:assert-fault", result.getTestAction(0).getName());

        int actionIndex = 0;

        AssertSoapFault action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1001", action.getFaultCode());
        Assertions.assertNull(action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertNotNull(action.getValidationContext());

        action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1002", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertNotNull(action.getValidationContext());

        action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1003", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals("FaultActor", action.getFaultActor());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<ns0:DetailId>1000</ns0:DetailId>" +
                "</ns0:FaultDetail>", action.getFaultDetails().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        action = (AssertSoapFault) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("customSoapFaultValidator", SoapFaultValidator.class));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1004", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertEquals(1L, action.getFaultDetailResourcePaths().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-fault-detail.xml", action.getFaultDetailResourcePaths().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());
    }

    private SoapFault createSoapFault(String faultCode) {
        SoapFault fault = new SoapFault("<TestResponse>Hello User</TestResponse>");

        fault.faultCode(String.format("{http://citrusframework.org/faults}%s", faultCode));
        fault.faultString("FaultString");
        fault.faultActor("FaultActor");

        return fault;
    }
}
