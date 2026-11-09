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

package org.citrusframework.http.config.annotation;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelSyncEndpointConfigParser;
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
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HttpClientConfigParserTest extends AbstractUnitTest {

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
            disableRedirectHandling = true,
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

    @BeforeEach
    public void setup() {
        lenient().when(referenceResolver.resolve("soapRequestFactory", ClientHttpRequestFactory.class)).thenReturn(requestFactory);
        lenient().when(referenceResolver.resolve("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        lenient().when(referenceResolver.resolve("restTemplate", RestTemplate.class)).thenReturn(restTemplate);
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        lenient().when(referenceResolver.resolve(new String[] {"clientInterceptor"}, ClientHttpRequestInterceptor.class)).thenReturn(Collections.singletonList(clientInterceptor));
        lenient().when(referenceResolver.resolve("errorHandler", ResponseErrorHandler.class)).thenReturn(errorHandler);
        lenient().when(referenceResolver.resolve("", ClientHttpRequestFactory.class)).thenThrow(new RuntimeException("Unexpected call to getBean on application context"));
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testHttpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assertions.assertNotNull(httpClient1.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals( "http://localhost:8080/test",httpClient1.getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals( InterceptingClientHttpRequestFactory.class,httpClient2.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals( 1L,httpClient1.getEndpointConfiguration().getClientInterceptors().size());
        Assertions.assertEquals( LoggingClientInterceptor.class,httpClient1.getEndpointConfiguration().getClientInterceptors().get(0).getClass());
        Assertions.assertEquals( RequestMethod.POST,httpClient1.getEndpointConfiguration().getRequestMethod());
        Assertions.assertTrue(httpClient1.getEndpointConfiguration().isDefaultAcceptHeader());
        Assertions.assertEquals( DefaultMessageCorrelator.class,httpClient1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals( 5000L,httpClient1.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(httpClient1.getEndpointConfiguration().isHandleCookies());
        Assertions.assertEquals( ErrorHandlingStrategy.PROPAGATE,httpClient1.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals( HttpResponseErrorHandler.class,httpClient1.getEndpointConfiguration().getErrorHandler().getClass());
        Assertions.assertEquals( 6L,httpClient1.getEndpointConfiguration().getBinaryMediaTypes().size());

        // 2nd message sender
        Assertions.assertNotNull(httpClient2.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals( "http://localhost:8080/test",httpClient2.getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals( InterceptingClientHttpRequestFactory.class,httpClient2.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals( RequestMethod.GET,httpClient2.getEndpointConfiguration().getRequestMethod());
        Assertions.assertEquals( DefaultMessageCorrelator.class,httpClient2.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals( "text/xml",httpClient2.getEndpointConfiguration().getContentType());
        Assertions.assertEquals( "ISO-8859-1",httpClient2.getEndpointConfiguration().getCharset());
        Assertions.assertEquals( messageConverter,httpClient2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals( endpointResolver,httpClient2.getEndpointConfiguration().getEndpointUriResolver());
        Assertions.assertEquals( 10000L,httpClient2.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(httpClient2.getEndpointConfiguration().isDefaultAcceptHeader());
        Assertions.assertTrue(httpClient2.getEndpointConfiguration().isHandleCookies());
        Assertions.assertTrue(httpClient2.getEndpointConfiguration().isDisableRedirectHandling());
        Assertions.assertEquals( ErrorHandlingStrategy.THROWS_EXCEPTION,httpClient2.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals( errorHandler,httpClient2.getEndpointConfiguration().getErrorHandler());
        Assertions.assertEquals( 2L,httpClient2.getEndpointConfiguration().getBinaryMediaTypes().size());
        Assertions.assertTrue(httpClient2.getEndpointConfiguration().getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        Assertions.assertNotNull(httpClient3.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals( restTemplate,httpClient3.getEndpointConfiguration().getRestTemplate());
        Assertions.assertEquals( "http://localhost:8080/test",httpClient3.getEndpointConfiguration().getRequestUrl());
        Assertions.assertNotNull(httpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals( messageCorrelator,httpClient3.getEndpointConfiguration().getCorrelator());

        // 4th message sender
        Assertions.assertNotNull(httpClient4.getActor());
        Assertions.assertEquals( testActor,httpClient4.getActor());
        Assertions.assertEquals( InterceptingClientHttpRequestFactory.class,httpClient4.getEndpointConfiguration().getRestTemplate().getRequestFactory().getClass());
        Assertions.assertEquals( 1L,httpClient4.getEndpointConfiguration().getClientInterceptors().size());
        Assertions.assertEquals( clientInterceptor,httpClient4.getEndpointConfiguration().getClientInterceptors().get(0));
        Assertions.assertEquals( 250L,httpClient4.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals( 8L,validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals( DirectEndpointConfigParser.class,validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals( DirectSyncEndpointConfigParser.class,validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("jms.async"));
        Assertions.assertEquals( JmsEndpointConfigParser.class,validators.get("jms.async").getClass());
        Assertions.assertNotNull(validators.get("jms.sync"));
        Assertions.assertEquals( JmsSyncEndpointConfigParser.class,validators.get("jms.sync").getClass());
        Assertions.assertNotNull(validators.get("channel.async"));
        Assertions.assertEquals( ChannelEndpointConfigParser.class,validators.get("channel.async").getClass());
        Assertions.assertNotNull(validators.get("channel.sync"));
        Assertions.assertEquals( ChannelSyncEndpointConfigParser.class,validators.get("channel.sync").getClass());
        Assertions.assertNotNull(validators.get("http.client"));
        Assertions.assertEquals( HttpClientConfigParser.class,validators.get("http.client").getClass());
        Assertions.assertNotNull(validators.get("http.server"));
        Assertions.assertEquals( HttpServerConfigParser.class,validators.get("http.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("http.client").isPresent());
    }
}
