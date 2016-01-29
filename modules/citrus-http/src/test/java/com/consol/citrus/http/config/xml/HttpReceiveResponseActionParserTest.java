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
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
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

        ReceiveMessageAction action = getNextTestActionFromTest();

        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof DefaultValidationContext);

        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();

        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getVariableExtractors().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof DefaultValidationContext);

        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 3L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_STATUS_CODE), "404");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_REASON_PHRASE), "NOT_FOUND");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(HttpMessageHeaders.HTTP_VERSION), "HTTP/1.1");
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "http://localhost:8080/test");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);

        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<user><id>1001</id><name>new_user</name></user>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("userId"), "1001");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("httpClient", HttpClient.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getVariableExtractors().size(), 1L);
        Assert.assertEquals(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().size(), 1L);
        Assert.assertEquals(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().get("$.user.id"), "userId");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof DefaultValidationContext);

        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
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