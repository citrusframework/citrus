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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class HttpClientTest extends AbstractTestNGUnitTest {

    private RestTemplate restTemplate = EasyMock.createMock(RestTemplate.class);

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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                        Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                        Assert.assertEquals(httpRequest.getHeaders().size(), 2);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/xml;charset=ISO-8859-1");
                        Assert.assertEquals(httpRequest.getHeaders().get("Operation").get(0), "foo");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                        Assert.assertEquals(httpRequest.getHeaders().size(), 2);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "application/xml;charset=UTF-8");
                        Assert.assertEquals(httpRequest.getHeaders().getAccept().get(0).toString(), "application/xml");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertNull(httpRequest.getBody()); // null because of GET
                        Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertNull(httpRequest.getBody()); // null because of GET
                        Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.PUT), anyObject(HttpEntity.class), eq(String.class)))
                .andAnswer(new IAnswer<ResponseEntity<String>>() {
                    public ResponseEntity<String> answer() throws Throwable {
                        HttpEntity<?> httpRequest = (HttpEntity<?>)getCurrentArguments()[2];

                        Assert.assertEquals(httpRequest.getBody().toString(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                        Assert.assertEquals(httpRequest.getHeaders().size(), 1);

                        Assert.assertEquals(httpRequest.getHeaders().getContentType().toString(), "text/plain;charset=UTF-8");

                        return new ResponseEntity<String>(responseBody, HttpStatus.OK);
                    }
                }).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate);
    }

    @Test
    public void testReplyMessageCorrelator() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.GET);
        endpointConfiguration.setRequestUrl(requestUrl);

        MessageCorrelator correlator = EasyMock.createMock(MessageCorrelator.class);
        endpointConfiguration.setCorrelator(correlator);

        Message requestMessage = new HttpMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, correlator);

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();

        expect(correlator.getCorrelationKey(requestMessage)).andReturn("correlationKey").once();
        expect(correlator.getCorrelationKeyName(anyObject(String.class))).andReturn("correlationKeyName").once();

        replay(restTemplate, correlator);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive("correlationKey", context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate, correlator);
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

        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        endpointConfiguration.setEndpointUriResolver(endpointUriResolver);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, endpointUriResolver);

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8088/test")).andReturn("http://localhost:8081/new").once();

        expect(restTemplate.exchange(eq("http://localhost:8081/new"), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();

        replay(restTemplate, endpointUriResolver);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "OK");

        verify(restTemplate, endpointUriResolver);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.FORBIDDEN)).once();

        replay(restTemplate);

        httpClient.send(requestMessage, context);

        HttpMessage responseMessage = (HttpMessage) httpClient.receive(context, 1000L);
        Assert.assertEquals(responseMessage.getStatusCode(), HttpStatus.FORBIDDEN);
        Assert.assertEquals(responseMessage.getReasonPhrase(), "FORBIDDEN");

        verify(restTemplate);
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

        restTemplate.setInterceptors(anyObject(List.class));
        expectLastCall().once();
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).once();

        replay(restTemplate);

        try {
            httpClient.send(requestMessage, context);

            Assert.fail("Missing exception due to http error status code");
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getMessage(), "403 FORBIDDEN");

            verify(restTemplate);
        }
    }
}
