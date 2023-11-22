/*
 * Copyright 2023 the original author or authors.
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

package org.citrusframework.openapi.groovy;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.groovy.GroovyTestLoader;
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
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
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
public class OpenApiClientTest extends AbstractGroovyActionDslTest {

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
    public void shouldLoadOpenApiClientActions() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/openapi/groovy/openapi-client.test.groovy");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("httpClient", httpClient);
        context.getReferenceResolver().bind("httpServer", httpServer);

        responses.add(new HttpMessage("""
                {
                  "id": 1000,
                  "name": "hasso",
                  "category": {
                    "id": 1000,
                    "name": "dog"
                  },
                  "photoUrls": [ "http://localhost:8080/photos/1000" ],
                  "tags": [
                    {
                      "id": 1000,
                      "name": "generated"
                    }
                  ],
                  "status": "available"
                }
                """).status(HttpStatus.OK).contentType("application/json"));
        responses.add(new HttpMessage().status(HttpStatus.CREATED));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "OpenApiClientTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "openapi:send-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "openapi:receive-response");

        int actionIndex = 0;

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(sendMessageAction.isForkMode());
        Assert.assertTrue(sendMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/pet/${petId}");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/pet/${petId}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(sendMessageAction.getEndpoint(), httpClient);

        Message controlMessage = new DefaultMessage("");
        Message request = inboundQueue.receive();
        headerValidator.validateMessage(request, controlMessage, context, new HeaderValidationContext());
        validator.validateMessage(request, controlMessage, context, new DefaultValidationContext());

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);

        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()),
                "{\"id\": \"@isNumber()@\",\"category\": {\"id\": \"@isNumber()@\",\"name\": \"@notEmpty()@\"},\"name\": \"@notEmpty()@\",\"photoUrls\": \"@ignore@\",\"tags\": \"@ignore@\",\"status\": \"@matches(available|pending|sold)@\"}");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/json");
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");
        Assert.assertEquals(receiveMessageAction.getMessageProcessors().size(), 0);
        Assert.assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(sendMessageAction.isForkMode());
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(requestHeaders.size(), 4L);
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/pet");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI), "/pet");
        Assert.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "application/json");
        Assert.assertNull(sendMessageAction.getEndpointUri());
        Assert.assertEquals(sendMessageAction.getEndpoint(), httpClient);

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex);
        Assert.assertEquals(receiveMessageAction.getValidationContexts().size(), 3);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assert.assertEquals(responseHeaders.size(), 2L);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), 201);
        Assert.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "CREATED");
        Assert.assertNull(receiveMessageAction.getEndpoint());
        Assert.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");

        Assert.assertEquals(receiveMessageAction.getVariableExtractors().size(), 0L);
    }
}
