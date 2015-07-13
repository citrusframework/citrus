/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.AssertSoapFaultBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.runner.TestActionConfigurer;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class AssertSoapFaultTestRunnerITest extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void AssertSoapFaultTestRunnerITest() {
        variable("soapFaultCode", "TEC-1001");
        variable("soapFaultString", "Invalid request");
        
        assertSoapFault(new TestActionConfigurer<AssertSoapFaultBuilder>() {
            @Override
            public void configure(AssertSoapFaultBuilder builder) {
                builder.faultString("Invalid request")
                        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
            }
        }).when(send(new TestActionConfigurer<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("webServiceHelloClient")
                                .soap()
                                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Message>This is invalid</ns0:Message>" +
                                        "</ns0:SoapFaultForcingRequest>");
                    }
                }));
        
        assertSoapFault(new TestActionConfigurer<AssertSoapFaultBuilder>() {
            @Override
            public void configure(AssertSoapFaultBuilder builder) {
                builder.faultString("@ignore@")
                        .faultCode("{http://www.citrusframework.org/faults}TEC-1001");
            }
        }).when(send(new TestActionConfigurer<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("webServiceHelloClient")
                                .soap()
                                .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Message>This is invalid</ns0:Message>" +
                                        "</ns0:SoapFaultForcingRequest>");
                    }
                }));

        assertSoapFault(new TestActionConfigurer<AssertSoapFaultBuilder>() {
            @Override
            public void configure(AssertSoapFaultBuilder builder) {
                builder.faultString("${soapFaultString}")
                        .faultCode("{http://www.citrusframework.org/faults}${soapFaultCode}");
            }
        }).when(send(new TestActionConfigurer<SendMessageBuilder>() {
                     @Override
                     public void configure(SendMessageBuilder builder) {
                         builder.endpoint("webServiceHelloClient")
                                 .soap()
                                 .payload("<ns0:SoapFaultForcingRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                         "<ns0:Message>This is invalid</ns0:Message>" +
                                         "</ns0:SoapFaultForcingRequest>");
                     }
                }));
    }
}