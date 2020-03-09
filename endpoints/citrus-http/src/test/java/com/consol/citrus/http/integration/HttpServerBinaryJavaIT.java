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

import java.nio.charset.Charset;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.config.annotation.HttpClientConfig;
import com.consol.citrus.http.config.annotation.HttpServerConfig;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.util.SocketUtils;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.StartServerAction.Builder.start;
import static com.consol.citrus.actions.StopServerAction.Builder.stop;
import static com.consol.citrus.container.FinallySequence.Builder.doFinally;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerBinaryJavaIT extends TestNGCitrusSupport {

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
        byte[] binaryDataUtf8 = "$&%!!Äöü".getBytes(Charset.forName("utf-8"));
        byte[] binaryDataLatin1 = "$&%!!Äöü".getBytes(Charset.forName("latin1"));

        httpClient.getEndpointConfiguration().setRequestUrl(String.format(httpClient.getEndpointConfiguration().getRequestUrl(), serverPort));
        httpServer.setPort(serverPort);

        given(start(httpServer));

        given(http().client(httpClient)
                .send()
                .post()
                .fork(true)
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryDataUtf8))
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        when(http().server(httpServer)
                .receive()
                .post("/test")
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryDataUtf8))
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM)
                .accept(MEDIA_TYPE_APPLICATION_CUSTOM));

        then(http().server(httpServer)
                .send()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryDataLatin1))
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        then(http().client(httpClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryDataLatin1))
                .contentType(MEDIA_TYPE_APPLICATION_CUSTOM));

        run(doFinally().actions(stop(httpServer)));
    }

    @CitrusTest
    public void httpServerBinary() {
        byte[] binaryData = "$&%!!".getBytes();

        given(http().client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        when(http().server("echoHttpServer")
                    .receive()
                    .post("/echo")
                    .messageType(MessageType.BINARY)
                    .message(new DefaultMessage(binaryData))
                    .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        then(http().server("echoHttpServer")
                .send()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        then(http().client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
    }
}
