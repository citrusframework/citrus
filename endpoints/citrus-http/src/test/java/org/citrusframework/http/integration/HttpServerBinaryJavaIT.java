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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.http.config.annotation.HttpServerConfig;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.SocketUtils;
import org.apache.hc.core5.http.ContentType;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpServerBinaryJavaIT implements TestActionSupport {

    /** Random http server port */
    private final static int serverPort = SocketUtils.findAvailableTcpPort();
    private static final String MEDIA_TYPE_APPLICATION_CUSTOM = "application/custom";

    @CitrusEndpoint(name = "httpClient")
    @HttpClientConfig(requestUrl = "http://localhost:%s/test", binaryMediaTypes = { MEDIA_TYPE_APPLICATION_CUSTOM })
    private HttpClient httpClient;

    @CitrusEndpoint(name = "httpServer")
    @HttpServerConfig(binaryMediaTypes = { MEDIA_TYPE_APPLICATION_CUSTOM })
    private HttpServer httpServer;

    @Test
    @CitrusTest
    public void customMediaTypeAndEncoding(@CitrusResource GherkinTestActionRunner runner) {
        byte[] binaryDataUtf8 = "$&%!!Äöü".getBytes(StandardCharsets.UTF_8);
        byte[] binaryDataLatin1 = "$&%!!Äöü".getBytes(Charset.forName("latin1"));

        httpClient.getEndpointConfiguration().setRequestUrl(String.format(httpClient.getEndpointConfiguration().getRequestUrl(), serverPort));
        httpServer.setPort(serverPort);

        runner.given(start(httpServer));

        runner.run(doFinally().actions(stop(httpServer)));

        runner.given(http().client(httpClient)
                .send()
                .post()
                .fork(true)
                .message(new DefaultMessage(binaryDataUtf8))
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM)
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        runner.when(http().server(httpServer)
                .receive()
                .post("/test")
                .message(new DefaultMessage(binaryDataUtf8))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM)
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM));

        runner.then(http().server(httpServer)
                .send()
                .response(HttpStatus.OK.value())
                .message(new DefaultMessage(binaryDataLatin1))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        runner.then(http().client(httpClient)
                .receive()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryDataLatin1))
                .type(MessageType.BINARY)
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));
    }

    @Test
    @CitrusTest
    public void httpServerBinary(@CitrusResource GherkinTestActionRunner runner) {
        byte[] binaryData = "$&%!!".getBytes();

        runner.given(http().client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        runner.when(http().server("echoHttpServer")
                    .receive()
                    .post("/echo")
                    .message(new DefaultMessage(binaryData))
                    .type(MessageType.BINARY)
                    .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        runner.then(http().server("echoHttpServer")
                .send()
                .response(HttpStatus.OK.value())
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        runner.then(http().client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .message(new DefaultMessage(binaryData))
                .type(MessageType.BINARY)
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
    }
}
