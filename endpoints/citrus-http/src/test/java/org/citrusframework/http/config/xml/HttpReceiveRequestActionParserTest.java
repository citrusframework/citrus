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
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpReceiveRequestActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(ReceiveMessageAction.class, "http:receive-request");

        HttpMessageBuilder httpMessageBuilder;

        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getMessageProcessors().size(), 0);
        Assert.assertEquals(action.getControlMessageProcessors().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

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
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "alive=,id=12345,type=gold");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "alive=,id=12345,type=gold");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().size(), 1L);
        Assert.assertEquals(((DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().get("$.user.id"), "userId");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.DELETE.name());
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/user/${id}");
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.HEAD.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.OPTIONS.name());
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assert.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpServer", HttpServer.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
