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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.http.message.HttpMessageContentBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpSendResponseActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(4);
        assertActionClassAndName(SendMessageAction.class, "http:send-response");

        PayloadTemplateMessageBuilder messageBuilder;
        HttpMessageContentBuilder httpMessageContentBuilder;

        SendMessageAction action = getNextTestActionFromTest();

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();

        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

}