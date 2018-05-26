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
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.http.message.HttpMessageContentBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.DefaultPayloadVariableExtractor;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpReceiveRequestActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(ReceiveMessageAction.class, "http:receive-request");

        PayloadTemplateMessageBuilder messageBuilder;
        HttpMessageContentBuilder httpMessageContentBuilder;

        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder) httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getVariableExtractors().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().size(), 10L);
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/order/${id}");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI), "/order/${id}");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/xml");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_ACCEPT), "text/xml");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "alive=,id=12345,type=gold");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME), "alive=,id=12345,type=gold");
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((DefaultPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().size(), 1L);
        Assert.assertEquals(((DefaultPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().get("$.user.id"), "userId");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.DELETE.name());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user/${id}");
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.HEAD.name());
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertNotNull(httpMessageContentBuilder.getDelegate());
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.OPTIONS.name());
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageContentBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}