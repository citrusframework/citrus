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

package org.citrusframework.http.groovy;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
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
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;

public class HttpServerTest extends AbstractGroovyActionDslTest {

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
    public void shouldLoadHttpServerActions() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/http/groovy/http-server.citrus.test.groovy");

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
        Assertions.assertEquals("HttpServerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(9L, result.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("http:receive-request", result.getTestAction(0).getName());

        Assertions.assertEquals(SendMessageAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("http:send-response", result.getTestAction(1).getName());

        int actionIndex = 0;

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);
        Assertions.assertEquals(0L, receiveMessageAction.getReceiveTimeout());

        Assertions.assertTrue(receiveMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals(3L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpMethod.GET.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertNull(receiveMessageAction.getEndpointUri());
        Assertions.assertEquals(context.getReferenceResolver().resolve("httpServer", HttpServer.class), receiveMessageAction.getEndpoint());
        Assertions.assertEquals(0, receiveMessageAction.getControlMessageProcessors().size());

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertEquals(2L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", sendMessageAction.getEndpointUri());
        Assertions.assertEquals(0, sendMessageAction.getMessageProcessors().size());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);
        Assertions.assertEquals(2000L, receiveMessageAction.getReceiveTimeout());

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(8L, requestHeaders.size());
        Assertions.assertEquals(HttpMethod.GET.name(), requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/test/order/12345", requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals("/test/order/12345", requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals("application/xml", requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals("application/xml", requestHeaders.get(HttpMessageHeaders.HTTP_ACCEPT));
        Assertions.assertEquals("HTTP/1.1", requestHeaders.get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals("id=12345,type=gold", requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("id=12345,type=gold", requestHeaders.get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("<order><id>12345</id><item>foo</item></order>", httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(4L, responseHeaders.size());
        Assertions.assertEquals(200, responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals("OK", responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals("HTTP/1.1", responseHeaders.get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals("application/xml", responseHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", sendMessageAction.getEndpointUri());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("<user><id>1001</id><name>new_user</name></user>", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals(HttpMethod.POST.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/user", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals("1001", httpMessageBuilder.buildMessageHeaders(context).get("userId"));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());

        Assertions.assertEquals(1L, receiveMessageAction.getVariableExtractors().size());
        Assertions.assertEquals(1L, ((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().size());
        Assertions.assertEquals("userId", ((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().get("/user/id"));

        Assertions.assertEquals("1001", context.getVariable("userId"));

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(3L, responseHeaders.size());
        Assertions.assertEquals(404, responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals("NOT_FOUND", responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals("1001", responseHeaders.get("userId"));
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals("httpServer", sendMessageAction.getEndpointUri());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals(HttpMethod.DELETE.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/user/${id}", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals(HttpMethod.HEAD.name(), requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/test", requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals(HttpMethod.OPTIONS.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("httpServer", receiveMessageAction.getEndpointUri());
        Assertions.assertEquals(testActor, receiveMessageAction.getActor());
    }
}
