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

package org.citrusframework.ws.integration;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated
public class AssertSoapFaultJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void assertSoapFaultAction(@CitrusResource TestCaseRunner runner) {
        runner.variable("soapFaultCode", "TEC-1001");
        runner.variable("soapFaultString", "Invalid request");

        runner.run(soap().client()
            .assertFault()
            .faultString("Invalid request")
            .faultCode("{http://citrusframework.org/faults}TEC-1001")
            .when(soap().client("helloSoapClient")
                .send()
                .message()
                .body("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ));

        runner.run(soap().client()
            .assertFault()
            .faultString("@ignore@")
            .faultCode("{http://citrusframework.org/faults}TEC-1001")
            .when(soap().client("helloSoapClient")
                .send()
                .message()
                .body("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<ns0:Message>This is invalid</ns0:Message>" +
                        "</ns0:SoapFaultForcingRequest>")
        ));

        runner.run(soap().client()
            .assertFault()
            .faultString("${soapFaultString}")
            .faultCode("{http://citrusframework.org/faults}${soapFaultCode}")
            .when(soap().client("helloSoapClient")
                    .send()
                    .message()
                    .body("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                "<ns0:Message>This is invalid</ns0:Message>" +
                            "</ns0:SoapFaultForcingRequest>")
        ));
    }
}
