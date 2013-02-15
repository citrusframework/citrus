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

package com.consol.citrus.http;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

/**
 * @author Christoph Deppisch
 */
public class HttpServerStandaloneJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    protected void configure() {
        variable("custom_header_id", "123456789");
        
        send("httpMessageSenderStandalone")
            .payload("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}");
        
        receive("httpResponseReceiver")
            .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
        
        send("httpMessageSenderStandalone")
            .payload("<moreRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}");
        
        receive("httpResponseReceiver")
            .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
    }
    
    @Test
    public void httpServerStandaloneITest(ITestContext testContext) {
        executeTest(testContext);
    }
}