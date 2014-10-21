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

package com.consol.citrus.ws.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.message.SoapFault;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = EasyMock.createMock(Endpoint.class);
    private Producer producer = EasyMock.createMock(Producer.class);
    private EndpointConfiguration endpointConfiguration = EasyMock.createMock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFault() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setEndpoint(endpoint);
        
        sendSoapFaultAction.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        sendSoapFaultAction.setFaultString("Internal server error");
        
        reset(endpoint, producer, endpointConfiguration);
        expect(endpoint.createProducer()).andReturn(producer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                
                return null;
            }
        }).once();
        
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, producer, endpointConfiguration);
        
        sendSoapFaultAction.execute(context);

        verify(endpoint, producer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultWithActor() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setEndpoint(endpoint);
        
        sendSoapFaultAction.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        sendSoapFaultAction.setFaultString("Internal server error");
        sendSoapFaultAction.setFaultActor("SERVER");
        
        reset(endpoint, producer, endpointConfiguration);
        expect(endpoint.createProducer()).andReturn(producer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                Assert.assertEquals(soapFault.getFaultActor(), "SERVER");
                
                return null;
            }
        }).once();
        
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, producer, endpointConfiguration);
        
        sendSoapFaultAction.execute(context);

        verify(endpoint, producer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultMissingFaultString() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setEndpoint(endpoint);
        
        sendSoapFaultAction.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        
        reset(endpoint, producer, endpointConfiguration);
        expect(endpoint.createProducer()).andReturn(producer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertNull(soapFault.getFaultString());
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                
                return null;
            }
        }).once();
        
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, producer, endpointConfiguration);
        
        sendSoapFaultAction.execute(context);

        verify(endpoint, producer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultWithVariableSupport() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setEndpoint(endpoint);
        
        sendSoapFaultAction.setFaultCode("citrus:concat('{http://citrusframework.org}ws:', ${faultCode})");
        sendSoapFaultAction.setFaultString("${faultString}");
        
        context.setVariable("faultCode", "TEC-1000");
        context.setVariable("faultString", "Internal server error");

        reset(endpoint, producer, endpointConfiguration);
        expect(endpoint.createProducer()).andReturn(producer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertTrue(sentMessage instanceof SoapFault);

                SoapFault soapFault = (SoapFault) sentMessage;
                Assert.assertEquals(soapFault.getFaultCode(), "{http://citrusframework.org}ws:TEC-1000");
                Assert.assertEquals(soapFault.getFaultString(), "Internal server error");
                Assert.assertEquals(soapFault.getLocale(), Locale.ENGLISH);
                
                return null;
            }
        }).once();
        
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, producer, endpointConfiguration);
        
        sendSoapFaultAction.execute(context);

        verify(endpoint, producer, endpointConfiguration);
    }
    
    @Test
    public void testSendSoapFaultMissingFaultCode() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setEndpoint(endpoint);
        
        reset(endpoint, producer, endpointConfiguration);
        expect(endpoint.createProducer()).andReturn(producer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, producer, endpointConfiguration);
        
        try {
            sendSoapFaultAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
            verify(endpoint, producer, endpointConfiguration);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of missing SOAP fault code");
    }
}
