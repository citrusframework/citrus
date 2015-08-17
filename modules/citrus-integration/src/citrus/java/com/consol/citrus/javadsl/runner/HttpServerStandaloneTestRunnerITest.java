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
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerStandaloneTestRunnerITest extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void httpServerStandalone() {
        variable("custom_header_id", "123456789");
        
        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("httpStandaloneClient")
                        .payload("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                                "</testRequestMessage>")
                        .header("CustomHeaderId", "${custom_header_id}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("httpStandaloneClient")
                        .payload("<testResponseMessage>" +
                                "<text>Hello TestFramework</text>" +
                                "</testResponseMessage>")
                        .header("citrus_http_status_code", "200")
                        .header("citrus_http_version", "HTTP/1.1")
                        .header("citrus_http_reason_phrase", "OK");
            }
        });
        
        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("httpStandaloneClient")
                        .payload("<moreRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                                "</moreRequestMessage>")
                        .header("CustomHeaderId", "${custom_header_id}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("httpStandaloneClient")
                        .payload("<testResponseMessage>" +
                                "<text>Hello TestFramework</text>" +
                                "</testResponseMessage>")
                        .header("citrus_http_status_code", "200")
                        .header("citrus_http_version", "HTTP/1.1")
                        .header("citrus_http_reason_phrase", "OK");
            }
        });
    }
}