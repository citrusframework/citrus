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

package org.citrusframework.citrus.dsl.runner;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.client.HttpEndpointConfiguration;
import org.citrusframework.citrus.http.message.HttpMessage;
import org.citrusframework.citrus.http.message.HttpMessageBuilder;
import org.citrusframework.citrus.http.message.HttpMessageHeaders;
import org.citrusframework.citrus.http.server.HttpServer;
import org.citrusframework.citrus.message.MessageHeaders;
import org.citrusframework.citrus.messaging.SelectiveConsumer;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.validation.json.JsonMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XmlMessageValidationContext;
import org.apache.hc.core5.http.ContentType;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveHttpMessageTestRunnerTest extends UnitTestSupport {

    private SelectiveConsumer messageConsumer = Mockito.mock(SelectiveConsumer.class);
    private HttpEndpointConfiguration configuration = Mockito.mock(HttpEndpointConfiguration.class);
    private HttpClient httpClient = Mockito.mock(HttpClient.class);
    private HttpServer httpServer = Mockito.mock(HttpServer.class);

    @Test
    public void testHttpRequestProperties() {
        reset(httpServer, messageConsumer, configuration);
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(action -> action.server(httpServer)
                        .receive()
                        .get("/test/foo")
                        .queryParam("noValue", null)
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpServer);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 5L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "noValue,param1=value1,param2=value2");
    }

    @Test
    public void testMessageObjectOverride() {
        reset(httpServer, messageConsumer, configuration);
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(action -> action.server(httpServer)
                        .receive()
                        .get("/test/foo")
                        .queryParam("param1", "value1")
                        .contentType(ContentType.APPLICATION_XML.getMimeType())
                        .header("X-Bar", "bar")
                        .message(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).header("X-Foo", "foo"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpServer);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 9L);
        Assert.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.ID));
        Assert.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertNotNull(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), action.getMessageType());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test/foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "param1=value1");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_CONTENT_TYPE), ContentType.APPLICATION_XML.getMimeType());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("X-Foo"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("X-Bar"), "bar");
    }

    @Test
    public void testHttpResponseProperties() {
        reset(httpClient, messageConsumer, configuration);
        when(httpClient.createConsumer()).thenReturn(messageConsumer);
        when(httpClient.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(httpClient.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .uri("/test")
                .status(HttpStatus.OK)
                .version("HTTP/1.1"));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(action -> action.client(httpClient)
                        .receive()
                        .response(HttpStatus.OK)
                        .version("HTTP/1.1")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 3L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
    }

}
