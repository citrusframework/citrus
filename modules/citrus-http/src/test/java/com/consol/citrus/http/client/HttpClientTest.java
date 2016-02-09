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

package com.consol.citrus.http.client;

import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class HttpClientTest extends AbstractTestNGUnitTest {

    private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    public void testHttpPostRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testCustomHeaders() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);
        endpointConfiguration.setContentType("text/xml");
        endpointConfiguration.setCharset("ISO-8859-1");

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("Operation", "foo");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity>() {
            @Override
            public ResponseEntity answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(httpRequest.getHeaders().size(), 2);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/xml;charset=ISO-8859-1");
                Assert.assertEquals(httpRequest.getHeaders().get("Operation").get(0), "foo");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testOverwriteContentTypeHeader() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);
        endpointConfiguration.setContentType("text/xml");
        endpointConfiguration.setCharset("ISO-8859-1");

        Message requestMessage = new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .contentType("application/xml;charset=UTF-8")
                .accept("application/xml");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(httpRequest.getHeaders().size(), 2);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "application/xml;charset=UTF-8");
                Assert.assertEquals(httpRequest.getHeaders().getAccept().get(0).toString(), "application/xml");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testOverwriteRequestMethod() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        HttpMessage requestMessage = new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .method(HttpMethod.GET);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertNull(httpRequest.getBody()); // null because of GET
                Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testHttpGetRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertNull(httpRequest.getBody()); // null because of GET
                Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testHttpPutRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.PUT);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testReplyMessageCorrelator() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        MessageCorrelator correlator = Mockito.mock(MessageCorrelator.class);
        endpointConfiguration.setCorrelator(correlator);

        Message requestMessage = new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, correlator);

        when(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK));

        when(correlator.getCorrelationKey(requestMessage)).thenReturn("correlationKey");
        when(correlator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive("correlationKey", context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testEndpointUriResolver() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        EndpointUriResolver endpointUriResolver = Mockito.mock(EndpointUriResolver.class);
        endpointConfiguration.setEndpointUriResolver(endpointUriResolver);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, endpointUriResolver);

        when(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8088/test")).thenReturn("http://localhost:8081/new");

        when(restTemplate.exchange(eq("http://localhost:8081/new"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testErrorResponsePropagateStrategy() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        endpointConfiguration.setErrorHandlingStrategy(ErrorHandlingStrategy.PROPAGATE);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        when(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>(responseBody, HttpStatus.FORBIDDEN));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, 1000L);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.FORBIDDEN);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "FORBIDDEN");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }

    @Test
    public void testErrorResponseExceptionStrategy() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        endpointConfiguration.setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        try {
            httpClient.send(requestMessage, context);

            Assert.fail("Missing exception due to http error status code");
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getMessage(), "403 FORBIDDEN");

            verify(restTemplate).setInterceptors(any(List.class));
            verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
        }
    }

    @Test
    public void testHttpPatchRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.PATCH);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        doAnswer(new Answer<ResponseEntity<String>>() {
            @Override
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
                HttpEntity<?> httpRequest = (HttpEntity<?>)invocation.getArguments()[2];

                Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                return new ResponseEntity<String>(responseBody, HttpStatus.OK);
            }
        }).when(restTemplate).exchange(eq(requestUrl), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(String.class));

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate).setInterceptors(any(List.class));
        verify(restTemplate).setErrorHandler(any(ResponseErrorHandler.class));
    }
}
