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

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.xml.transform.StringSource;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class XmlSoapFaultValidatorTest extends AbstractTestNGUnitTest {
    
    @Autowired
    private XmlSoapFaultValidator soapFaultValidator;
    
    private String detail = "<ws:message-sender " +
    		"xmlns:ws=\"http://www.citrusframework.org/schema/ws/config\" " +
    		"id=\"fooMsgSender\" " +
    		"request-url=\"http://foo.org/test\"/>";
    @Test
    public void testXmlDetailValidation() {
        soapFaultValidator.validateFaultDetailString(detail, detail, context);
    }
    
    @Test
    public void testFaultDetailTranslation() {
        SoapFaultDetail receivedDetail = EasyMock.createMock(SoapFaultDetail.class);
        SoapFaultDetail controlDetail = EasyMock.createMock(SoapFaultDetail.class);
        
        reset(receivedDetail, controlDetail);
        
        expect(receivedDetail.getSource()).andReturn(new StringSource("<detail>" + detail + "</detail>")).once();
        expect(controlDetail.getSource()).andReturn(new StringSource(detail)).once();
        
        replay(receivedDetail, controlDetail);
        
        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context);
        
        verify(receivedDetail, controlDetail);
    }
}
