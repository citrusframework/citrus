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

package org.citrusframework.openapi.yaml;

import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OpenApiServerTest extends AbstractYamlActionTest {

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final EndpointAdapter endpointAdapter = new DirectEndpointAdapter(direct()
            .synchronous()
            .timeout(100L)
            .queue(inboundQueue)
            .build());
    private HttpServer httpServer;

    @BeforeEach
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
    public void shouldLoadOpenApiServerActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/openapi/yaml/openapi-server.citrus.it.yaml");

        context.getReferenceResolver().bind("httpServer", httpServer);

        endpointAdapter.handleMessage(new HttpMessage()
                .method(HttpMethod.GET)
                .path("/petstore/v3/pet/12345")
                .version("HTTP/1.1")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE));
        endpointAdapter.handleMessage(new HttpMessage("""
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
                """)
                .method(HttpMethod.POST)
                .path("/petstore/v3/pet")
                .contentType(APPLICATION_JSON_VALUE));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(result.getName(), "OpenApiServerTest");
        Assertions.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assertions.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assertions.assertEquals(result.getActionCount(), 4L);
        Assertions.assertEquals(result.getTestAction(0).getClass(), ReceiveMessageAction.class);
        Assertions.assertEquals(result.getTestAction(0).getName(), "openapi:receive-request");

        Assertions.assertEquals(result.getTestAction(1).getClass(), SendMessageAction.class);
        Assertions.assertEquals(result.getTestAction(1).getName(), "openapi:send-response");

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(receiveMessageAction.getValidationContexts().size(), 2);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof OpenApiMessageValidationContext);
        Assertions.assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);

        Assertions.assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 2L);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");
        Assertions.assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), APPLICATION_JSON_VALUE);

        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals(sendMessageAction.getEndpointUri(), "httpServer");
        Assertions.assertEquals(sendMessageAction.getMessageProcessors().size(), 1);

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(receiveMessageAction.getValidationContexts().size(), 2);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof OpenApiMessageValidationContext);
        Assertions.assertEquals(receiveMessageAction.getReceiveTimeout(), 2000L);

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(requestHeaders.size(), 0L);
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals(receiveMessageAction.getEndpointUri(), "httpServer");

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(responseHeaders.size(), 2L);
        Assertions.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), 201);
        Assertions.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "CREATED");
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals(sendMessageAction.getEndpointUri(), "httpServer");
    }
}
