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

import jakarta.servlet.http.Cookie;
import org.apache.hc.core5.http.ContentType;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.UnitTestSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Producer;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendHttpMessageTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    @Mock
    private HttpClient httpClient;
    @Mock
    private Producer messageProducer;

    @Override
    @BeforeEach
    public void prepareTest() {
        super.prepareTest();
    }

    @Test
    public void testFork() {
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "Foo",message.getPayload(String.class));
            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "Bar",message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .send()
                        .get()
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .type(MessageType.PLAINTEXT)
                        .header("additional", "additionalValue"));

        builder.$(http().client(httpClient)
                .send()
                .post()
                .message(new DefaultMessage("Bar").setHeader("operation", "bar"))
                .type(MessageType.PLAINTEXT)
                .fork(true));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 2,test.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(0).getClass());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(1).getClass());

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assertions.assertEquals( "http:send-request",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "Foo",messageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals( 4L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( MessageType.PLAINTEXT.name(),messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE));
        Assertions.assertEquals( HttpMethod.GET.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "foo",messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals( "additionalValue",messageBuilder.buildMessageHeaders(context).get("additional"));

        Assertions.assertFalse(action.isForkMode());

        action = ((SendMessageAction)test.getActions().get(1));
        Assertions.assertEquals( "http:send-request",action.getName());

        messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());
        Assertions.assertEquals( "Bar",messageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals( 3L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( MessageType.PLAINTEXT.name(),messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE));
        Assertions.assertEquals( HttpMethod.POST.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "bar",messageBuilder.buildMessageHeaders(context).get("operation"));

        Assertions.assertTrue(action.isForkMode());
    }

    @Test
    public void testMessageObjectOverride() {
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "Foo",message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
            .send()
            .get()
            .message(new HttpMessage("Foo")
                    .cookie(new Cookie("Bar", "987654"))
                    .setHeader("operation", "foo"))
            .cookie(new Cookie("Foo", "123456"))
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .type(MessageType.PLAINTEXT)
            .header("additional", "additionalValue"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(0).getClass());

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assertions.assertEquals( "http:send-request",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "Foo",messageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals( 7L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertNotEquals( MessageType.PLAINTEXT,messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE));
        Assertions.assertEquals( HttpMethod.GET.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( ContentType.APPLICATION_JSON.getMimeType(),messageBuilder.buildMessageHeaders(context).get("Content-Type"));
        Assertions.assertEquals( "foo",messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals( "additionalValue",messageBuilder.buildMessageHeaders(context).get("additional"));
        Assertions.assertEquals( "Foo=123456",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Foo"));
        Assertions.assertEquals( "Bar=987654",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Bar"));
        Assertions.assertEquals( 2L,((HttpMessage) messageBuilder.getMessage()).getCookies().size());
        Assertions.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Foo")));
        Assertions.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Bar")));
    }

    @Test
    public void testHttpMethod() {
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",message.getPayload(String.class));
            Assertions.assertEquals( HttpMethod.GET.name(),message.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .send()
                        .get()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(0).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals( "http:send-request",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.getMessage().getPayload());
        Assertions.assertEquals( 1L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( HttpMethod.GET.name(),messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
    }

    @Test
    public void testHttpRequestUriAndPath() {
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",message.getPayload(String.class));
            Assertions.assertEquals( "http://localhost:8080/",message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
            Assertions.assertEquals( "/test",message.getHeader(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .send()
                        .get("/test")
                        .uri("http://localhost:8080/")
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(0).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals( "http:send-request",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.getMessage().getPayload());
        Assertions.assertEquals( 4L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( "GET",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "http://localhost:8080/",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "http://localhost:8080/",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals( "/test",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
    }

    @Test
    public void testHttpRequestUriAndQueryParams() {
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",message.getPayload(String.class));
            Assertions.assertEquals( "http://localhost:8080/",message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
            Assertions.assertEquals( "param1=value1,param2=value2",message.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .send()
                        .get()
                        .uri("http://localhost:8080/")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals( 1,test.getActionCount());
        Assertions.assertEquals( SendMessageAction.class,test.getActions().get(0).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals( "http:send-request",action.getName());

        Assertions.assertEquals( httpClient,action.getEndpoint());
        Assertions.assertEquals( HttpMessageBuilder.class,action.getMessageBuilder().getClass());

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals( "<TestRequest><Message>Hello World!</Message></TestRequest>",messageBuilder.getMessage().getPayload());
        Assertions.assertEquals( 5L,messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals( "GET",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "http://localhost:8080/",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "param1=value1,param2=value2",messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "http://localhost:8080/",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals( "param1=value1,param2=value2",messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
    }
}
