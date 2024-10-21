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

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
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
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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
    public void shouldLoadOpenApiServerActions() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/openapi/groovy/openapi-server.test.groovy");

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
        assertEquals(result.getName(), "OpenApiServerTest");
        assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        assertEquals(result.getActionCount(), 4L);
        assertEquals(result.getTestAction(0).getClass(), ReceiveMessageAction.class);
        assertEquals(result.getTestAction(0).getName(), "openapi:receive-request");

        assertEquals(result.getTestAction(1).getClass(), SendMessageAction.class);
        assertEquals(result.getTestAction(1).getName(), "openapi:send-response");

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        assertEquals(receiveMessageAction.getValidationContexts().size(), 4);
        assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(3) instanceof OpenApiMessageValidationContext);
        assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);

        assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/petstore/v3/pet/${petId}");
        assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/petstore/v3/pet/${petId}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        assertEquals(receiveMessageAction.getEndpoint(), httpServer);
        assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);

        assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), APPLICATION_JSON_VALUE);

        Assert.assertNull(sendMessageAction.getEndpoint());
        assertEquals(sendMessageAction.getEndpointUri(), "httpServer");
        assertEquals(sendMessageAction.getMessageProcessors().size(), 1);

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        assertEquals(receiveMessageAction.getValidationContexts().size(), 4);
        assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        assertTrue(receiveMessageAction.getValidationContexts().get(3) instanceof OpenApiMessageValidationContext);
        assertEquals(receiveMessageAction.getReceiveTimeout(), 2000L);

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()),
                "{\"id\": \"@isNumber()@\",\"category\": {\"id\": \"@isNumber()@\",\"name\": \"@notEmpty()@\"},\"name\": \"@notEmpty()@\",\"photoUrls\": \"@ignore@\",\"tags\": \"@ignore@\",\"status\": \"@matches(available|pending|sold)@\"}");
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        assertEquals(requestHeaders.size(), 4L);
        assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/petstore/v3/pet");
        assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI), "/petstore/v3/pet");
        assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "@startsWith(application/json)@");
        Assert.assertNull(receiveMessageAction.getEndpointUri());
        assertEquals(receiveMessageAction.getEndpoint(), httpServer);

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        assertNotNull(httpMessageBuilder);
        assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        assertEquals(responseHeaders.size(), 2L);
        assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), 201);
        assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "CREATED");
        Assert.assertNull(sendMessageAction.getEndpoint());
        assertEquals(sendMessageAction.getEndpointUri(), "httpServer");
    }
}
