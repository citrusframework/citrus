/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.http.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpResponseErrorHandler;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.*;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class HttpClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "httpClient1")
    @HttpClientConfig(requestUrl = "http://localhost:8080/test")
    private HttpClient httpClient1;

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8080/test",
            requestMethod=HttpMethod.GET,
            contentType="text/xml",
            charset="ISO-8859-1",
            defaultAcceptHeader=false,
            handleCookies=true,
            timeout=10000L,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            errorHandler = "errorHandler",
            messageConverter="messageConverter",
            binaryMediaTypes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/custom"},
            requestFactory="soapRequestFactory",
            endpointResolver="endpointResolver")
    private HttpClient httpClient2;

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8080/test",
            restTemplate="restTemplate",
            correlator="replyMessageCorrelator")
    private HttpClient httpClient3;

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8080/test",
            interceptors={ "clientInterceptor" },
            pollingInterval=250,
            actor="testActor")
    private HttpClient httpClient4;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
    @Mock
    private ClientHttpRequestFactory requestFactory = Mockito.mock(ClientHttpRequestFactory.class);
    @Mock
    private HttpMessageConverter messageConverter = Mockito.mock(HttpMessageConverter.class);
    @Mock
    private EndpointUriResolver endpointResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private ClientHttpRequestInterceptor clientInterceptor = Mockito.mock(ClientHttpRequestInterceptor.class);
    @Mock
    private ResponseErrorHandler errorHandler = Mockito.mock(ResponseErrorHandler.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("soapRequestFactory", ClientHttpRequestFactory.class)).thenReturn(requestFactory);
        when(applicationContext.getBean("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(applicationContext.getBean("restTemplate", RestTemplate.class)).thenReturn(restTemplate);
        when(applicationContext.getBean("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
        when(applicationContext.getBean("clientInterceptor", ClientHttpRequestInterceptor.class)).thenReturn(clientInterceptor);
        when(applicationContext.getBean("errorHandler", ResponseErrorHandler.class)).thenReturn(errorHandler);
        when(applicationContext.getBean("", ClientHttpRequestFactory.class)).thenThrow(new RuntimeException("Unexpected call to getBean on application context"));
    }

    @Test
    public void testHttpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertNotNull(httpClient1.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertTrue(HttpComponentsClientHttpRequestFactory.class.isInstance(httpClient1.getEndpointConfiguration().getRestTemplate().getRequestFactory()));
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getClientInterceptors().size(), 0L);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getRequestMethod(), HttpMethod.POST);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().isDefaultAcceptHeader(), true);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().isHandleCookies(), false);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getErrorHandler().getClass(), HttpResponseErrorHandler.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getBinaryMediaTypes().size(), 6L);

        // 2nd message sender
        Assert.assertNotNull(httpClient2.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRestTemplate().getRequestFactory(), requestFactory);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRequestMethod(), HttpMethod.GET);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getContentType(), "text/xml");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getCharset(), "ISO-8859-1");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getEndpointUriResolver(), endpointResolver);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().isDefaultAcceptHeader(), false);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().isHandleCookies(), true);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getErrorHandler(), errorHandler);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getBinaryMediaTypes().size(), 2L);
        Assert.assertTrue(httpClient2.getEndpointConfiguration().getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        Assert.assertNotNull(httpClient3.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient3.getEndpointConfiguration().getRestTemplate(), restTemplate);
        Assert.assertEquals(httpClient3.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertNotNull(httpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(httpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 4th message sender
        Assert.assertNotNull(httpClient4.getActor());
        Assert.assertEquals(httpClient4.getActor(), testActor);
        Assert.assertEquals(httpClient4.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient4.getEndpointConfiguration().getClientInterceptors().size(), 1L);
        Assert.assertEquals(httpClient4.getEndpointConfiguration().getClientInterceptors().get(0), clientInterceptor);
        Assert.assertEquals(httpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
