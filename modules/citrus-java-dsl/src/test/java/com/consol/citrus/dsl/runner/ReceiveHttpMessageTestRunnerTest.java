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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.*;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.apache.http.entity.ContentType;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveHttpMessageTestRunnerTest extends AbstractTestNGUnitTest {

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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
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
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpServer);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageContentBuilder messageBuilder = (HttpMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 7L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "noValue,param1=value1,param2=value2");
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
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
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpServer);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageContentBuilder messageBuilder = (HttpMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 10L);
        Assert.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.ID));
        Assert.assertNotNull(messageBuilder.getMessage().getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test/foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test/foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "param1=value1");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), ContentType.APPLICATION_XML.getMimeType());
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get("X-Foo"), "foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get("X-Bar"), "bar");
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
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
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        HttpMessageContentBuilder messageBuilder = (HttpMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
    }

}
