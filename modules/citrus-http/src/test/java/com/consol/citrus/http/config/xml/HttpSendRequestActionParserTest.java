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
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpSendRequestActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(SendMessageAction.class, "http:get");

        SendMessageAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.GET);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "");
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath());
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.GET);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "");
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath(), "/order/${id}");
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams(), "id=12345,type=gold");
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.POST);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(((HttpMessage)((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath(), "/user");
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.DELETE);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "");
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath(), "/user/${id}");
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.HEAD);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "");
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath());
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams());
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/test");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        Assert.assertEquals(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getRequestMethod(), HttpMethod.OPTIONS);
        Assert.assertEquals(((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(), "");
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getPath());
        Assert.assertNull(((HttpMessage) ((StaticMessageContentBuilder) action.getMessageBuilder()).getMessage()).getQueryParams());
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