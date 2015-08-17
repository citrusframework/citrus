/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class XMLValidationMatcherJavaITest extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void xmlValidationMatcher() {
        variable("greetingText", "Hello Citrus");      
        
        parallel(
            send("httpClient")
                .payload("<testRequestMessage>" +
                            "<text>citrus:cdataSection('<data>" +
                              "<greeting>Hello Citrus</greeting>" +
                              "<timestamp>2012-07-01T00:00:00</timestamp>" +
                            "</data>')</text>" +
                        "</testRequestMessage>")
                .header("Content-Type", "text/xml")
                .header("Accept", "text/xml, */*")
                .header("citrus_http_method", "POST"),
            sequential(
                receive("httpServerRequestEndpoint")
                    .payload("<testRequestMessage>" +
                                    "<text>citrus:cdataSection('@matchesXml('<data>" +
                                  "<greeting>${greetingText}</greeting>" +
                                  "<timestamp>@ignore@</timestamp>" +
                                "</data>')@')</text>" +
                                "</testRequestMessage>")
                    .header("Content-Type", "text/xml")
                    .header("Accept", "text/xml, */*")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .header("citrus_http_method", "POST")
                    .header("citrus_http_request_uri", "/test")
                    .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                send("httpServerResponseEndpoint")
                    .payload("<testResponseMessage>" +
                                    "<text>Hello Citrus</text>" +
                                "</testResponseMessage>")
                    .header("citrus_http_status_code", "200")
                    .header("citrus_http_version", "HTTP/1.1")
                    .header("citrus_http_reason_phrase", "OK")
                    .header("Content-Type", "text/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );
        
        receive("httpClient")
            .payload("<testResponseMessage>" +
                        "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
             .header("citrus_http_status_code", "200")
             .header("citrus_http_version", "HTTP/1.1")
             .header("citrus_http_reason_phrase", "OK");
    }
}