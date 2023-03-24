/*
 * Copyright 2006-2012 the original author or authors.
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

import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.SocketUtils;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Simple unit test for HttpServer
 * @author jza
 */
public class HttpServerTest extends AbstractTestNGUnitTest {

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private HttpClient client;
    private final HttpServer server = new HttpServer();

    @Autowired
    private EndpointAdapter mockResponseEndpointAdapter;

    @BeforeClass
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

    @AfterClass(alwaysRun = true)
    public void shutdown() {
        server.shutdown();

        try {
            client.send(new HttpMessage()
                    .method(HttpMethod.GET), context);

            Assert.fail("Server supposed to be in shutdown state, but was accessible via client request");
        } catch (ResourceAccessException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetRequest() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assert.assertTrue(request instanceof HttpMessage);
            Assert.assertEquals(request.getPayload(String.class), "");
            Assert.assertEquals(request.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/plain;charset=UTF-8");
            Assert.assertEquals(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/hello");

            return new HttpMessage("Hello user")
                            .status(HttpStatus.OK);
        });

        client.send(new HttpMessage()
                            .path("/hello")
                            .method(HttpMethod.GET), context);

        Message response = client.receive(context);

        Assert.assertEquals(response.getPayload(String.class), "Hello user");
        Assert.assertEquals(response.getHeaders().size(), 9L);
        Assert.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assert.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK.value());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE), HttpStatus.OK.getReasonPhrase().toUpperCase());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), ContentType.TEXT_PLAIN.getMimeType() + ";charset=utf-8");

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testPostRequest() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assert.assertTrue(request instanceof HttpMessage);
            Assert.assertEquals(request.getPayload(String.class), "Hello");

            return new HttpMessage().status(HttpStatus.FOUND);
        });

        client.send(new HttpMessage("Hello")
                            .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assert.assertEquals(response.getHeaders().size(), 9L);
        Assert.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assert.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.FOUND.value());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE), HttpStatus.FOUND.getReasonPhrase().toUpperCase());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), ContentType.TEXT_PLAIN.getMimeType() + ";charset=utf-8");

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

            Assert.assertTrue(request instanceof HttpMessage);
            Assert.assertEquals(request.getPayload(byte[].class), requestBody);

            return new HttpMessage(responseBody)
                            .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .status(HttpStatus.OK);
        });

        client.send(new HttpMessage(requestBody)
                            .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                            .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assert.assertEquals(response.getPayload(byte[].class), responseBody);
        Assert.assertEquals(response.getHeaders().size(), 9L);
        Assert.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assert.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK.value());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE), HttpStatus.OK.getReasonPhrase().toUpperCase());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), ContentType.APPLICATION_OCTET_STREAM.getMimeType());

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }

    @Test
    public void testCustomContentType() {
        TestContext context = testContextFactory.getObject();

        reset(mockResponseEndpointAdapter);
        when(mockResponseEndpointAdapter.handleMessage(any(Message.class))).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);

            Assert.assertTrue(request instanceof HttpMessage);
            Assert.assertEquals(request.getPayload(String.class), "Hello");
            Assert.assertEquals(request.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/foo");
            Assert.assertEquals(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/hello");

            return new HttpMessage("Hello user")
                    .contentType("application/bar")
                    .status(HttpStatus.OK);
        });

        client.send(new HttpMessage("Hello")
                .path("/hello")
                .contentType("application/foo")
                .method(HttpMethod.POST), context);

        Message response = client.receive(context);

        Assert.assertEquals(response.getPayload(String.class), "Hello user");
        Assert.assertEquals(response.getHeaders().size(), 9L);
        Assert.assertNotNull(response.getHeader(MessageHeaders.ID));
        Assert.assertNotNull(response.getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK.value());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE), HttpStatus.OK.getReasonPhrase().toUpperCase());
        Assert.assertEquals(response.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/bar");

        verify(mockResponseEndpointAdapter).handleMessage(any(Message.class));
    }


}
