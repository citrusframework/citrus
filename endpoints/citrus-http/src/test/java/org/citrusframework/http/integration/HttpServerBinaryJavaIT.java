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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.http.config.annotation.HttpServerConfig;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.apache.hc.core5.http.ContentType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.actions.StartServerAction.Builder.start;
import static org.citrusframework.actions.StopServerAction.Builder.stop;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerBinaryJavaIT extends TestNGCitrusSpringSupport {

    /** Random http server port */
    private final static int serverPort = SocketUtils.findAvailableTcpPort();
    private static final String MEDIA_TYPE_APPLICATION_CUSTOM = "application/custom";

    @CitrusEndpoint(name = "httpClient")
    @HttpClientConfig(requestUrl = "http://localhost:%s/test", binaryMediaTypes = { MEDIA_TYPE_APPLICATION_CUSTOM })
    private HttpClient httpClient;

    @CitrusEndpoint(name = "httpServer")
    @HttpServerConfig(binaryMediaTypes = { MEDIA_TYPE_APPLICATION_CUSTOM })
    private HttpServer httpServer;

    @CitrusTest
    public void customMediaTypeAndEncoding() {
        byte[] binaryDataUtf8 = "$&%!!Äöü".getBytes(StandardCharsets.UTF_8);
        byte[] binaryDataLatin1 = "$&%!!Äöü".getBytes(Charset.forName("latin1"));

        httpClient.getEndpointConfiguration().setRequestUrl(String.format(httpClient.getEndpointConfiguration().getRequestUrl(), serverPort));
        httpServer.setPort(serverPort);

        given(start(httpServer));

        run(doFinally().actions(stop(httpServer)));

        given(http().client(httpClient)
                .send()
                .post()
                .fork(true)
                .message(new DefaultMessage(binaryDataUtf8))
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM)
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        when(http().server(httpServer)
                .receive()
                .post("/test")
                .message(new DefaultMessage(binaryDataUtf8))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM)
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM));

        then(http().server(httpServer)
                .send()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryDataLatin1))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        then(http().client(httpClient)
                .receive()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryDataLatin1))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));
    }

    @CitrusTest
    public void httpServerBinary() {
        byte[] binaryData = "$&%!!".getBytes();

        given(http().client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        when(http().server("echoHttpServer")
                    .receive()
                    .post("/echo")
                    .message(new DefaultMessage(binaryData))
                    .type(MessageType.BINARY)
                    .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        then(http().server("echoHttpServer")
                .send()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        then(http().client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
    }
}
