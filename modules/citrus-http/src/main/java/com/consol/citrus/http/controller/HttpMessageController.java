/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.http.controller;

import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Message controller implementation handling all incoming requests by forwarding to a message 
 * handler for further processing.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/*")
public class HttpMessageController {

    /** Message handler for incoming requests, providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseEndpointAdapter();

    /** Endpoint configuration */
    private HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();

    /** Hold the latest response message for message tracing reasons */
    private ResponseEntity<String> responseCache;
    
    @RequestMapping(value = "**", method = { RequestMethod.GET })
    @ResponseBody
    public ResponseEntity<String> handleGetRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.GET, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.POST })
    @ResponseBody
    public ResponseEntity<String> handlePostRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.POST, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.PUT })
    @ResponseBody
    public ResponseEntity<String> handlePutRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.PUT, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.DELETE })
    @ResponseBody
    public ResponseEntity<String> handleDeleteRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.DELETE, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.OPTIONS })
    @ResponseBody
    public ResponseEntity<String> handleOptionsRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.OPTIONS, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.HEAD })
    @ResponseBody
    public ResponseEntity<String> handleHeadRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.HEAD, requestEntity);
    }
    
    @RequestMapping(value= "**", method = { RequestMethod.TRACE })
    @ResponseBody
    public ResponseEntity<String> handleTraceRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.TRACE, requestEntity);
    }
    
    /**
     * Handles requests with message handler implementation. Previously sets Http request method as header parameter.
     * @param method
     * @param requestEntity
     * @return
     */
    private ResponseEntity<String> handleRequestInternal(HttpMethod method, HttpEntity<String> requestEntity) {
        MessageBuilder requestBuilder = MessageBuilder.fromMessage(endpointConfiguration.getMessageConverter().convertInbound(requestEntity, endpointConfiguration));

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        UrlPathHelper pathHelper = new UrlPathHelper();

        requestBuilder.setHeader(CitrusHttpMessageHeaders.HTTP_REQUEST_URI, pathHelper.getRequestUri(servletRequest));
        requestBuilder.setHeader(CitrusHttpMessageHeaders.HTTP_CONTEXT_PATH, pathHelper.getContextPath(servletRequest));

        String queryParams = pathHelper.getOriginatingQueryString(servletRequest);
        requestBuilder.setHeader(CitrusHttpMessageHeaders.HTTP_QUERY_PARAMS, queryParams != null ? queryParams : "");

        requestBuilder.setHeader(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD, method.toString());

        Message<?> response = messageHandler.handleMessage(requestBuilder.build());

        if (response == null) {
            responseCache = new ResponseEntity<String>(HttpStatus.OK);
        } else {
            if (!response.getHeaders().containsKey(CitrusHttpMessageHeaders.HTTP_STATUS_CODE)) {
                response = MessageBuilder.fromMessage(response)
                        .setHeader(CitrusHttpMessageHeaders.HTTP_STATUS_CODE, HttpStatus.OK.value())
                        .build();
            }

            responseCache = (ResponseEntity<String>) endpointConfiguration.getMessageConverter().convertOutbound(response, endpointConfiguration);
        }

        return responseCache;
    }
    
    /**
     * Sets the messageHandler.
     * @param messageHandler the messageHandler to set
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Gets the message handler.
     * @return
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Gets the endpoint configuration.
     * @return
     */
    public HttpEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Sets the endpoint configuration.
     * @param endpointConfiguration
     */
    public void setEndpointConfiguration(HttpEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    /**
     * Gets the responseCache.
     * @return the responseCache the responseCache to get.
     */
    public ResponseEntity<String> getResponseCache() {
        return responseCache;
    }
}
