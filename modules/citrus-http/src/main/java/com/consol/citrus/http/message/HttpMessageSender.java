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

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.integration.Message;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.*;

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.adapter.common.endpoint.MessageHeaderEndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.util.MessageUtils;

/**
 * Message sender implementation sending messages over Http.
 * 
 * Note: Message sender is only using POST request method to publish
 * messages to the service endpoint.
 * 
 * @author Christoph Deppisch
 */
public class HttpMessageSender implements MessageSender {

    /** Http url as service destination */
    private String requestUrl;

    /** Request method */
    private HttpMethod requestMethod = HttpMethod.POST;
    
    /** The request charset */
    private String charset = "UTF-8";
    
    /** Default content type */
    private String contentType = "text/plain";

    /** The reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** The reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /** The rest template */
    private RestTemplate restTemplate;
    
    /** Resolves dynamic endpoint uri */
    private EndpointUriResolver endpointUriResolver = new MessageHeaderEndpointUriResolver();
    
    /** Header mapper */
    private HeaderMapper<HttpHeaders> headerMapper = DefaultHttpHeaderMapper.outboundMapper();
    
    /** Should http errors be handled with reply message handler or simply throw exception */
    private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.PROPAGATE;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(HttpMessageSender.class);
    
    /**
     * Default constructor.
     */
    public HttpMessageSender() {
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }
    
    /**
     * Constructor using custom client request factory.
     * @param requestFactory the custom request factory.
     */
    public HttpMessageSender(ClientHttpRequestFactory requestFactory) {
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
    }
    
    /**
     * Constructor using custom rest template.
     * @param restTemplate the custom rest template.
     */
    public HttpMessageSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        String endpointUri;
        if (endpointUriResolver != null) {
            endpointUri = endpointUriResolver.resolveEndpointUri(message, getRequestUrl());
        } else {
            endpointUri = getRequestUrl();
        }
        
        log.info("Sending HTTP message to: '" + endpointUri + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:\n" + message.getPayload().toString());
        }

        HttpEntity<?> requestEntity = generateRequest(message);
        
