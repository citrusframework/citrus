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

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.Message;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Message controller implementation handling all incoming requests by forwarding to a message 
 * handler for further processing.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/*")
public class HttpMessageController {

    /** Endpoint adapter for incoming requests, providing proper responses */
    private EndpointAdapter endpointAdapter = new EmptyResponseEndpointAdapter();

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

    @RequestMapping(value= "**", method = { RequestMethod.PATCH })
    @ResponseBody
    public ResponseEntity<String> handlePatchRequest(HttpEntity<String> requestEntity) {
        return handleRequestInternal(HttpMethod.PATCH, requestEntity);
    }
    
    /**
     * Handles requests with endpoint adapter implementation. Previously sets Http request method as header parameter.
     * @param method
     * @param requestEntity
     * @return
     */
    private ResponseEntity<String> handleRequestInternal(HttpMethod method, HttpEntity<String> requestEntity) {
        HttpMessage request = endpointConfiguration.getMessageConverter().convertInbound(requestEntity, endpointConfiguration, null);

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        UrlPathHelper pathHelper = new UrlPathHelper();

        Enumeration allHeaders = servletRequest.getHeaderNames();
        for (String headerName : CollectionUtils.toArray(allHeaders, new String[] {})) {
            if (request.getHeader(headerName) == null) {
                String headerValue = servletRequest.getHeader(headerName);
                request.header(headerName, headerValue != null ? headerValue : "");
            }
        }

        String queryParams = pathHelper.getOriginatingQueryString(servletRequest);

        if (queryParams == null) {
            queryParams = "";
        } else {
            queryParams = queryParams.replaceAll("&", ",");
        }

        request.path(pathHelper.getRequestUri(servletRequest))
                .uri(pathHelper.getRequestUri(servletRequest))
                .contextPath(pathHelper.getContextPath(servletRequest))
                .queryParams(queryParams)
                .version(servletRequest.getProtocol())
                .method(method);

        Message response = endpointAdapter.handleMessage(request);
        if (response == null) {
            responseCache = new ResponseEntity(HttpStatus.OK);
        } else {
            HttpMessage httpResponse;
            if (response instanceof HttpMessage) {
                httpResponse = (HttpMessage) response;
            } else {
                httpResponse = new HttpMessage(response);
            }

            if (httpResponse.getStatusCode() == null) {
                httpResponse.status(HttpStatus.OK);
            }

            responseCache = (ResponseEntity) endpointConfiguration.getMessageConverter().convertOutbound(httpResponse, endpointConfiguration, null);
        }

        return responseCache;
    }
    
    /**
     * Sets the endpointAdapter.
     * @param endpointAdapter the endpointAdapter to set
     */
    public void setEndpointAdapter(EndpointAdapter endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
    }

    /**
     * Gets the endpoint adapter.
     * @return
     */
    public EndpointAdapter getEndpointAdapter() {
        return endpointAdapter;
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
