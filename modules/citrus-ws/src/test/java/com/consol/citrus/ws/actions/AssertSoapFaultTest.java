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

import java.util.Locale;

import javax.xml.transform.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.ws.validation.SoapFaultValidator;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultTest extends AbstractBaseTest {
    
    @Autowired 
    SaajSoapMessageFactory messageFactory;
    
    @Autowired
    SoapFaultValidator soapFaultValidator;
    
    @Test
    public void testAssertSoapFault() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
     
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        
        assertAction.execute(context);
    }
    
    @Test
    public void testNoPrefix() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
        
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}TEC-1001");
        
        assertAction.execute(context);
    }
    
    @Test
    public void testWrongFaultCode() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
        
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-2002"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        
        try {
            assertAction.execute(context);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "SOAP fault validation failed! Fault code does not match - expected: '{http://citrusframework.org}TEC-1001' but was: '{http://citrusframework.org}TEC-2002'");
            return;
        }
        
        Assert.fail("Missing validation exception");
    }
    
    @Test
    public void testWrongFaultString() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
        
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Invalid request");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        
        try {
            assertAction.execute(context);
        } catch(ValidationException e) {
            Assert.assertEquals(e.getMessage(), "SOAP fault validation failed! Fault string does not match - expected: 'Invalid request' but was: 'Internal server error'");
            return;
        }
        
        Assert.fail("Missing validation exception");
    }
    
    @Test
    public void testAssertSoapFaultDetail() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
     
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    
                    transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException(e);
                }
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        assertAction.setFaultDetail("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>");
        
        assertAction.execute(context);
    }
    
    @Test
    public void testAssertSoapFaultDetailVariableSupport() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
     
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    
                    transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException(e);
                }
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        context.setVariable("faultReason", "Invalid request");
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        assertAction.setFaultDetail("<FaultDetail><Reason>${faultReason}</Reason></FaultDetail>");
        
        assertAction.execute(context);
    }
    
    @Test
    public void testAssertSoapFaultDetailResource() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
     
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    
                    transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException(e);
                }
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        assertAction.setFaultDetailResource(new ClassPathResource("test-fault-detail.xml", AssertSoapFaultTest.class));
        
        assertAction.execute(context);
    }
    
    @Test
    public void testAssertSoapFaultDetailResourceVariableSupport() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setValidator(soapFaultValidator);
        assertAction.setMessageFactory(messageFactory);
     
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)messageFactory.createWebServiceMessage();
                
                SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"), 
                        "Internal server error", 
                        Locale.GERMANY);
                
                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    
                    transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException(e);
                }
                
                throw new SoapFaultClientException(faultMessage);
            }
        });
        
        context.setVariable("faultReason", "Invalid request");
        
        assertAction.setFaultString("Internal server error");
        assertAction.setFaultCode("{http://citrusframework.org}ws:TEC-1001");
        assertAction.setFaultDetailResource(new ClassPathResource("test-fault-detail-with-variables.xml", AssertSoapFaultTest.class));
        
        assertAction.execute(context);
    }
}
