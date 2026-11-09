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
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.springframework.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpReceiveRequestActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(6);
        assertActionClassAndName(ReceiveMessageAction.class, "http:receive-request");

        HttpMessageBuilder httpMessageBuilder;

        ReceiveMessageAction action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( 3L,httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals( HttpMethod.GET.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertEquals( 0,action.getMessageProcessors().size());
        Assertions.assertEquals( 0,action.getControlMessageProcessors().size());

        action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( 10L,httpMessageBuilder.getMessage().getHeaders().size());
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assertions.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals( HttpMethod.GET.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/order/${id}",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals( "/order/${id}",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_URI));
        Assertions.assertEquals( "text/xml",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE));
        Assertions.assertEquals( "text/xml",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_ACCEPT));
        Assertions.assertEquals( "HTTP/1.1",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION));
        Assertions.assertEquals( "alive=,id=12345,type=gold",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( "alive=,id=12345,type=gold",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "<user><id>1001</id><name>new_user</name></user>",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( HttpMethod.POST.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/user",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertEquals( "/test",httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_CONTEXT_PATH));
        Assertions.assertEquals( "1001",httpMessageBuilder.getMessage().getHeaders().get("userId"));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( 1,httpMessageBuilder.getMessage().getCookies().size());
        jakarta.servlet.http.Cookie cookie = httpMessageBuilder.getMessage().getCookies().get(0);
        Assertions.assertEquals( "session",cookie.getName());
        Assertions.assertEquals( "abc123",cookie.getValue());
        Assertions.assertEquals( "/user",cookie.getPath());
        Assertions.assertEquals( "example.com",cookie.getDomain());
        Assertions.assertEquals( 3600,cookie.getMaxAge());
        Assertions.assertTrue(cookie.getSecure());
        Assertions.assertTrue(cookie.isHttpOnly());
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        Assertions.assertEquals( 1L,action.getVariableExtractors().size());
        Assertions.assertEquals( 1L,((DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().size());
        Assertions.assertEquals( "userId",((DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().get("$.user.id"));

        action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( HttpMethod.DELETE.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertEquals( "/user/${id}",httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( HttpMethod.HEAD.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assertions.assertEquals( 2,action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assertions.assertNotNull(httpMessageBuilder);
        Assertions.assertEquals( "",httpMessageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals( HttpMethod.OPTIONS.name(),httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REQUEST_METHOD));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME));
        Assertions.assertNull(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_QUERY_PARAMS));
        Assertions.assertEquals( beanDefinitionContext.getBean("httpServer", HttpServer.class),action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertEquals( beanDefinitionContext.getBean("testActor", TestActor.class),action.getActor());
    }
}
