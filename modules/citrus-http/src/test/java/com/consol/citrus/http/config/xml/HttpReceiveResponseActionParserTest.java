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
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessageContentBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.DefaultPayloadVariableExtractor;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpReceiveResponseActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testHttpRequestActionParser() {
        assertActionCount(4);
        assertActionClassAndName(ReceiveMessageAction.class, "http:receive-response");

        PayloadTemplateMessageBuilder messageBuilder;
        HttpMessageContentBuilder httpMessageContentBuilder;

        ReceiveMessageAction action = getNextTestActionFromTest();

        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();

        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().size(), 2L);
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.ID));
        Assert.assertNotNull(httpMessageContentBuilder.getMessage().getHeaders().get(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getVariableExtractors().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

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
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(httpMessageContentBuilder.getMessage().getHeaders().get("userId"), "1001");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((DefaultPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().size(), 1L);
        Assert.assertEquals(((DefaultPayloadVariableExtractor)action.getVariableExtractors().get(0)).getPathExpressions().get("$.user.id"), "userId");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);

        httpMessageContentBuilder = ((HttpMessageContentBuilder)action.getMessageBuilder());
        Assert.assertNotNull(httpMessageContentBuilder);
        Assert.assertEquals(httpMessageContentBuilder.getDelegate().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)httpMessageContentBuilder.getDelegate();
        Assert.assertNull(messageBuilder.getPayloadData());
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