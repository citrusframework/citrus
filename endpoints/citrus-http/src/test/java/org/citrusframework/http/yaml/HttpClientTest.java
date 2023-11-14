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

package org.citrusframework.http.yaml;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;

/**
 * @author Christoph Deppisch
 */
public class HttpClientTest extends AbstractYamlActionTest {

    @BindToRegistry
    final TestActor testActor = Mockito.mock(TestActor.class);

    @BindToRegistry
    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();

    @BindToRegistry
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim();

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private HttpServer httpServer;
    private HttpClient httpClient;

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");

    private final Queue<HttpMessage> responses = new ArrayBlockingQueue<>(6);

    @BeforeClass
    public void setupEndpoints() {
        EndpointAdapter endpointAdapter = new DirectEndpointAdapter(new DirectSyncEndpointConfiguration()) {
            @Override
            public Message handleMessageInternal(Message request) {
                inboundQueue.send(request);
                return responses.isEmpty() ? new HttpMessage().status(HttpStatus.OK) : responses.remove();
            }
        };

        httpServer = http().server()
                .port(port)
                .timeout(500L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("httpServer")
                .build();
        httpServer.initialize();

        httpClient = http().client()
                .requestUrl(uri)
                .name("httpClient")
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEndpoints() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    @Test
    public void shouldLoadHttpClientActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/http/yaml/http-client-test.yaml");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("httpClient", httpClient);
        context.getReferenceResolver().bind("httpServer", httpServer);

        responses.add(new HttpMessage().status(HttpStatus.OK));
        responses.add(new HttpMessage("<order><id>12345</id><item>foo</item></order>").status(HttpStatus.OK).contentType("application/xml"));
        responses.add(new HttpMessage().status(HttpStatus.NOT_FOUND).header("userId", "1001"));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "HttpClientTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 10L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "http:send-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "http:receive-response");

        int actionIndex = 0;

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(sendMessageAction.isForkMode());
        Assert.assertTrue(sendMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        Message controlMessage = new DefaultMessage("");
        Message request = inboundQueue.receive();
        headerValidator.validateMessage(request, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(request, controlMessage, context, new DefaultValidationContext());

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof HeaderValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);

        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");
        Assert.assertEquals(receiveMessageAction.getMessageProcessors().size(), 0);
        Assert.assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(sendMessageAction.isForkMode());
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(requestHeaders.size(), 9L);
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/order/12345");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI), "/order/12345");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/xml");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_ACCEPT), "application/xml");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "id=12345,type=gold");
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "id=12345,type=gold");
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), uri);
        Assert.assertNull(sendMessageAction.getEndpoint());
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        Assert.assertEquals(result.getTestAction(actionIndex++).getClass(), SleepAction.class);

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof HeaderValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "<order><id>12345</id><item>foo</item></order>");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(responseHeaders.size(), 4L);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), "200");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/xml");
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");

        Assert.assertEquals(receiveMessageAction.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().get("/order/id"), "orderId");

        Assert.assertEquals(context.getVariable("orderId"), "12345");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user");
        Assert.assertEquals(httpMessageBuilder.buildMessageHeaders(context).get("userId"), "1001");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertEquals(receiveMessageAction.getReceiveTimeout(), 2000L);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof JsonMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof HeaderValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(responseHeaders.size(), 3L);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(responseHeaders.get("userId"), "1001");
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.DELETE.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user/${id}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.HEAD.name());
        Assert.assertNull(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), uri);
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.OPTIONS.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(sendMessageAction.getEndpointUri(), "http://localhost:${port}/test");
        Assert.assertEquals(sendMessageAction.getActor(), testActor);
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(YamlTestActionBuilder.lookup().containsKey("http"));
        Assert.assertTrue(YamlTestActionBuilder.lookup("http").isPresent());
        Assert.assertEquals(YamlTestActionBuilder.lookup("http").get().getClass(), Http.class);
    }
}
