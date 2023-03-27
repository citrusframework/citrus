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

package org.citrusframework.citrus.http.config.xml;

import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.message.HttpMessageBuilder;
import org.citrusframework.citrus.http.message.HttpMessageHeaders;
import org.citrusframework.citrus.message.MessageHeaders;
import org.citrusframework.citrus.testng.AbstractActionParserTest;
import org.citrusframework.citrus.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.validation.json.JsonMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpReceiveResponseActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(4);
        assertActionClassAndName(ReceiveMessageAction.class, "http:receive-response");

        HttpMessageBuilder httpMessageBuilder;

        ReceiveMessageAction action = getNextTestActionFromTest();

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);

        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
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
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().size(), 5L);
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageBuilder = ((HttpMessageBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageBuilder);
        Assert.assertEquals(httpMessageBuilder.buildMessagePayload(context, action.getMessageType()), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
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
