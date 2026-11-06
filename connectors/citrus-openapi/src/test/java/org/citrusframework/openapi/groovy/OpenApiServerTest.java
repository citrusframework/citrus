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

package org.citrusframework.openapi.groovy;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenApiServerTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    final TestActor testActor = Mockito.mock(TestActor.class);

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
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/openapi/groovy/openapi-server.citrus.test.groovy");

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
        assertEquals("OpenApiServerTest", result.getName());
        assertEquals("Christoph", result.getMetaInfo().getAuthor());
        assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        assertEquals(4L, result.getActionCount());
        assertEquals(ReceiveMessageAction.class, result.getTestAction(0).getClass());
        assertEquals("openapi:receive-request", result.getTestAction(0).getName());

        assertEquals(SendMessageAction.class, result.getTestAction(1).getClass());
        assertEquals("openapi:send-response", result.getTestAction(1).getName());

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        assertEquals(3, receiveMessageAction.getValidationContexts().size());
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(HeaderValidationContext.class::isInstance), "List must contain HeaderValidationContext");
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(OpenApiMessageValidationContext.class::isInstance), "List must contain OpenApiMessageValidationContext");
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(DefaultMessageValidationContext.class::isInstance), "List must contain DefaultMessageValidationContext");
        assertEquals(0L, receiveMessageAction.getReceiveTimeout());

        assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        assertEquals(2L, httpMessageBuilder.getMessage().getHeaders().size());
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        assertEquals(httpServer, receiveMessageAction.getEndpoint());
        assertEquals(0, receiveMessageAction.getControlMessageProcessors().size());

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);

        assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        assertEquals(5L, httpMessageBuilder.getMessage().getHeaders().size());
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        assertEquals(200, httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE));
        assertEquals("OK", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        assertEquals(APPLICATION_JSON_VALUE, httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE));

        assertNull(sendMessageAction.getEndpoint());
        assertEquals("httpServer", sendMessageAction.getEndpointUri());
        assertEquals(1, sendMessageAction.getMessageProcessors().size());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        assertEquals(3, receiveMessageAction.getValidationContexts().size());
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(HeaderValidationContext.class::isInstance), "List must contain HeaderValidationContext");
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(OpenApiMessageValidationContext.class::isInstance), "List must contain OpenApiMessageValidationContext");
        assertTrue(receiveMessageAction.getValidationContexts().stream()
            .anyMatch(DefaultMessageValidationContext.class::isInstance), "List must contain DefaultMessageValidationContext");
        assertEquals(2000L, receiveMessageAction.getReceiveTimeout());

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        assertEquals(0L, requestHeaders.size());
        assertNull(receiveMessageAction.getEndpointUri());
        assertEquals(httpServer, receiveMessageAction.getEndpoint());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertEquals("", httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        assertEquals(2L, responseHeaders.size());
        assertEquals(201, responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        assertEquals("CREATED", responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        assertNull(sendMessageAction.getEndpoint());
        assertEquals("httpServer", sendMessageAction.getEndpointUri());
    }
}
