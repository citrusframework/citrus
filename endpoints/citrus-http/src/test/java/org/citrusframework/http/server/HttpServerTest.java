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

package org.citrusframework.http.server;

import java.util.Random;

import org.apache.hc.core5.http.ContentType;
import org.citrusframework.spring.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Simple unit test for HttpServer
 */
public class HttpServerTest extends AbstractUnitTest {

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private HttpClient client;
    private final HttpServer server = new HttpServer();

    @Autowired
    private EndpointAdapter mockResponseEndpointAdapter;

    @BeforeEach
    public void setupClient() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        endpointConfiguration.setRequestUrl(uri);
        client = new HttpClient(endpointConfiguration);

        server.setPort(port);
        server.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext));
        server.setUseRootContextAsParent(true);
        server.setContextConfigLocation("classpath:org/citrusframework/http/HttpServerTest-http-servlet.xml");

        server.startup();
    }

    @AfterEach
    public void shutdown() {
        server.shutdown();

        try {
            client.send(new HttpMessage().method(HttpMethod.GET), context);

            fail("Server supposed to be in shutdown state, but was accessible via client request");
        } catch (ResourceAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetRequest() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assertions.assertTrue(request instanceof HttpMessage);
            Assertions.assertEquals("", request.getPayload(String.class));
            Assertions.assertNull(request.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));
            Assertions.assertEquals("/test/hello", request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI));

            return new HttpMessage("Hello user")
                            .status(HttpStatus.OK);
        });

        client.send(new HttpMessage()
                            .path("/hello")
                            .method(HttpMethod.GET), context);

        Message response = client.receive(context);

        Assertions.assertEquals("Hello user", response.getPayload(String.class));
        Assertions.assertEquals(9L, response.getHeaders().size());
        Assertions.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpStatus.OK.value(), response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals(HttpStatus.OK.getReasonPhrase().toUpperCase(), response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals(ContentType.TEXT_PLAIN.getMimeType() + ";charset=utf-8", response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testPostRequest() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assertions.assertTrue(request instanceof HttpMessage);
            Assertions.assertEquals("Hello", request.getPayload(String.class));

            return new HttpMessage().status(HttpStatus.FOUND);
        });

        client.send(new HttpMessage("Hello")
                            .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assertions.assertEquals(9L, response.getHeaders().size());
        Assertions.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpStatus.FOUND.value(), response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals(HttpStatus.FOUND.getReasonPhrase().toUpperCase(), response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals(ContentType.TEXT_PLAIN.getMimeType() + ";charset=utf-8", response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testBinaryRequestResponse() {
        TestContext context = testContextFactory.getObject();

        final byte[] requestBody = new byte[20];
        new Random().nextBytes(requestBody);

        final byte[] responseBody = new byte[20];
        new Random().nextBytes(responseBody);

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assertions.assertTrue(request instanceof HttpMessage);
            Assertions.assertArrayEquals(requestBody, request.getPayload(byte[].class));

            return new HttpMessage(responseBody)
                            .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .status(HttpStatus.OK);
        });

        client.send(new HttpMessage(requestBody)
                            .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assertions.assertArrayEquals(responseBody, response.getPayload(byte[].class));
        Assertions.assertEquals(9L, response.getHeaders().size());
        Assertions.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpStatus.OK.value(), response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals(HttpStatus.OK.getReasonPhrase().toUpperCase(), response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals(ContentType.APPLICATION_OCTET_STREAM.getMimeType(), response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testCustomContentType() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assertions.assertTrue(request instanceof HttpMessage);
            Assertions.assertEquals("Hello", request.getPayload(String.class));
            Assertions.assertEquals("application/foo", request.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));
            Assertions.assertEquals("/test/hello", request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI));

            return new HttpMessage("Hello user")
                    .contentType("application/bar")
                    .status(HttpStatus.OK);
        });

        client.send(new HttpMessage("Hello")
                .path("/hello")
                .contentType("application/foo")
                .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assertions.assertEquals("Hello user", response.getPayload(String.class));
        Assertions.assertEquals(9L, response.getHeaders().size());
        Assertions.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpStatus.OK.value(), response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals(HttpStatus.OK.getReasonPhrase().toUpperCase(), response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals("application/bar", response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testHttpResponseErrorHandlerWithPropagateStrategy() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        doReturn(new HttpMessage().status(INTERNAL_SERVER_ERROR))
                .when(mockResponseEndpointAdapter).handleMessage(any(Message.class));

        client.send(new HttpMessage()
                .path("/hello")
                .method(HttpMethod.GET), context);

        Message response = client.receive(context);

        assertThat(response)
                .isInstanceOf(HttpMessage.class)
                .satisfies(
                        r -> assertThat(((HttpMessage) r).getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR)
                );

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }
}
