/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.actions;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.xml.namespace.QNameUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class AssertSoapFaultTest extends AbstractBaseTest {
    
    @Autowired 
    SaajSoapMessageFactory saajMessageFactory;
    
    @Test
    public void testAssertSoapFault() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)saajMessageFactory.createWebServiceMessage();
                
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
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)saajMessageFactory.createWebServiceMessage();
                
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
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)saajMessageFactory.createWebServiceMessage();
                
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
            Assert.assertEquals(e.getMessage(), "SOAP fault validation failed! Fault code does not match - expected: '{http://citrusframework.org}ws:TEC-1001' but was: '{http://citrusframework.org}TEC-2002'");
            return;
        }
        
        Assert.fail("Missing validation exception");
    }
    
    @Test
    public void testWrongFaultString() throws Exception {
        AssertSoapFault assertAction = new AssertSoapFault();
        assertAction.setAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                SoapMessage faultMessage = null;
                
                faultMessage = (SaajSoapMessage)saajMessageFactory.createWebServiceMessage();
                
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
}
