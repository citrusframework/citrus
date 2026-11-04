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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
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
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OpenApiClientTest extends AbstractYamlActionTest {


    @BindToRegistry
    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();

    @BindToRegistry
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim();

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";
    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final Queue<HttpMessage> responses = new ArrayBlockingQueue<>(6);
    private HttpServer httpServer;
    private HttpClient httpClient;

    @BeforeEach
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

    @AfterEach
    public void cleanupEndpoints() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    @Test
    public void shouldLoadOpenApiClientActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/openapi/yaml/openapi-client.citrus.it.yaml");

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
                """).status(HttpStatus.OK).contentType(APPLICATION_JSON_VALUE));
        responses.add(new HttpMessage().status(HttpStatus.CREATED));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(result.getName(), "OpenApiClientTest");
        Assertions.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assertions.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assertions.assertEquals(result.getActionCount(), 4L);
        Assertions.assertEquals(result.getTestAction(0).getClass(), SendMessageAction.class);
        Assertions.assertEquals(result.getTestAction(0).getName(), "openapi:send-request");

        Assertions.assertEquals(result.getTestAction(1).getClass(), ReceiveMessageAction.class);
        Assertions.assertEquals(result.getTestAction(1).getName(), "openapi:receive-response");

        int actionIndex = 0;

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(sendMessageAction.isForkMode());
        Assertions.assertTrue(sendMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()), "");
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/petstore/v3/pet/${petId}");
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/petstore/v3/pet/${petId}");
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        Message controlMessage = new DefaultMessage("");
        Message request = inboundQueue.receive();
        headerValidator.validateMessage(request, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(request, controlMessage, context, new DefaultValidationContext());

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(receiveMessageAction.getValidationContexts().size(), 2);
        Assertions.assertEquals(receiveMessageAction.getReceiveTimeout(), 0L);
        Assertions.assertEquals(receiveMessageAction.getValidationContexts().size(), 2);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof OpenApiMessageValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof HeaderValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 4L);
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assertions.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");
        Assertions.assertEquals(receiveMessageAction.getMessageProcessors().size(), 1);
        Assertions.assertEquals(receiveMessageAction.getControlMessageProcessors().size(), 0);

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(sendMessageAction.isForkMode());
        httpMessageBuilder = ((HttpMessageBuilder) sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertTrue(httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()).toString().startsWith("{\"id\": "));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(requestHeaders.size(), 4L);
        Assertions.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assertions.assertEquals(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/petstore/v3/pet");
        Assertions.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI), "/petstore/v3/pet");
        Assertions.assertEquals(requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE), APPLICATION_JSON_VALUE);
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals(sendMessageAction.getEndpointUri(), "httpClient");

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(receiveMessageAction.getValidationContexts().size(), 2);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof OpenApiMessageValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof HeaderValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder) receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals(httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()), "");
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(responseHeaders.size(), 2L);
        Assertions.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE), 201);
        Assertions.assertEquals(responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE), "CREATED");
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals(receiveMessageAction.getEndpointUri(), "httpClient");

        Assertions.assertEquals(receiveMessageAction.getVariableExtractors().size(), 0L);
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(YamlTestActionBuilder.lookup("openapi").isPresent());
        Assertions.assertEquals(YamlTestActionBuilder.lookup("openapi").get().getClass(), OpenApi.class);
    }
}
