/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.http.interceptor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.report.MessageTracingTestListener;

/**
 * Simple logging interceptor writes Http request and response messages to the console.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class LoggingClientInterceptor implements ClientHttpRequestInterceptor {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(LoggingClientInterceptor.class);
    
    @Autowired(required=false)
    private MessageTracingTestListener messageTracingTestListener;
    
    /**
     * {@inheritDoc}
     */
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
        ClientHttpRequestExecution execution) throws IOException {
        handleRequest(getRequestContent(request, new String(body)));
        
        ClientHttpResponse response = execution.execute(request, body);
        CachingClientHttpResponseWrapper bufferedResponse = new CachingClientHttpResponseWrapper(response);
        handleResponse(getResponseContent(bufferedResponse));

        return bufferedResponse;
    }

    /**
     * Handles request messages for logging.
     * @param request
     */
    public void handleRequest(String request) {
        if (log.isDebugEnabled()) {
            log.debug("Send Http request:\n" + request);
        }
        
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Send Http request:\n" + request);
        }
    }
    
    /**
     * Handles response messages for logging.
     * @param response
     */
    public void handleResponse(String response) {
        if (log.isDebugEnabled()) {
            log.debug("Received Http response:\n" + response);
        }
        
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Received Http response:\n" + response);
        }
    }
    
    /**
     * Builds request content string from request and body.
     * @param request
     * @param body
     * @return
     */
    private String getRequestContent(HttpRequest request, String body) {
        StringBuilder builder = new StringBuilder();
        
        builder.append(request.getMethod());
        builder.append(" ");
        builder.append(request.getURI());
        builder.append("\n");
        
        appendHeaders(request.getHeaders(), builder);
        
        builder.append("\n");
        builder.append(new String(body));
        
        return builder.toString(); 
    }
    
    /**
     * Builds response content string from response object.
     * @param response
     * @return
     * @throws IOException
     */
    private String getResponseContent(CachingClientHttpResponseWrapper response) throws IOException {
        if (response != null) {
            StringBuilder builder = new StringBuilder();
            
            builder.append("HTTP/1.1 ");
            builder.append(response.getStatusCode());
            builder.append(" ");
            builder.append(response.getStatusText());
            builder.append("\n");
            
            appendHeaders(response.getHeaders(), builder);
            
            builder.append("\n");
            builder.append(response.getBodyContent());
            
            return builder.toString();
        } else {
            return "";
        }
    }
    
    /**
     * Append Http headers to string builder.
     * @param headers
     * @param builder
     */
    private void appendHeaders(HttpHeaders headers, StringBuilder builder) {
        for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
            builder.append(headerEntry.getKey());
            builder.append(":");
            builder.append(StringUtils.arrayToCommaDelimitedString(headerEntry.getValue().toArray()));
            builder.append("\n");
        }
    }
    
    /**
     * Response wrapper implementation of {@link ClientHttpResponse} that reads the message body 
     * into memory for caching, thus allowing for multiple invocations of {@link #getBody()}.
     */
    private final static class CachingClientHttpResponseWrapper implements ClientHttpResponse {

        private final ClientHttpResponse response;

        private byte[] body;

        CachingClientHttpResponseWrapper(ClientHttpResponse response) {
            this.response = response;
        }

        public HttpStatus getStatusCode() throws IOException {
            return this.response.getStatusCode();
        }

        public int getRawStatusCode() throws IOException {
            return this.response.getRawStatusCode();
        }

        public String getStatusText() throws IOException {
            return this.response.getStatusText();
        }

        public HttpHeaders getHeaders() {
            return this.response.getHeaders();
        }

        public InputStream getBody() throws IOException {
            if (this.body == null) {
                if (response.getBody() != null) {
                    this.body = FileCopyUtils.copyToByteArray(response.getBody());
                } else {
                    body = new byte[] {};
                }
            }
            return new ByteArrayInputStream(this.body);
        }
        
        public String getBodyContent() throws IOException {
            if (this.body == null) {
                getBody();
            }
            
            return new String(body, Charset.forName("UTF-8"));
        }

        public void close() {
            this.response.close();
        }
    }

}
