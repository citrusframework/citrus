/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.message.HttpMessageBuilder;
import org.citrusframework.citrus.http.message.HttpMessageHeaders;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 */
public class SendHttpMessageTestDesignerTest extends UnitTestSupport {

    private HttpClient httpClient = Mockito.mock(HttpClient.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testFork() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(httpClient)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                            .header("additional", "additionalValue");

                send(httpClient)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .fork(true);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");

        Assert.assertFalse(action.isForkMode());

        action = (SendMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testHttpMethod() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                http().client(httpClient)
                        .send()
                        .get()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
    }

    @Test
    public void testHttpRequestUriAndPath() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                http().client(httpClient)
                        .send()
                        .get("/test")
                        .uri("http://localhost:8080/")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 6L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
    }

    @Test
    public void testHttpRequestUriAndQueryParams() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                http().client(httpClient)
                        .send()
                        .get()
                        .uri("http://localhost:8080/")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 7L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1,param2=value2");
    }

}
