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

package com.consol.citrus.http.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.TestNGCitrusSupport;
import com.consol.citrus.validation.interceptor.GzipMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpClientSideGzipJavaIT extends TestNGCitrusSupport {

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
                .contentType(MediaType.TEXT_HTML_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .process(new GzipMessageProcessor())
                .payload(text)
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
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip"));

        then(http().client(client)
                .receive()
                .response()
                .message()
                .contentType(MediaType.TEXT_PLAIN_VALUE));
    }
}
