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

package com.consol.citrus.javadsl;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    public void configure() {
        variable("soapFaultCode", "TEC-1001");
        variable("soapFaultString", "Invalid request");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("Invalid request")
        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("@ignore@")
        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("${soapFaultString}")
        .faultCode("{http://www.citrusframework.org/faults}${soapFaultCode}");
    }
    
    @Test
    public void echoActionITest(ITestContext testContext) {
        executeTest(testContext);
    }
}