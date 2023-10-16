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

package org.citrusframework.ws.validation;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.message.SoapFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

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
        soapFaultValidator.validateFaultDetailString(detail, detail, context, new SoapFaultDetailValidationContext());
    }

    @Test
    public void testFaultDetailValidation() {
        SoapFault receivedDetail = new SoapFault();
        receivedDetail.addFaultDetail(error);
        SoapFault controlDetail = new SoapFault();
        controlDetail.addFaultDetail(error);

        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new SoapFaultValidationContext());
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Node value not equal for element 'code', expected '1001' but was '1002'")
    public void testFaultDetailValidationError() {
        SoapFault receivedDetail = new SoapFault();
        receivedDetail.addFaultDetail(error.replaceFirst("1001", "1002"));
        SoapFault controlDetail = new SoapFault();
        controlDetail.addFaultDetail(error);

        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new SoapFaultValidationContext());
    }

    @Test
    public void testMultipleFaultDetailValidation() {
        SoapFault receivedDetail = new SoapFault();
        receivedDetail.addFaultDetail(error);
        receivedDetail.addFaultDetail(detail);
        SoapFault controlDetail = new SoapFault();
        controlDetail.addFaultDetail(error);
        controlDetail.addFaultDetail(detail);

        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new SoapFaultValidationContext());
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Node value not equal for element 'code', expected '1002' but was '1001'")
    public void testMultipleFaultDetailValidationError() {
        SoapFault receivedDetail = new SoapFault();
        receivedDetail.addFaultDetail(error);
        receivedDetail.addFaultDetail(detail);
        SoapFault controlDetail = new SoapFault();
        controlDetail.addFaultDetail(error.replaceFirst("1001", "1002"));
        controlDetail.addFaultDetail(detail);

        soapFaultValidator.validateFaultDetail(receivedDetail, controlDetail, context, new SoapFaultValidationContext());
    }
}
