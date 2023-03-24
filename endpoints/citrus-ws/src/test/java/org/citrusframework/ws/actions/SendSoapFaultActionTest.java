/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.actions;

import java.util.Locale;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.message.SoapFault;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionTest extends UnitTestSupport {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFault() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction.Builder()
                .endpoint(endpoint)
                .message()
                .faultCode("{http://citrusframework.org}ws:TEC-1000")
                .faultString("Internal server error")
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message sentMessage = (Message)invocation.getArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        sendSoapFaultAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultWithActor() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction.Builder()
                .endpoint(endpoint)
                .message()
                .faultCode("{http://citrusframework.org}ws:TEC-1000")
                .faultString("Internal server error")
                .faultActor("SERVER")
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message sentMessage = (Message)invocation.getArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                Assert.assertEquals(soapFault.getFaultActor(), "SERVER");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        sendSoapFaultAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultMissingFaultString() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction.Builder()
                .endpoint(endpoint)
                .message()
                .faultCode("{http://citrusframework.org}ws:TEC-1000")
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message sentMessage = (Message)invocation.getArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertNull(soapFault.getFaultString());
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        sendSoapFaultAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultWithVariableSupport() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction.Builder()
                .endpoint(endpoint)
                .message()
                .faultCode("citrus:concat('{http://citrusframework.org}ws:', ${faultCode})")
                .faultString("${faultString}")
                .build();

        context.setVariable("faultCode", "TEC-1000");
        context.setVariable("faultString", "Internal server error");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message sentMessage = (Message)invocation.getArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        sendSoapFaultAction.execute(context);

    }

    @Test
    public void testSendSoapFaultMissingFaultCode() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction.Builder()
                .endpoint(endpoint)
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(endpoint.getActor()).thenReturn(null);

        try {
            sendSoapFaultAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of missing SOAP fault code");
    }
}
