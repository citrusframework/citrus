/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpResponseErrorHandler;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.util.ObjectHelper;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.http.MediaType;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HttpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testHttpClientParser() {
        Map<String, HttpClient> clients = beanDefinitionContext.getBeansOfType(HttpClient.class);

        Assert.assertEquals(clients.size(), 4);

        // 1st message sender
        HttpClient httpClient = clients.get("httpClient1");
        Assert.assertEquals(httpClient.getName(), "httpClient1");
        Assert.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getClientInterceptors().size(), 1L);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getClientInterceptors().get(0).getClass(), LoggingClientInterceptor.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getBinaryMediaTypes().size(), 6L);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getErrorHandler().getClass(), HttpResponseErrorHandler.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRequestMethod(), RequestMethod.POST);
        Assert.assertTrue(httpClient.getEndpointConfiguration().isDefaultAcceptHeader());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(httpClient.getEndpointConfiguration().isHandleCookies());

        // 2nd message sender
        httpClient = clients.get("httpClient2");
        Assert.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRequestMethod(), RequestMethod.GET);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getContentType(), "text/xml");
        Assert.assertEquals(httpClient.getEndpointConfiguration().getCharset(), "ISO-8859-1");
        Assert.assertEquals(httpClient.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getEndpointUriResolver(), beanDefinitionContext.getBean("endpointResolver"));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertFalse(httpClient.getEndpointConfiguration().isDefaultAcceptHeader());
        Assert.assertTrue(httpClient.getEndpointConfiguration().isHandleCookies());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getErrorHandler(), beanDefinitionContext.getBean("errorHandler"));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getBinaryMediaTypes().size(), 2L);
        Assert.assertTrue(httpClient.getEndpointConfiguration().getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        httpClient = clients.get("httpClient3");
        Assert.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRestTemplate(), beanDefinitionContext.getBean("restTemplate"));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertNotNull(httpClient.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(httpClient.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));

        // 4th message sender
        httpClient = clients.get("httpClient4");
        Assert.assertNotNull(httpClient.getActor());
        Assert.assertEquals(httpClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getClientInterceptors().size(), 1L);
        Assert.assertEquals(httpClient.getEndpointConfiguration().getClientInterceptors().get(0), beanDefinitionContext.getBean("clientInterceptor"));
        Assert.assertEquals(httpClient.getEndpointConfiguration().getPollingInterval(), 250L);
    }

    @Test
    public void testBothRestTemplateAndRequestFactorySet() {
        try {
            createApplicationContext("failed1");
            Assert.fail("Missing bean creation exception due to rest template and request factory property set");
        } catch (BeanDefinitionParsingException e) {
            Assert.assertTrue(ObjectHelper.assertNotNull(e.getMessage()).contains("no 'request-factory' should be set"), e.getMessage());
        }
    }

    @Test
    public void testMissingRequestUrlOrEndpointResolver() {
        try {
            createApplicationContext("failed2");
            Assert.fail("Missing bean creation exception due to missing request url or endpoint resolver");
        } catch (BeanDefinitionParsingException e) {
            Assert.assertTrue(ObjectHelper.assertNotNull(e.getMessage()).contains("One of the properties 'request-url' or 'endpoint-resolver' is required"));
        }
    }

}
