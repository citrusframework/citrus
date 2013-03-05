/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ws.validation;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.xml.transform.StringSource;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class XmlSoapFaultValidatorTest extends AbstractTestNGUnitTest {
    
    @Autowired
    private XmlSoapFaultValidator soapFaultValidator;
    
    private String error = "<ws:Error xmlns:ws=\"http://www.citrusframework.org/schema/ws/fault\" " +
    		"type=\"INTERNAL\">" +
    		    "<ws:code>1001</ws:code>" +
    		    "<ws:message>Something went wrong</ws:message>" +
    		"</ws:Error>";
    
    private String detail = "<ws:ErrorDetails xmlns:ws=\"http://www.citrusframework.org/schema/ws/fault\">" +
                "<ws:stacktrace>N/A</ws:stacktrace>" +
            "</ws:ErrorDetails>";
    
    @Test
    public void testXmlDetailValidation() {
        soapFaultValidator.validateFaultDetailString(detail, detail, context, new XmlMessageValidationContext());
    }
    
    @Test
    public void testFaultDetailTranslation() {
        SoapFaultDetail receivedDetail = EasyMock.createMock(SoapFaultDetail.class);
        SoapFaultDetail controlDetail = EasyMock.createMock(SoapFaultDetail.class);
        
        SoapFaultDetailElement receivedDetailElement = EasyMock.createMock(SoapFaultDetailElement.class);
        List<SoapFaultDetailElement> receivedDetailElements = new ArrayList<SoapFaultDetailElement>();
        receivedDetailElements.add(receivedDetailElement);
        
        SoapFaultDetailElement controlDetailElement = EasyMock.createMock(SoapFaultDetailElement.class);
        List<SoapFaultDetailElement> controlDetailElements = new ArrayList<SoapFaultDetailElement>();
        controlDetailElements.add(controlDetailElement);
        
        reset(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
        
        expect(receivedDetail.getDetailEntries()).andReturn(receivedDetailElements.iterator()).once();
        expect(controlDetail.getDetailEntries()).andReturn(controlDetailElements.iterator()).once();
        expect(receivedDetailElement.getSource()).andReturn(new StringSource(error)).once();
        expect(controlDetailElement.getSource()).andReturn(new StringSource(error)).once();
        
        replay(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
        
        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new XmlMessageValidationContext());
        
        verify(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
    }
    
    @Test
    public void testMultipleFaultDetailTranslation() {
        SoapFaultDetail receivedDetail = EasyMock.createMock(SoapFaultDetail.class);
        SoapFaultDetail controlDetail = EasyMock.createMock(SoapFaultDetail.class);
        
        SoapFaultDetailElement receivedDetailElement = EasyMock.createMock(SoapFaultDetailElement.class);
        List<SoapFaultDetailElement> receivedDetailElements = new ArrayList<SoapFaultDetailElement>();
        receivedDetailElements.add(receivedDetailElement);
        receivedDetailElements.add(receivedDetailElement);
        
        SoapFaultDetailElement controlDetailElement = EasyMock.createMock(SoapFaultDetailElement.class);
        List<SoapFaultDetailElement> controlDetailElements = new ArrayList<SoapFaultDetailElement>();
        controlDetailElements.add(controlDetailElement);
        controlDetailElements.add(controlDetailElement);
        
        reset(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
        
        expect(receivedDetail.getDetailEntries()).andReturn(receivedDetailElements.iterator()).once();
        expect(controlDetail.getDetailEntries()).andReturn(controlDetailElements.iterator()).once();
        expect(receivedDetailElement.getSource()).andReturn(new StringSource(error)).once();
        expect(controlDetailElement.getSource()).andReturn(new StringSource(error)).once();
        
        expect(receivedDetailElement.getSource()).andReturn(new StringSource(detail)).once();
        expect(controlDetailElement.getSource()).andReturn(new StringSource(detail)).once();
        
        replay(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
        
        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new XmlMessageValidationContext());
        
        verify(receivedDetail, controlDetail, receivedDetailElement, controlDetailElement);
    }
}
