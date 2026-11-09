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
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;

public class HttpClientTest extends AbstractGroovyActionDslTest {

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
    public void shouldLoadHttpClientActions() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/http/groovy/http-client.citrus.test.groovy");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("httpClient", httpClient);
        context.getReferenceResolver().bind("httpServer", httpServer);

        responses.add(new HttpMessage().status(HttpStatus.OK));
        responses.add(new HttpMessage("<order><id>12345</id><item>foo</item></order>").status(HttpStatus.OK).contentType("application/xml"));
        responses.add(new HttpMessage().status(HttpStatus.NOT_FOUND).header("userId", "1001"));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals( "HttpClientTest",result.getName());
        Assertions.assertEquals( "Christoph",result.getMetaInfo().getAuthor());
        Assertions.assertEquals( TestCaseMetaInfo.Status.FINAL,result.getMetaInfo().getStatus());
        Assertions.assertEquals( 10L,result.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,result.getTestAction(0).getClass());
        Assertions.assertEquals( "http:send-request",result.getTestAction(0).getName());

        Assertions.assertEquals( ReceiveMessageAction.class,result.getTestAction(1).getClass());
        Assertions.assertEquals( "http:receive-response",result.getTestAction(1).getName());

        int actionIndex = 0;

        SendMessageAction sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertFalse(sendMessageAction.isForkMode());
        Assertions.assertTrue(sendMessageAction.getMessageBuilder() instanceof HttpMessageBuilder);
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertEquals( 3L,httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals( HttpMethod.GET.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertNull(sendMessageAction.getEndpointUri());
        Assertions.assertEquals( context.getReferenceResolver().resolve("httpClient", HttpClient.class),sendMessageAction.getEndpoint());

        Message controlMessage = new DefaultMessage("");
        Message request = inboundQueue.receive();
        headerValidator.validateMessage(request, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(request, controlMessage, context, new DefaultValidationContext());

        ReceiveMessageAction receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals( 2,receiveMessageAction.getValidationContexts().size());
        Assertions.assertEquals( 0L,receiveMessageAction.getReceiveTimeout());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertEquals( 2L,httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals( "httpClient",receiveMessageAction.getEndpointUri());
        Assertions.assertEquals( 0,receiveMessageAction.getMessageProcessors().size());
        Assertions.assertEquals( 0,receiveMessageAction.getControlMessageProcessors().size());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(sendMessageAction.isForkMode());
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));

        Map<String, Object> requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals( 9L,requestHeaders.size());
        Assertions.assertEquals( HttpMethod.GET.name(),requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/order/12345",requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals( "/order/12345",requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "application/xml",requestHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals( "application/xml",requestHeaders.get(HttpMessageHeaders.HTTP_ACCEPT));
        Assertions.assertEquals( "HTTP/1.1",requestHeaders.get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals( "id=12345,type=gold",requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "id=12345,type=gold",requestHeaders.get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
        Assertions.assertEquals( uri,requestHeaders.get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertNull(sendMessageAction.getEndpoint());
        Assertions.assertEquals( "httpClient",sendMessageAction.getEndpointUri());

        Assertions.assertEquals( SleepAction.class,result.getTestAction(actionIndex++).getClass());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals( 2,receiveMessageAction.getValidationContexts().size());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof XmlMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "<order><id>12345</id><item>foo</item></order>",httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Map<String, Object> responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals( 4L,responseHeaders.size());
        Assertions.assertEquals( 200,responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals( "OK",responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals( "HTTP/1.1",responseHeaders.get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals( "application/xml",responseHeaders.get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals( "httpClient",receiveMessageAction.getEndpointUri());

        Assertions.assertEquals( 1L,receiveMessageAction.getVariableExtractors().size());
        Assertions.assertEquals( 1L,((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().size());
        Assertions.assertEquals( "orderId",((DelegatingPayloadVariableExtractor)receiveMessageAction.getVariableExtractors().get(0)).getPathExpressions().get("/order/id"));

        Assertions.assertEquals( "12345",context.getVariable("orderId"));

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "<user><id>1001</id><name>new_user</name></user>",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertEquals( HttpMethod.POST.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/user",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals( "1001",httpMessageBuilder.buildMessageHeaders(context).get("userId"));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "httpClient",sendMessageAction.getEndpointUri());

        receiveMessageAction = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals( 2,receiveMessageAction.getValidationContexts().size());
        Assertions.assertEquals( 2000L,receiveMessageAction.getReceiveTimeout());
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(receiveMessageAction.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)receiveMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, receiveMessageAction.getMessageType()));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        responseHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals( 3L,responseHeaders.size());
        Assertions.assertEquals( 404,responseHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals( "NOT_FOUND",responseHeaders.get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals( "1001",responseHeaders.get("userId"));
        Assertions.assertNull(receiveMessageAction.getEndpoint());
        Assertions.assertEquals( "httpClient",receiveMessageAction.getEndpointUri());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertEquals( HttpMethod.DELETE.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/user/${id}",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "httpClient",sendMessageAction.getEndpointUri());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex++);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        requestHeaders = httpMessageBuilder.buildMessageHeaders(context);
        Assertions.assertEquals( HttpMethod.HEAD.name(),requestHeaders.get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(requestHeaders.get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(requestHeaders.get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( uri,requestHeaders.get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals( "httpClient",sendMessageAction.getEndpointUri());

        sendMessageAction = (SendMessageAction) result.getTestAction(actionIndex);
        httpMessageBuilder = ((HttpMessageBuilder)sendMessageAction.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, sendMessageAction.getMessageType()));
        Assertions.assertEquals( HttpMethod.OPTIONS.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "http://localhost:${port}/test",sendMessageAction.getEndpointUri());
        Assertions.assertEquals( testActor,sendMessageAction.getActor());
    }
}
