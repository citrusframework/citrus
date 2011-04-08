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

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.integration.Message;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    
    /** Request factory */
    private ClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpMessageSender.class);
    
    /**
     * Default constructor.
     */
    public HttpMessageSender() {
        Assert.isTrue(requestFactory != null, "Neither a 'restTemplate' nor 'requestFactory' was set correctly.");
        
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
    }
    
    /**
     * Constructor using fields.
     * @param restTemplate
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
        ResponseEntity<?> response = restTemplate.exchange(getRequestUrl(), requestMethod, requestEntity, String.class);
        
        log.info("HTTP message was successfully sent to endpoint: '" + endpointUri + "'");
        
        Map<String, ?> httpResponseHeaders = headerMapper.toHeaders(response.getHeaders());
        
        Map<String, String> customHeaders = new HashMap<String, String>();
        for (Entry<String, List<String>> header : response.getHeaders().entrySet()) {
            if (!httpResponseHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(header.getValue()));
            }
        }
        
        Message<?> responseMessage = MessageBuilder.withPayload(response.getBody())
                                                    .copyHeaders(httpResponseHeaders)
                                                    .copyHeaders(customHeaders)
                                                    .setHeader(CitrusHttpMessageHeaders.HTTP_STATUS_CODE, response.getStatusCode())
                                                    .setHeader(CitrusHttpMessageHeaders.HTTP_VERSION, "HTTP/1.1") //TODO check if we have access to version information
                                                    .setHeader(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE, response.getStatusCode().name())
                                                    .build();
        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage,
                        correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
        
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
     * Sets the requestFactory.
     * @param requestFactory the requestFactory to set
     */
    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    /**
     * Sets the contentType.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
