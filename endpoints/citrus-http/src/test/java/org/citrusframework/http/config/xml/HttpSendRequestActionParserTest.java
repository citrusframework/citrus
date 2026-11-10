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

package org.citrusframework.http.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpSendRequestActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(SendMessageAction.class, "http:send-request");

        SendMessageAction action = getNextTestActionFromTest();
        HttpMessageBuilder httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpMethod.GET.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpClient", HttpClient.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(10L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(HttpMethod.GET.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/order/${id}", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals("/order/${id}", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals("text/xml", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals("text/xml", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_ACCEPT));
        Assertions.assertEquals("HTTP/1.1", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals("id=12345,type=gold", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("id=12345,type=gold", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("http://localhost:8080/test", action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("<user><id>1001</id><name>new_user</name></user>", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(HttpMethod.POST.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/user", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals("/test", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTEXT_PATH));
        Assertions.assertEquals("1001", httpMessageBuilder.getMessage().getHeaders().get("userId"));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals(1, httpMessageBuilder.getMessage().getCookies().size());
        jakarta.servlet.http.Cookie cookie = httpMessageBuilder.getMessage().getCookies().get(0);
        Assertions.assertEquals("session", cookie.getName());
        Assertions.assertEquals("abc123", cookie.getValue());
        Assertions.assertEquals("/user", cookie.getPath());
        Assertions.assertEquals("example.com", cookie.getDomain());
        Assertions.assertEquals(3600, cookie.getMaxAge());
        Assertions.assertTrue(cookie.getSecure());
        Assertions.assertTrue(cookie.isHttpOnly());
        Assertions.assertEquals(beanDefinitionContext.getBean("httpClient", HttpClient.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(HttpMethod.DELETE.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals("/user/${id}", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpClient", HttpClient.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(HttpMethod.HEAD.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals("http://localhost:8080/test", httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpClient", HttpClient.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(HttpMethod.OPTIONS.name(), httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("http://localhost:8080/test", action.getEndpointUri());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), action.getActor());
    }

    @Test
    public void testHttpRequestActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to invalid attributes");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertTrue(e.getCause().getMessage().startsWith("Neither http request uri nor http client endpoint reference is given"));
        }
    }
}
