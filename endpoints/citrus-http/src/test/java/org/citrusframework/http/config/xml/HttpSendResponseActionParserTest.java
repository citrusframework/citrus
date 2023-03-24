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
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpSendResponseActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(4);
        assertActionClassAndName(SendMessageAction.class, "http:send-response");

        HttpMessageBuilder httpMessageBuilder;

        SendMessageAction action = getNextTestActionFromTest();

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);

        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context,action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

}
