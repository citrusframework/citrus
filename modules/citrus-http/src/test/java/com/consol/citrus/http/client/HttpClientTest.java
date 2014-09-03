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
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.message.ReplyMessageCorrelator;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class HttpClientTest {

    private RestTemplate restTemplate = EasyMock.createMock(RestTemplate.class);

    @Test
    public void testHttpPostRequest() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        HttpClient httpClient = new HttpClient(endpointConfiguration);
        String requestUrl = "http://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setRequestMethod(HttpMethod.POST);
        endpointConfiguration.setRequestUrl(requestUrl);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("Operation", "foo")
                .build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("Content-Type", "application/xml;charset=UTF-8")
                .setHeader("Accept", "application/xml")
                .build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD, "GET")
                .build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

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

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        ReplyMessageCorrelator correlator = EasyMock.createMock(ReplyMessageCorrelator.class);
        endpointConfiguration.setCorrelator(correlator);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, correlator);

        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();

        expect(correlator.getCorrelationKey(requestMessage)).andReturn("correlationKey").once();

        replay(restTemplate, correlator);

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive("correlationKey", endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        endpointConfiguration.setEndpointUriResolver(endpointUriResolver);

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate, endpointUriResolver);

        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8088/test")).andReturn("http://localhost:8081/new").once();

        expect(restTemplate.exchange(eq("http://localhost:8081/new"), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();

        replay(restTemplate, endpointUriResolver);

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "OK");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.FORBIDDEN)).once();

        replay(restTemplate);

        httpClient.send(requestMessage);

        Message<?> responseMessage = httpClient.receive(1000L);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.FORBIDDEN);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE), "FORBIDDEN");

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

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        endpointConfiguration.setRestTemplate(restTemplate);

        reset(restTemplate);

        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();

        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                .andThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).once();

        replay(restTemplate);

        try {
            httpClient.send(requestMessage);

            Assert.fail("Missing exception due to http error status code");
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getMessage(), "403 FORBIDDEN");

            verify(restTemplate);
        }
    }
}
