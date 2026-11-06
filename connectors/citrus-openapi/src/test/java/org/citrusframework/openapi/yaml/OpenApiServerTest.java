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
        Assertions.assertEquals("OpenApiServerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("openapi:receive-request", result.getTestAction(0).getName());

        Assertions.assertEquals(SendMessageAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("openapi:send-response", result.getTestAction(1).getName());

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof OpenApiMessageValidationContext);
        Assertions.assertEquals(0L, receiveMessageAction.getReceiveTimeout());

        Assertions.assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals(2L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());
        Assertions.assertEquals(0, receiveMessageAction.getControlMessageProcessors().size());

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        Assertions.assertEquals(5L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(200, httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals("OK", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals(APPLICATION_JSON_VALUE, httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", sendMessageAction.getEndpointUri());
        Assertions.assertEquals(1, sendMessageAction.getMessageProcessors().size());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof OpenApiMessageValidationContext);
        Assertions.assertEquals(2000L, receiveMessageAction.getReceiveTimeout());

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(0L, requestHeaders.size());
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(2L, responseHeaders.size());
        Assertions.assertEquals(201, responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals("CREATED", responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", sendMessageAction.getEndpointUri());
    }
}
