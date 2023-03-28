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

package org.citrusframework.http.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpSendRequestActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(SendMessageAction.class, "http:send-request");

        SendMessageAction action = getNextTestActionFromTest();
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 10L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/order/${id}");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/order/${id}");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/xml");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_ACCEPT), "text/xml");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "id=12345,type=gold");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "id=12345,type=gold");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.DELETE.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user/${id}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.HEAD.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/test");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.OPTIONS.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");
        Assert.assertEquals(action.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

    @Test
    public void testHttpRequestActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to invalid attributes");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().startsWith("Neither http request uri nor http client endpoint reference is given"));
        }
    }

}
