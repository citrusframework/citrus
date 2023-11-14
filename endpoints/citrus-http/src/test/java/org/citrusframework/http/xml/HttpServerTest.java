/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.http.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;

/**
 * @author Christoph Deppisch
 */
public class HttpServerTest extends AbstractXmlActionTest {

    @BindToRegistry
    final TestActor testActor = Mockito.mock(TestActor.class);

    @BindToRegistry
    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();

    @BindToRegistry
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim();

    private HttpServer httpServer;

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final EndpointAdapter endpointAdapter = new DirectEndpointAdapter(direct()
            .synchronous()
            .timeout(100L)
            .queue(inboundQueue)
            .build());

    @BeforeClass
    public void setupEndpoints() {
        ((AbstractEndpointAdapter) endpointAdapter).setTestContextFactory(testContextFactory);

        httpServer = http().server()
                .timeout(100L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("httpServer")
                .build();
    }

    @Test
    public void shouldLoadHttpServerActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/http/xml/http-server-test.xml");

        context.getReferenceResolver().bind("httpServer", httpServer);

        endpointAdapter.handleMessage(new HttpMessage().method(HttpMethod.GET));
        endpointAdapter.handleMessage(new HttpMessage()
                        .method(HttpMethod.GET)
                        .path("/test/order/12345")
                        .version("HTTP/1.1")
                        .queryParam("id", "12345")
                        .queryParam("type", "gold")
                        .accept("application/xml")
                        .contentType("application/xml"));
        endpointAdapter.handleMessage(new HttpMessage("<user><id>1001</id><name>new_user</name></user>")
                        .method(HttpMethod.POST)
                        .path("/user")
                        .header("userId", "1001")
                        .contentType("application/xml"));
        endpointAdapter.handleMessage(new HttpMessage()
                        .method(HttpMethod.DELETE)
                        .path("/user/12345"));
        endpointAdapter.handleMessage(new HttpMessage()
                        .method(HttpMethod.HEAD)
                        .path("/test"));
        endpointAdapter.handleMessage(new HttpMessage()
                        .method(HttpMethod.OPTIONS));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "HttpServerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 9L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "http:receive-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), SendMessageAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "http:send-response");

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);

        Assert.assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");
        Assert.assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);

        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertNull(sendMessageAction.getEndpoint());
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpServer");
        Assert.assertEquals(sendMessageAction.getMessageProcessors().size(), 0);

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertEquals(receiveMessageAction.getReceiveTimeout(), 2000L);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(requestHeaders.size(), 8L);
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test/order/12345");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/order/12345");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/xml");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_ACCEPT), "application/xml");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "id=12345,type=gold");
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "id=12345,type=gold");
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "<order><id>12345</id><item>foo</item></order>");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(responseHeaders.size(), 4L);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), "200");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/xml");
        Assert.assertNull(sendMessageAction.getEndpoint());
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpServer");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user");
        Assert.assertEquals(httpMessageBuilder.buildMessageHeaders(context).get("userId"), "1001");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");

        Assert.assertEquals(receiveMessageAction.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().get("/user/id"), "userId");

        Assert.assertEquals(context.getVariable("userId"), "1001");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(responseHeaders.size(), 3L);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(responseHeaders.get("userId"), "1001");
        Assert.assertNull(sendMessageAction.getEndpoint());
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpServer");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.DELETE.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user/${id}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.HEAD.name());
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
        Assert.assertNull(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.OPTIONS.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");
        Assert.assertEquals(receiveMessageAction.getActor(), testActor);
    }
}
