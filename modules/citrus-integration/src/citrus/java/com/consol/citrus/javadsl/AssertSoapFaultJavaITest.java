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

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class AssertSoapFaultJavaITest extends TestNGCitrusTestBuilder {
    
    @CitrusTest
    public void AssertSoapFaultJavaITest() {
        variable("soapFaultCode", "TEC-1001");
        variable("soapFaultString", "Invalid request");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("Invalid request")
        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("@ignore@")
        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
        
        assertSoapFault(
            send("webServiceHelloRequestSender")
                .soap()
                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ).faultString("${soapFaultString}")
        .faultCode("{http://www.citrusframework.org/faults}${soapFaultCode}");
    }
}