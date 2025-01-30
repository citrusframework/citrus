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

package org.citrusframework.http.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_STATUS_CODE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Test
public class HttpServerStandaloneJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void httpServerStandalone() {
        variable("custom_header_id", "123456789");

        given(http().client("httpStandaloneClient")
            .send()
            .post()
            .message()
            .body("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}"));

        when(http().client("httpStandaloneClient")
            .receive()
            .response(OK)
            .message()
            .body("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .version("HTTP/1.1"));

        then(http().client("httpStandaloneClient")
            .send()
            .post()
            .message()
            .body("<moreRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}"));

        then(http().client("httpStandaloneClient")
            .receive()
            .response()
            .message()
            .status(OK)
            .body("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .version("HTTP/1.1"));

        run(echo("Test pure Http status code validation"));

        when(http().client("httpStandaloneClient")
            .send()
            .post()
            .message()
            .body("<moreRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}"));

        then(http().client("httpStandaloneClient")
            .receive()
            .response(OK));

        run(echo("Test Http status code matcher validation"));

        when(http().client("httpStandaloneClient")
                .send()
                .post()
                .message()
                .body("<moreRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}"));

        then(http().client("httpStandaloneClient")
                .receive()
                .response()
                .message()
                .header(HTTP_STATUS_CODE, is(
                        oneOf(CREATED.value(),
                                ACCEPTED.value(),
                                OK.value()))));

        run(echo("Test header validation error"));

        when(http().client("httpStandaloneClient")
            .send()
            .post()
            .message()
            .body("<moreRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}"));

        then(assertException().exception(ValidationException.class).when(
            http().client("httpStandaloneClient")
                .receive()
                .response(NOT_FOUND)
        ));
    }
}
