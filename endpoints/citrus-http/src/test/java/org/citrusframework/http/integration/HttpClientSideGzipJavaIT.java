/*
 * Copyright 2006-2018 the original author or authors.
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
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.interceptor.GzipMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpClientSideGzipJavaIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("echoHttpClient")
    private HttpClient client;

    @Autowired
    @Qualifier("echoHttpServer")
    private HttpServer server;

    @CitrusTest
    public void testClientSideGzipCompression(){

        String text = "This is the text";

        given(http().client(client)
                .send()
                .post()
                .message()
                .contentType(MediaType.TEXT_HTML_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .process(new GzipMessageProcessor())
                .body(text)
                .fork(true));

        when(http().server(server)
                .receive()
                .post("/echo")
                .message()
                .type(MessageType.PLAINTEXT)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .body(text));

        then(http().server(server)
                .respond()
                .message()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip"));

        then(http().client(client)
                .receive()
                .response()
                .message()
                .contentType(MediaType.TEXT_PLAIN_VALUE));
    }
}
