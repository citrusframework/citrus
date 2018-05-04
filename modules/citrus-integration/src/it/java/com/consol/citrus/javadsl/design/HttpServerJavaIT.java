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
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void httpServer() {
        variable("custom_header_id", "123456789");
        
        echo("Send Http message and respond with 200 OK");
        
        parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .payload("<testRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}")
                .contentType("application/xml")
                .accept("application/xml"),
            
            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .post("/test")
                    .payload("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .contentType("application/xml")
                    .accept("application/xml")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                    
               http().server("httpServerResponseEndpoint")
                   .send()
                   .response(HttpStatus.OK)
                   .payload("<testResponseMessage>" +
                                "<text>Hello Citrus</text>" +
                            "</testResponseMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .version("HTTP/1.1")
                    .contentType("application/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );
        
        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .payload("<testResponseMessage>" +
                    "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .version("HTTP/1.1");

        echo("Send Http request and respond with 404 status code");
        
        parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .payload("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}")
                .contentType("application/xml")
                .accept("application/xml"),
            
            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .post()
                    .path("/test")
                    .payload("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .contentType("application/xml")
                    .accept("application/xml")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                    
               http().server("httpServerResponseEndpoint")
                   .send()
                   .response()
                   .status(HttpStatus.NOT_FOUND)
                   .payload("<testResponseMessage>" +
                                "<text>Hello Citrus</text>" +
                            "</testResponseMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .version("HTTP/1.1")
                    .contentType("application/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );
        
        http().client("httpClient")
            .receive()
            .response()
            .status(HttpStatus.NOT_FOUND)
            .payload("<testResponseMessage>" +
                        "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .version("HTTP/1.1");
        
        echo("Skip response and use fallback endpoint adapter");
        
        http().client("httpClient")
            .send()
            .post()
            .payload("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .contentType("application/xml")
            .accept("application/xml");

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .version("HTTP/1.1")
            .timeout(2000L);

    }
}