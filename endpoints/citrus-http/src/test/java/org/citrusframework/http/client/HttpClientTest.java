/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.http.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.hc.core5.http.ContentType;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class HttpClientTest extends AbstractTestNGUnitTest {

    private final String requestBody = "<TestRequest><Message>Hello Citrus!</Message></TestRequest>";
    private final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

    @Mock
    private RestTemplate restTemplate;

    @BeforeMethod
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHttpPostRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testCustomHeaders() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);
        endpointConfiguration.setContentType("text/xml");
        endpointConfiguration.setCharset("ISO-8859-1");

        Message requestMessage = new DefaultMessage(requestBody)
                .setHeader("Operation", "foo");

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 2);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/xml;charset=ISO-8859-1");
            Assert.assertEquals(httpRequest.getHeaders().get("Operation").get(0), "foo");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testNoDefaultAcceptHeader() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);
        endpointConfiguration.setContentType("text/xml");
        endpointConfiguration.setCharset("ISO-8859-1");
        endpointConfiguration.setDefaultAcceptHeader(false);

        Message requestMessage = new DefaultMessage(requestBody)
                .setHeader("Operation", "foo");

        endpointConfiguration.setRestTemplate(restTemplate);

        StringHttpMessageConverter messageConverter = Mockito.mock(StringHttpMessageConverter.class);
        when(restTemplate.getMessageConverters()).thenReturn(Collections.<HttpMessageConverter<?>>singletonList(messageConverter));

        doAnswer((Answer<ResponseEntity>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 2);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/xml;charset=ISO-8859-1");
            Assert.assertEquals(httpRequest.getHeaders().get("Operation").get(0), "foo");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(messageConverter, atLeastOnce()).setWriteAcceptCharset(false);
        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testOverwriteContentTypeHeader() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);
        endpointConfiguration.setContentType("text/xml");
        endpointConfiguration.setCharset("ISO-8859-1");

        Message requestMessage = new HttpMessage(requestBody)
                .contentType("application/xml;charset=UTF-8")
                .accept("application/xml");

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 2);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "application/xml;charset=UTF-8");
            Assert.assertEquals(httpRequest.getHeaders().getAccept().get(0).toString(), "application/xml");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testOverwriteRequestMethod() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        HttpMessage requestMessage = new HttpMessage(requestBody)
                .method(HttpMethod.GET);

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertNull(httpRequest.getBody()); // null because of GET
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testHttpGetRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertNull(httpRequest.getBody()); // null because of GET
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testHttpPutRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.PUT);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testReplyMessageCorrelator() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        MessageCorrelator correlator = Mockito.mock(MessageCorrelator.class);
        endpointConfiguration.setCorrelator(correlator);

        Message requestMessage = new HttpMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(correlator);

        when(restTemplate.exchange(eq(URI.create(requestUrl)), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        when(correlator.getCorrelationKey(requestMessage)).thenReturn("correlationKey");
        when(correlator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive("correlationKey", context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testEndpointUriResolver() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new HttpMessage(requestBody);

        EndpointUriResolver endpointUriResolver = Mockito.mock(EndpointUriResolver.class);
        endpointConfiguration.setEndpointUriResolver(endpointUriResolver);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(endpointUriResolver);

        when(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8088/test")).thenReturn("http://localhost:8081/new");

        when(restTemplate.exchange(eq(URI.create("http://localhost:8081/new")), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testErrorResponsePropagateStrategy() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        endpointConfiguration.setErrorHandlingStrategy(ErrorHandlingStrategy.PROPAGATE);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doThrow(new HttpErrorPropagatingException(HttpStatus.FORBIDDEN, "Not allowed", new HttpHeaders(), responseBody.getBytes(), StandardCharsets.UTF_8)).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, 1000L);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.FORBIDDEN);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "FORBIDDEN");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testErrorResponseExceptionStrategy() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        endpointConfiguration.setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        try {
            httpClient.send(requestMessage, context);

            Assert.fail("Missing exception due to http error status code");
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getMessage(), "403 FORBIDDEN");

            verify(restTemplate).setInterceptors(anyList());
        }
    }

    @Test
    public void testHttpPatchRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.PATCH);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage(requestBody);

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testBinaryBody() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final byte[] responseBody = new byte[20];
        new Random().nextBytes(responseBody);

        final byte[] requestBody = new byte[20];
        new Random().nextBytes(requestBody);

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new HttpMessage(requestBody)
                                        .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                                        .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType());

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<?>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 2);

            Assert.assertEquals(httpRequest.getHeaders().getAccept().get(0), MediaType.APPLICATION_OCTET_STREAM);
            Assert.assertEquals(httpRequest.getHeaders().getContentType(), MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }

    @Test
    public void testNotWellFormedContentType() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(RequestMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new HttpMessage(requestBody)
                .contentType("foo");

        endpointConfiguration.setRestTemplate(restTemplate);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

            Assert.assertEquals(httpRequest.getBody().toString(), requestBody);
            Assert.assertEquals(httpRequest.getHeaders().size(), 1);

            Assert.assertEquals(httpRequest.getHeaders().getFirst(HttpMessageHeaders.HTTP_CONTENT_TYPE), "foo");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }).when(restTemplate).exchange(eq(URI.create(requestUrl)), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(anyList());
    }
}
