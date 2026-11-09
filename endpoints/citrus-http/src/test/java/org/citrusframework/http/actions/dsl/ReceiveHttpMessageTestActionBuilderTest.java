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

package org.citrusframework.http.actions.dsl;

import org.apache.hc.core5.http.ContentType;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.UnitTestSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReceiveHttpMessageTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    @Mock
    private SelectiveConsumer messageConsumer;
    @Mock
    private HttpEndpointConfiguration configuration;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpServer httpServer;

    @Override
    @BeforeEach
    public void prepareTest() {
        super.prepareTest();
    }

    @Test
    public void testHttpRequestProperties() {
        when(httpServer.createConsumer()).thenReturn(messageConsumer);
        when(httpServer.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(httpServer.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .path("/test/foo")
                .queryParam("noValue", null)
                .queryParam("param1", "value1")
                .queryParam("param2", "value2"));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().server(httpServer)
            .receive()
            .get("/test/foo")
            .queryParam("noValue", null)
            .queryParam("param1", "value1")
            .queryParam("param2", "value2")
            .message()
            .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( ReceiveMessageAction.class,test.getActions().get(0).getClass());

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assertions.assertEquals( "http:receive-request",action.getName());

        Assertions.assertEquals( httpServer,action.getEndpoint());
        Assertions.assertEquals( 2L,action.getValidationContexts().size());
        Assertions.assertEquals( HeaderValidationContext.class,action.getValidationContexts().get(0).getClass());
        Assertions.assertEquals( XmlMessageValidationContext.class,action.getValidationContexts().get(1).getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( 5L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( HttpMethod.GET.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/test/foo",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "noValue,param1=value1,param2=value2",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
    }

    @Test
    public void testMessageObjectOverride() {
        when(httpServer.createConsumer()).thenReturn(messageConsumer);
        when(httpServer.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(httpServer.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .path("/test/foo")
                .header("X-Foo", "foo")
                .header("X-Bar", "bar")
                .contentType(ContentType.APPLICATION_XML.getMimeType())
                .queryParam("param1", "value1"));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().server(httpServer)
                        .receive()
                        .get("/test/foo")
                        .queryParam("param1", "value1")
                        .message(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("X-Foo", "foo")
                        .header("X-Bar", "bar")
                        .contentType(ContentType.APPLICATION_XML.getMimeType()));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( ReceiveMessageAction.class,test.getActions().get(0).getClass());

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assertions.assertEquals( "http:receive-request",action.getName());

        Assertions.assertEquals( httpServer,action.getEndpoint());
        Assertions.assertEquals( 2L,action.getValidationContexts().size());
        Assertions.assertEquals( HeaderValidationContext.class,action.getValidationContexts().get(0).getClass());
        Assertions.assertEquals( XmlMessageValidationContext.class,action.getValidationContexts().get(1).getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( 9L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.TIMESTAMP));
        Assertions.assertNotNull(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), action.getMessageType());
        Assertions.assertEquals( HttpMethod.GET.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/test/foo",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "/test/foo",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals( "param1=value1",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "param1=value1",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
        Assertions.assertEquals( ContentType.APPLICATION_XML.getMimeType(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals( "foo",messageBuilder.buildMessageHeaders(context).get("X-Foo"));
        Assertions.assertEquals( "bar",messageBuilder.buildMessageHeaders(context).get("X-Bar"));
    }

    @Test
    public void testHttpResponseProperties() {
        when(httpClient.createConsumer()).thenReturn(messageConsumer);
        when(httpClient.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(httpClient.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .uri("/test")
                .status(HttpStatus.OK)
                .version("HTTP/1.1"));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .receive()
                        .response(HttpStatus.OK)
                        .message()
                        .version("HTTP/1.1")
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( ReceiveMessageAction.class,test.getActions().get(0).getClass());

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assertions.assertEquals( "http:receive-response",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( 2L,action.getValidationContexts().size());
        Assertions.assertEquals( HeaderValidationContext.class,action.getValidationContexts().get(0).getClass());
        Assertions.assertEquals( XmlMessageValidationContext.class,action.getValidationContexts().get(1).getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( 3L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( 200,messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals( "OK",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals( "HTTP/1.1",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_VERSION));
    }
}
