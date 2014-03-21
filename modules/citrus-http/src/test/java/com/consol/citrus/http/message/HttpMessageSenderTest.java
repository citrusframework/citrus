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

package com.consol.citrus.http.message;

import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.http.*;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class HttpMessageSenderTest {

    private RestTemplate restTemplate = EasyMock.createMock(RestTemplate.class);
    private ReplyMessageHandler replyMessageHandler = EasyMock.createMock(ReplyMessageHandler.class);
    
    @Test
    public void testHttpPostRequest() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.POST);
        messageSender.setRequestUrl(requestUrl);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testCustomHeaders() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.POST);
        messageSender.setRequestUrl(requestUrl);
        messageSender.setContentType("text/xml");
        messageSender.setCharset("ISO-8859-1");
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                                  .setHeader("Operation", "foo")
                                                  .build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testOverwriteContentTypeHeader() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.POST);
        messageSender.setRequestUrl(requestUrl);
        messageSender.setContentType("text/xml");
        messageSender.setCharset("ISO-8859-1");
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                                  .setHeader("Content-Type", "application/xml;charset=UTF-8")
                                                  .setHeader("Accept", "application/xml")
                                                  .build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testOverwriteRequestMethod() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.GET);
        messageSender.setRequestUrl(requestUrl);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                                  .setHeader(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD, "GET")
                                                  .build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testHttpGetRequest() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.GET);
        messageSender.setRequestUrl(requestUrl);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testHttpPutRequest() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.PUT);
        messageSender.setRequestUrl(requestUrl);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
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
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testReplyMessageCorrelator() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.GET);
        messageSender.setRequestUrl(requestUrl);
        
        ReplyMessageCorrelator correlator = EasyMock.createMock(ReplyMessageCorrelator.class);
        messageSender.setCorrelator(correlator);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler, correlator);
        
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();
        
        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                           .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();
        
        expect(correlator.getCorrelationKey(requestMessage)).andReturn("correlationKey").once();
        
        replay(restTemplate, replyMessageHandler, correlator);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler, correlator);
    }
    
    @Test
    public void testEndpointUriResolver() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.GET);
        messageSender.setRequestUrl(requestUrl);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        messageSender.setEndpointUriResolver(endpointUriResolver);
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler, endpointUriResolver);
        
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();
        
        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8088/test")).andReturn("http://localhost:8081/new").once();
        
        expect(restTemplate.exchange(eq("http://localhost:8081/new"), eq(HttpMethod.GET), anyObject(HttpEntity.class), eq(String.class)))
                           .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.OK)).once();
        
        replay(restTemplate, replyMessageHandler, endpointUriResolver);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler, endpointUriResolver);
    }
    
    @Test
    public void testErrorResponsePropagateStrategy() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.POST);
        messageSender.setRequestUrl(requestUrl);
        
        messageSender.setErrorHandlingStrategy(ErrorHandlingStrategy.PROPAGATE);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();
        
        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                           .andReturn(new ResponseEntity<String>(responseBody, HttpStatus.FORBIDDEN)).once();
        
        replay(restTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(restTemplate, replyMessageHandler);
    }
    
    @Test
    public void testErrorResponseExceptionStrategy() {
        HttpMessageSender messageSender = new HttpMessageSender();
        String requestUrl = "http://localhost:8088/test";
        
        messageSender.setReplyMessageHandler(replyMessageHandler);

        messageSender.setRequestMethod(HttpMethod.POST);
        messageSender.setRequestUrl(requestUrl);
        
        messageSender.setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        messageSender.setRestTemplate(restTemplate);
        
        reset(restTemplate, replyMessageHandler);
        
        restTemplate.setErrorHandler(anyObject(ResponseErrorHandler.class));
        expectLastCall().once();
        
        expect(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.POST), anyObject(HttpEntity.class), eq(String.class)))
                           .andThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).once();
        
        replay(restTemplate, replyMessageHandler);
        
        try {
            messageSender.send(requestMessage);
            
            Assert.fail("Missing exception due to http error status code");
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getMessage(), "403 FORBIDDEN");
            
            verify(restTemplate, replyMessageHandler);
        }
    }
}
