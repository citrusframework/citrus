/*
 * Copyright 2006-2015 the original author or authors.
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
import org.citrusframework.DefaultTestCaseRunner;
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
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendHttpMessageTestActionBuilderTest extends UnitTestSupport {

    private final HttpClient httpClient = Mockito.mock(HttpClient.class);
    private final Producer messageProducer = Mockito.mock(Producer.class);

    @Test
    public void testFork() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Bar");
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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assert.assertEquals(action.getName(), "http:send-request");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 4L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");

        Assert.assertFalse(action.isForkMode());

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "http:send-request");

        messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Bar");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 3L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "bar");

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testMessageObjectOverride() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assert.assertEquals(action.getName(), "http:send-request");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 7L);
        Assert.assertNotEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("Content-Type"), ContentType.APPLICATION_JSON.getMimeType());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Foo"), "Foo=123456");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Bar"), "Bar=987654");
        Assert.assertEquals(((HttpMessage) messageBuilder.getMessage()).getCookies().size(), 2L);
        Assert.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Foo")));
        Assert.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Bar")));
    }

    @Test
    public void testHttpMethod() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(http().client(httpClient)
                        .send()
                        .get()
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "http:send-request");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
    }

    @Test
    public void testHttpRequestUriAndPath() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "http:send-request");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 4L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), "GET");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
    }

    @Test
    public void testHttpRequestUriAndQueryParams() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1,param2=value2");
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "http:send-request");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 5L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), "GET");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "param1=value1,param2=value2");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1,param2=value2");
    }

}
