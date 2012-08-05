/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestActor;
import com.consol.citrus.http.message.HttpMessageSender;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;

/**
 * @author Christoph Deppisch
 */
public class HttpMessageSenderParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, HttpMessageSender> messageSenders = beanDefinitionContext.getBeansOfType(HttpMessageSender.class);
        
        Assert.assertEquals(messageSenders.size(), 4);
        
        // 1st message sender
        HttpMessageSender messageSender = messageSenders.get("httpMessageSender1");
        Assert.assertNotNull(messageSender.getRestTemplate());
        Assert.assertEquals(messageSender.getRequestUrl(), "http://localhost:8080/test");
        Assert.assertTrue(messageSender.getRestTemplate().getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory);
        Assert.assertEquals(messageSender.getRequestMethod(), HttpMethod.POST);
        Assert.assertNull(messageSender.getCorrelator());
        
        // 2nd message sender
        messageSender = messageSenders.get("httpMessageSender2");
        Assert.assertNotNull(messageSender.getRestTemplate());
        Assert.assertEquals(messageSender.getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(messageSender.getRestTemplate().getRequestFactory(), beanDefinitionContext.getBean("soapRequestFactory"));
        Assert.assertEquals(messageSender.getRequestMethod(), HttpMethod.GET);
        Assert.assertNull(messageSender.getCorrelator());
        Assert.assertEquals(messageSender.getContentType(), "text/xml");
        Assert.assertEquals(messageSender.getCharset(), "ISO-8859-1");
        Assert.assertEquals(messageSender.getEndpointUriResolver(), beanDefinitionContext.getBean("endpointResolver"));
        
        // 3rd message sender
        messageSender = messageSenders.get("httpMessageSender3");
        Assert.assertNotNull(messageSender.getRestTemplate());
        Assert.assertEquals(messageSender.getRestTemplate(), beanDefinitionContext.getBean("restTemplate"));
        Assert.assertEquals(messageSender.getRequestUrl(), "http://localhost:8080/test");
        Assert.assertNotNull(messageSender.getCorrelator());
        Assert.assertEquals(messageSender.getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));
        
        // 4th message sender
        messageSender = messageSenders.get("httpMessageSender4");
        Assert.assertNotNull(messageSender.getActor());
        Assert.assertEquals(messageSender.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
