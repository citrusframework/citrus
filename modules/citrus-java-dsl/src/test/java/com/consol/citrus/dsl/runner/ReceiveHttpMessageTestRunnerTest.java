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
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveHttpMessageTestRunnerTest extends AbstractTestNGUnitTest {

    private SelectiveConsumer messageConsumer = EasyMock.createMock(SelectiveConsumer.class);
    private HttpEndpointConfiguration configuration = EasyMock.createMock(HttpEndpointConfiguration.class);
    private HttpClient httpClient = EasyMock.createMock(HttpClient.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testHttpRequestProperties() {
        reset(httpClient, messageConsumer, configuration);
        expect(httpClient.createConsumer()).andReturn(messageConsumer).once();
        expect(httpClient.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(httpClient.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .uri("/test")
                .contextPath("foo")
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")).atLeastOnce();
        replay(httpClient, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(httpClient)
                                .http()
                                .method(HttpMethod.GET)
                                .uri("/test")
                                .contextPath("foo")
                                .queryParam("param1", "value1")
                                .queryParam("param2", "value2")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getValidationContexts().size(), 1L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) ((XmlMessageValidationContext) action.getValidationContexts().get(0)).getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 4L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_CONTEXT_PATH), "foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/test");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "param1=value1,param2=value2");

        verify(httpClient, messageConsumer, configuration);
    }

    @Test
    public void testHttpResponseProperties() {
        reset(httpClient, messageConsumer, configuration);
        expect(httpClient.createConsumer()).andReturn(messageConsumer).once();
        expect(httpClient.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(httpClient.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET)
                .uri("/test")
                .statusCode(HttpStatus.OK)
                .reasonPhrase(HttpStatus.OK.name())
                .version("HTTP/1.1")).atLeastOnce();
        replay(httpClient, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(httpClient)
                                .http()
                                .method(HttpMethod.GET)
                                .status(HttpStatus.OK)
                                .version("HTTP/1.1")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getValidationContexts().size(), 1L);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) ((XmlMessageValidationContext) action.getValidationContexts().get(0)).getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 4L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), "200");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "OK");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");

        verify(httpClient, messageConsumer, configuration);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid use of http and soap action builder")
    public void testReceiveBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);
        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("httpClient")
                                .http()
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "soapOperation")
                                .soap();
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "httpClient");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        verify(applicationContextMock);
    }
}
