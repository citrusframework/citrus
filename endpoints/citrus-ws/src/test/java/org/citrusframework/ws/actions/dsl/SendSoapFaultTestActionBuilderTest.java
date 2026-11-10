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
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SendSoapFaultTestActionBuilderTest extends UnitTestSupport {

    public static final String FAULT_STRING = "Something went wrong";
    public static final String FAULT_CODE = "CITRUS-1000";
    public static final String ERROR_DETAIL = "<ErrorDetail><message>Something went wrong</message></ErrorDetail>";

    private final WebServiceServer soapServer = Mockito.mock(WebServiceServer.class);
    private final Producer messageProducer = Mockito.mock(Producer.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private final Resource resource = Mockito.mock(Resource.class);

    @Test
    public void testSendSoapFault() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals("faultActor", message.getFaultActor());
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultActor("faultActor")
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals(soapServer, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("faultActor", action.getFaultActor());
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        reset(referenceResolver, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable("soapServer", Endpoint.class)).thenReturn(true);
        when(referenceResolver.resolve("soapServer", Endpoint.class)).thenReturn(soapServer);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server("soapServer")
                        .sendFault()
                        .message()
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals("soapServer", action.getEndpointUri());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertNull(action.getFaultActor());
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }

    @Test
    public void testSendSoapFaultWithDetailResource() {
        reset(resource, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            Assertions.assertEquals(1L, message.getFaultDetails().size());
            Assertions.assertEquals(ERROR_DETAIL, message.getFaultDetails().get(0));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(ERROR_DETAIL.getBytes()));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource(resource)
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals(soapServer, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals(ERROR_DETAIL, action.getFaultDetails().get(0));
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }

    @Test
    public void testSendSoapFaultWithDetail() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            Assertions.assertEquals(1L, message.getFaultDetails().size());
            Assertions.assertEquals("DETAIL", message.getFaultDetails().get(0));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL")
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals(soapServer, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("DETAIL", action.getFaultDetails().get(0));
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }

    @Test
    public void testSendSoapFaultWithDetailResourcePath() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            Assertions.assertEquals(1L, message.getFaultDetails().size());
            Assertions.assertEquals(ERROR_DETAIL, message.getFaultDetails().get(0).trim());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource("classpath:org/citrusframework/ws/actions/dsl/soap-fault-detail.xml")
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals(soapServer, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertEquals(1L, action.getFaultDetailResourcePaths().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/dsl/soap-fault-detail.xml", action.getFaultDetailResourcePaths().get(0));
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assertions.assertEquals(FAULT_CODE, message.getFaultCode());
            Assertions.assertEquals(FAULT_STRING, message.getFaultString());
            Assertions.assertEquals(2L, message.getFaultDetails().size());
            Assertions.assertEquals("DETAIL1", message.getFaultDetails().get(0));
            Assertions.assertEquals("DETAIL2", message.getFaultDetails().get(1));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL1")
                        .faultDetail("DETAIL2")
                        .faultString(FAULT_STRING));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendSoapFaultAction.class, test.getActions().get(0).getClass());

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assertions.assertEquals("soap:send-fault", action.getName());

        Assertions.assertEquals(soapServer, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("", messageBuilder.getMessage().getPayload());
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(2L, action.getFaultDetails().size());
        Assertions.assertEquals("DETAIL1", action.getFaultDetails().get(0));
        Assertions.assertEquals("DETAIL2", action.getFaultDetails().get(1));
        Assertions.assertEquals(FAULT_CODE, action.getFaultCode());
        Assertions.assertEquals(FAULT_STRING, action.getFaultString());
    }
}
