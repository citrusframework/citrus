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

package com.consol.citrus.dsl.definition;

import java.util.HashMap;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.adapter.common.endpoint.MessageHeaderEndpointUriResolver;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendHttpMessageDefinitionTest extends AbstractTestNGUnitTest {

    private HttpClient httpClient = EasyMock.createMock(HttpClient.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testFork() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(httpClient)
                        .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());

                send(httpClient)
                        .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build())
                        .fork(true);
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "Foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");

        Assert.assertFalse(action.isForkMode());

        action = ((SendMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testHttpMethod() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(httpClient)
                        .http()
                        .method(HttpMethod.GET)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
    }

    @Test
    public void testHttpRequestUriAndPath() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(httpClient)
                        .http()
                        .uri("http://localhost:8080/")
                        .path("/test")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 2L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(MessageHeaderEndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(MessageHeaderEndpointUriResolver.ENDPOINT_PATH_HEADER_NAME), "/test");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid use of http and soap action definition")
    public void testSendBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("httpClient", Endpoint.class)).andReturn(httpClient).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                send("httpClient")
                        .http()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("operation", "soapOperation")
                        .soap();
            }
        };

        builder.execute();

        verify(applicationContextMock);
    }

}
