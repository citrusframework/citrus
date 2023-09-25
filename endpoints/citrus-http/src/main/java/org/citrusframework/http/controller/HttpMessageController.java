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

package org.citrusframework.http.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServerSettings;
import org.citrusframework.message.Message;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

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

    /** Cache response messages for message tracing reasons */
    private final ConcurrentHashMap<HttpServletRequest, ResponseEntity<?>> responseCache = new ConcurrentHashMap<>();

    /** List of requests used to clear caches when too many requests are in memory */
    private final ConcurrentLinkedQueue<HttpServletRequest> activeRequests = new ConcurrentLinkedQueue<>();

    /** Maximum number of responses cached on this server for message tracing reasons */
    private int responseCacheSize = HttpServerSettings.responseCacheSize();

    @RequestMapping(value = "**", method = { RequestMethod.GET })
    @ResponseBody
    public ResponseEntity<?> handleGetRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.GET, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.POST })
    @ResponseBody
    public ResponseEntity<?> handlePostRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.POST, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.PUT })
    @ResponseBody
    public ResponseEntity<?> handlePutRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.PUT, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.DELETE })
    @ResponseBody
    public ResponseEntity<?> handleDeleteRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.DELETE, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.OPTIONS })
    @ResponseBody
    public ResponseEntity<?> handleOptionsRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.OPTIONS, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.HEAD })
    @ResponseBody
    public ResponseEntity<?> handleHeadRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.HEAD, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.TRACE })
    @ResponseBody
    public ResponseEntity<?> handleTraceRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.TRACE, requestEntity);
    }

    @RequestMapping(value= "**", method = { RequestMethod.PATCH })
    @ResponseBody
    public ResponseEntity<?> handlePatchRequest(HttpEntity<Object> requestEntity) {
        return handleRequestInternal(HttpMethod.PATCH, requestEntity);
    }

    /**
     * Handles requests with endpoint adapter implementation. Previously sets Http request method as header parameter.
     * @param method
     * @param requestEntity
     * @return
     */
    private ResponseEntity<?> handleRequestInternal(HttpMethod method, HttpEntity<?> requestEntity) {
        HttpMessage request = endpointConfiguration.getMessageConverter().convertInbound(requestEntity, endpointConfiguration, null);

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CitrusRuntimeException("Failed to retrieve servlet request");
        }

        HttpServletRequest servletRequest = ((ServletRequestAttributes) attributes).getRequest();
        UrlPathHelper pathHelper = new UrlPathHelper();

        Enumeration<String> allHeaders = servletRequest.getHeaderNames();
        for (String headerName : CollectionUtils.toArray(allHeaders, new String[] {})) {
            if (request.getHeader(headerName) == null) {
                String headerValue = servletRequest.getHeader(headerName);
                request.header(headerName, headerValue != null ? headerValue : "");
            }
        }

        if (endpointConfiguration.isHandleCookies()) {
            request.setCookies(servletRequest.getCookies());
        }

        if (endpointConfiguration.isHandleAttributeHeaders()) {
            Enumeration<String> attributeNames = servletRequest.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attribute = servletRequest.getAttribute(attributeName);
                request.setHeader(attributeName, attribute);
            }
        }

        request.path(pathHelper.getRequestUri(servletRequest))
                .uri(pathHelper.getRequestUri(servletRequest))
                .contextPath(pathHelper.getContextPath(servletRequest))
                .queryParams(Optional.ofNullable(pathHelper.getOriginatingQueryString(servletRequest))
                                    .map(queryString -> queryString.replaceAll("&", ","))
                                    .orElse(""))
                .version(servletRequest.getProtocol())
                .method(method);

        Message response = endpointAdapter.handleMessage(request);

        ResponseEntity<?> responseEntity;
        if (response == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.valueOf(endpointConfiguration.getDefaultStatusCode()));
        } else {
            HttpMessage httpResponse;
            if (response instanceof HttpMessage) {
                httpResponse = (HttpMessage) response;
            } else {
                httpResponse = new HttpMessage(response);
            }

            if (httpResponse.getStatusCode() == null) {
                httpResponse.status(HttpStatusCode.valueOf(endpointConfiguration.getDefaultStatusCode()));
            }

            responseEntity = (ResponseEntity<?>) endpointConfiguration.getMessageConverter().convertOutbound(httpResponse, endpointConfiguration, null);

            if (endpointConfiguration.isHandleCookies() && httpResponse.getCookies() != null) {
                HttpServletResponse servletResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
                if (servletResponse == null) {
                    throw new CitrusRuntimeException("Failed to retrieve servlet response");
                }

                for (Cookie cookie : httpResponse.getCookies()) {
                    servletResponse.addCookie(cookie);
                }
            }
        }
        responseCache.put(servletRequest, responseEntity);
        activeRequests.add(servletRequest);

        clearResponseCacheEntries(activeRequests, responseCache);

        return responseEntity;
    }

    /**
     * Clear cache when max size is reached. Removes the oldest entries according to list of the active requests.
     * @param activeRequests
     * @param responseCache
     */
    private void clearResponseCacheEntries(ConcurrentLinkedQueue<HttpServletRequest> activeRequests,
                                                  ConcurrentHashMap<HttpServletRequest, ResponseEntity<?>> responseCache) {
        while (activeRequests.size() >= responseCacheSize) {
            Optional.ofNullable(activeRequests.poll())
                    .ifPresent(responseCache::remove);
        }
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
    public ResponseEntity<?> getResponseCache(HttpServletRequest request) {
        return responseCache.get(request);
    }

    /**
     * Gets the response cache size.
     * @return
     */
    public int getResponseCacheSize() {
        return responseCacheSize;
    }

    /**
     * Sets the response cache size.
     * @param responseCacheSize
     */
    public void setResponseCacheSize(int responseCacheSize) {
        this.responseCacheSize = responseCacheSize;
    }
}