        HttpMethod method = requestMethod;
        if (message.getHeaders().containsKey(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD)) {
            method = HttpMethod.valueOf((String)message.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD));
        }
        
        restTemplate.setErrorHandler(new InternalResponseErrorHandler(message));
        ResponseEntity<?> response = restTemplate.exchange(endpointUri, method, requestEntity, String.class);
        
        log.info("HTTP message was successfully sent to endpoint: '" + endpointUri + "'");
        
        informReplyMessageHandler(buildResponseMessage(response.getHeaders(), 
                                                       response.getBody() != null ? response.getBody() : "", 
                                                       response.getStatusCode()), message);
    }
    
    /**
     * Informs reply message handler for further processing 
     * of reply message.
     * @param responseMessage the reply message.
     * @param requestMessage the initial request message.
     */
    protected void informReplyMessageHandler(Message<?> responseMessage, Message<?> requestMessage) {
        if (replyMessageHandler != null) {
            log.info("Informing reply message handler for further processing");
            
            if (correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(requestMessage));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }
    
    /**
     * Builds the actual integration message from HTTP response entity.
     * @param response the HTTP response entity.
     * @return
     */
    private Message<?> buildResponseMessage(HttpHeaders headers, Object responseBody, HttpStatus statusCode) {
        Map<String, ?> mappedHeaders = headerMapper.toHeaders(headers);
        
        Message<?> responseMessage = MessageBuilder.withPayload(responseBody)
                                            .copyHeaders(mappedHeaders)
                                            .copyHeaders(getCustomHeaders(headers, mappedHeaders))
                                            .setHeader(CitrusHttpMessageHeaders.HTTP_STATUS_CODE, statusCode)
                                            .setHeader(CitrusHttpMessageHeaders.HTTP_VERSION, "HTTP/1.1") //TODO check if we have access to version information
                                            .setHeader(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE, statusCode.name())
                                            .build();
        
        return responseMessage;
    }

    /**
     * Message headers consist of standard HTTP message headers and custom headers.
     * This method assumes that all header entries that were not initially mapped 
     * by header mapper implementations are custom headers.
     * 
     * @param httpHeaders all message headers in their pre nature.
     * @param mappedHeaders the previously mapped header entries (all standard headers). 
     * @return
     */
    private Map<String, String> getCustomHeaders(HttpHeaders httpHeaders, Map<String, ?> mappedHeaders) {
        Map<String, String> customHeaders = new HashMap<String, String>();
        
        for (Entry<String, List<String>> header : httpHeaders.entrySet()) {
            if (!mappedHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(header.getValue()));
            }
        }
        
        return customHeaders;
    }

    /**
     * Generate http request entity from Spring Integration message.
     * @param requestMessage
     * @return
     */
    private HttpEntity<?> generateRequest(Message<?> requestMessage) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headerMapper.fromHeaders(requestMessage.getHeaders(), httpHeaders);
        
        Map<String, ?> messageHeaders = requestMessage.getHeaders();
        for (Entry<String, ?> header : messageHeaders.entrySet()) {
            if (!header.getKey().startsWith(CitrusMessageHeaders.PREFIX) && 
                    !MessageUtils.isSpringInternalHeader(header.getKey()) &&
                    !httpHeaders.containsKey(header.getKey())) {
                httpHeaders.add(header.getKey(), header.getValue().toString());
            }
        }
        
        Object payload = requestMessage.getPayload();
        if (httpHeaders.getContentType() == null) {
            httpHeaders.setContentType(MediaType.parseMediaType(contentType.contains("charset") ? contentType : contentType + ";charset=" + charset));
        }
        
        if (HttpMethod.POST.equals(requestMethod) || HttpMethod.PUT.equals(requestMethod)) {
            return new HttpEntity<Object>(payload, httpHeaders);
        }
        
        return new HttpEntity<Object>(httpHeaders);
    }
    
    /**
     * Handles error response messages constructing a proper response message
     * which will be propagated to the respective reply message handler for 
     * further processing.
     */
    private class InternalResponseErrorHandler implements ResponseErrorHandler {

        /** Request message associated with this response error handler */
        private Message<?> requestMessage;
        
        /**
         * Default constructor provided with request message 
         * associated with this error handler.
         */
        public InternalResponseErrorHandler(Message<?> requestMessage) {
            this.requestMessage = requestMessage;
        }
        
        /**
         * Check for error HTTP status code in response message. 
         * Delegates to default Spring implementation.
         */
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return new DefaultResponseErrorHandler().hasError(response);
        }

        /**
         * Handle error response message according to error strategy.
         */
        public void handleError(ClientHttpResponse response) throws IOException {
            if (errorHandlingStrategy.equals(ErrorHandlingStrategy.PROPAGATE)) {
                informReplyMessageHandler(buildResponseMessage(response.getHeaders(), 
                                                           response.getBody() != null ? response.getBody() : "", 
                                                           response.getStatusCode()), requestMessage);
            } else if (errorHandlingStrategy.equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
                new DefaultResponseErrorHandler().handleError(response);
            } else {
                throw new CitrusRuntimeException("Unsupported error strategy: " + errorHandlingStrategy);
            }
        }

    }
    
    /**
     * Get the complete request URL.
     * @return the urlPath
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Set the complete request URL.
     * @param url the url to set
     */
    public void setRequestUrl(String url) {
        this.requestUrl = url;
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the endpoint uri resolver.
     * @param endpointUriResolver the endpointUriResolver to set
     */
    public void setEndpointUriResolver(EndpointUriResolver endpointUriResolver) {
        this.endpointUriResolver = endpointUriResolver;
    }

    /**
     * Sets the restTemplate.
     * @param restTemplate the restTemplate to set
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the requestMethod.
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Sets the charset.
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Sets the headerMapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(HeaderMapper<HttpHeaders> headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the contentType.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the errorHandlingStrategy.
     * @return the errorHandlingStrategy
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return errorHandlingStrategy;
    }

    /**
     * Sets the errorHandlingStrategy.
     * @param errorHandlingStrategy the errorHandlingStrategy to set
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
    }

    /**
     * Gets the requestMethod.
     * @return the requestMethod
     */
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * Gets the charset.
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Gets the contentType.
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the replyMessageHandler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the restTemplate.
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Gets the endpointUriResolver.
     * @return the endpointUriResolver
     */
    public EndpointUriResolver getEndpointUriResolver() {
        return endpointUriResolver;
    }

    /**
     * Gets the headerMapper.
     * @return the headerMapper
     */
    public HeaderMapper<HttpHeaders> getHeaderMapper() {
        return headerMapper;
    }

}
