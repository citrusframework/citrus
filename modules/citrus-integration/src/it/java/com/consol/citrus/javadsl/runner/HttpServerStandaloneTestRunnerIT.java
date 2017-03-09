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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerStandaloneTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void httpServerStandalone() {
        variable("custom_header_id", "123456789");
        
        http(action -> action.client("httpStandaloneClient")
                .send()
                .post()
                .payload("<testRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}"));
        
        http(action -> action.client("httpStandaloneClient")
                .receive()
                .response(HttpStatus.OK)
                .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                        "</testResponseMessage>")
                .version("HTTP/1.1"));
        
        http(action -> action.client("httpStandaloneClient")
                .send()
                .post()
                .payload("<moreRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}"));
        
        http(action -> action.client("httpStandaloneClient")
                .receive()
                .response(HttpStatus.OK)
                .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                        "</testResponseMessage>")
                .version("HTTP/1.1"));

        echo("Test pure Http status code validation");

        http(action -> action.client("httpStandaloneClient")
                .send()
                .post()
                .payload("<moreRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}"));

        http(action -> action.client("httpStandaloneClient")
                .receive()
                .response(HttpStatus.OK));

        echo("Test header validation error");

        http(action -> action.client("httpStandaloneClient")
                .send()
                .post()
                .payload("<moreRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}"));

        assertException().exception(ValidationException.class).when(
                http(action -> action.client("httpStandaloneClient")
                        .receive()
                        .response(HttpStatus.NOT_FOUND)
        ));
    }
}