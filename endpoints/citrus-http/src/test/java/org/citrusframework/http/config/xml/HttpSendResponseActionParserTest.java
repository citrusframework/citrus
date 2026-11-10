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
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpSendResponseActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(4);
        assertActionClassAndName(SendMessageAction.class, "http:send-response");

        HttpMessageBuilder httpMessageBuilder;

        SendMessageAction action = getNextTestActionFromTest();

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);

        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context,action.getMessageType()));
        Assertions.assertEquals(2L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpServer", HttpServer.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(5L, httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals("404", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE));
        Assertions.assertEquals("NOT_FOUND", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE));
        Assertions.assertEquals("HTTP/1.1", httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpServer", HttpServer.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("<user><id>1001</id><name>new_user</name></user>", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("1001", httpMessageBuilder.getMessage().getHeaders().get("userId"));
        Assertions.assertEquals(1, httpMessageBuilder.getMessage().getCookies().size());
        jakarta.servlet.http.Cookie cookie = httpMessageBuilder.getMessage().getCookies().get(0);
        Assertions.assertEquals("session", cookie.getName());
        Assertions.assertEquals("abc123", cookie.getValue());
        Assertions.assertEquals("/user", cookie.getPath());
        Assertions.assertEquals("example.com", cookie.getDomain());
        Assertions.assertEquals(3600, cookie.getMaxAge());
        Assertions.assertTrue(cookie.getSecure());
        Assertions.assertTrue(cookie.isHttpOnly());
        Assertions.assertEquals(beanDefinitionContext.getBean("httpServer", HttpServer.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals("", httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(beanDefinitionContext.getBean("httpServer", HttpServer.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), action.getActor());
    }
}
