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

package org.citrusframework.http.config.annotation;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpResponseErrorHandler;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.jms.config.annotation.JmsEndpointConfigParser;
import org.citrusframework.jms.config.annotation.JmsSyncEndpointConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
            requestMethod= RequestMethod.GET,
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ClientHttpRequestFactory requestFactory;
    @Mock
    private HttpMessageConverter messageConverter;
    @Mock
    private EndpointUriResolver endpointResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private ClientHttpRequestInterceptor clientInterceptor;
    @Mock
    private ResponseErrorHandler errorHandler;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("soapRequestFactory", ClientHttpRequestFactory.class)).thenReturn(requestFactory);
        when(referenceResolver.resolve("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(referenceResolver.resolve("restTemplate", RestTemplate.class)).thenReturn(restTemplate);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve(new String[] {"clientInterceptor"}, ClientHttpRequestInterceptor.class)).thenReturn(Collections.singletonList(clientInterceptor));
        when(referenceResolver.resolve("errorHandler", ResponseErrorHandler.class)).thenReturn(errorHandler);
        when(referenceResolver.resolve("", ClientHttpRequestFactory.class)).thenThrow(new RuntimeException("Unexpected call to getBean on application context"));
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testHttpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertNotNull(httpClient1.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getClientInterceptors().size(), 1L);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getClientInterceptors().get(0).getClass(), LoggingClientInterceptor.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getRequestMethod(), RequestMethod.POST);
        Assert.assertTrue(httpClient1.getEndpointConfiguration().isDefaultAcceptHeader());
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(httpClient1.getEndpointConfiguration().isHandleCookies());
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getErrorHandler().getClass(), HttpResponseErrorHandler.class);
        Assert.assertEquals(httpClient1.getEndpointConfiguration().getBinaryMediaTypes().size(), 6L);

        // 2nd message sender
        Assert.assertNotNull(httpClient2.getEndpointConfiguration().getRestTemplate());
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080/test");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getRequestMethod(), RequestMethod.GET);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getContentType(), "text/xml");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getCharset(), "ISO-8859-1");
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getEndpointUriResolver(), endpointResolver);
        Assert.assertEquals(httpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertFalse(httpClient2.getEndpointConfiguration().isDefaultAcceptHeader());
        Assert.assertTrue(httpClient2.getEndpointConfiguration().isHandleCookies());
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 8L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.async"));
        Assert.assertEquals(validators.get("jms.async").getClass(), JmsEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.sync"));
        Assert.assertEquals(validators.get("jms.sync").getClass(), JmsSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.async"));
        Assert.assertEquals(validators.get("channel.async").getClass(), ChannelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.sync"));
        Assert.assertEquals(validators.get("channel.sync").getClass(), ChannelSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("http.client"));
        Assert.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assert.assertNotNull(validators.get("http.server"));
        Assert.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("http.client").isPresent());
    }
}
