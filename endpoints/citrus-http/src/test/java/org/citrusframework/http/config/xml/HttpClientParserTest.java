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

import java.util.Map;
import org.citrusframework.TestActor;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpResponseErrorHandler;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.util.ObjectHelper;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.http.MediaType;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testHttpClientParser() {
        Map<String, HttpClient> clients = beanDefinitionContext.getBeansOfType(HttpClient.class);

        Assertions.assertEquals(4, clients.size());

        // 1st message sender
        HttpClient httpClient = clients.get("httpClient1");
        Assertions.assertEquals("httpClient1", httpClient.getName());
        Assertions.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals("http://localhost:8080/test", httpClient.getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals(InterceptingClientHttpRequestFactory.class, httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals(1L, httpClient.getEndpointConfiguration().getClientInterceptors().size());
        Assertions.assertEquals(LoggingClientInterceptor.class, httpClient.getEndpointConfiguration().getClientInterceptors().get(0).getClass());
        Assertions.assertEquals(6L, httpClient.getEndpointConfiguration().getBinaryMediaTypes().size());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, httpClient.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(HttpResponseErrorHandler.class, httpClient.getEndpointConfiguration().getErrorHandler().getClass());
        Assertions.assertEquals(RequestMethod.POST, httpClient.getEndpointConfiguration().getRequestMethod());
        Assertions.assertTrue(httpClient.getEndpointConfiguration().isDefaultAcceptHeader());
        Assertions.assertEquals(DefaultMessageCorrelator.class, httpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(5000L, httpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(httpClient.getEndpointConfiguration().isHandleCookies());
        Assertions.assertFalse(httpClient.getEndpointConfiguration().isDisableRedirectHandling());

        // 2nd message sender
        httpClient = clients.get("httpClient2");
        Assertions.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals("http://localhost:8080/test", httpClient.getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals(InterceptingClientHttpRequestFactory.class, httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals(RequestMethod.GET, httpClient.getEndpointConfiguration().getRequestMethod());
        Assertions.assertEquals(DefaultMessageCorrelator.class, httpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("text/xml", httpClient.getEndpointConfiguration().getContentType());
        Assertions.assertEquals("ISO-8859-1", httpClient.getEndpointConfiguration().getCharset());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), httpClient.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("endpointResolver"), httpClient.getEndpointConfiguration().getEndpointUriResolver());
        Assertions.assertEquals(10000L, httpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(httpClient.getEndpointConfiguration().isDefaultAcceptHeader());
        Assertions.assertTrue(httpClient.getEndpointConfiguration().isHandleCookies());
        Assertions.assertTrue(httpClient.getEndpointConfiguration().isDisableRedirectHandling());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, httpClient.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(beanDefinitionContext.getBean("errorHandler"), httpClient.getEndpointConfiguration().getErrorHandler());
        Assertions.assertEquals(2L, httpClient.getEndpointConfiguration().getBinaryMediaTypes().size());
        Assertions.assertTrue(httpClient.getEndpointConfiguration().getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        httpClient = clients.get("httpClient3");
        Assertions.assertNotNull(httpClient.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals(beanDefinitionContext.getBean("restTemplate"), httpClient.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals("http://localhost:8080/test", httpClient.getEndpointConfiguration().getRequestUrl());
        Assertions.assertNotNull(httpClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), httpClient.getEndpointConfiguration().getCorrelator());

        // 4th message sender
        httpClient = clients.get("httpClient4");
        Assertions.assertNotNull(httpClient.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), httpClient.getActor());
        Assertions.assertEquals(InterceptingClientHttpRequestFactory.class, httpClient.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals(1L, httpClient.getEndpointConfiguration().getClientInterceptors().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("clientInterceptor"), httpClient.getEndpointConfiguration().getClientInterceptors().get(0));
        Assertions.assertEquals(250L, httpClient.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testBothRestTemplateAndRequestFactorySet() {
        try {
            createApplicationContext("failed1");
            Assertions.fail("Missing bean creation exception due to rest template and request factory property set");
        } catch (BeanDefinitionParsingException e) {
            Assertions.assertTrue(ObjectHelper.assertNotNull(e.getMessage()).contains("no 'request-factory' should be set"), e.getMessage());
        }
    }

    @Test
    public void testMissingRequestUrlOrEndpointResolver() {
        try {
            createApplicationContext("failed2");
            Assertions.fail("Missing bean creation exception due to missing request url or endpoint resolver");
        } catch (BeanDefinitionParsingException e) {
            Assertions.assertTrue(ObjectHelper.assertNotNull(e.getMessage()).contains("One of the properties 'request-url' or 'endpoint-resolver' is required"));
        }
    }
}
