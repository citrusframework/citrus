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

import com.consol.citrus.http.controller.HttpMessageController;
import com.consol.citrus.message.RawMessage;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Logging interceptor called by Spring MVC for each controller handling a RESTful Http request 
 * as a server.
 * 
 * Interceptor is capable of informing message tracing test listener on the request and response
 * messages arriving and leaving Citrus.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class LoggingHandlerInterceptor implements HandlerInterceptor {
    
    /** New line characters in log files */
    private static final String NEWLINE = System.getProperty("line.separator");

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(LoggingHandlerInterceptor.class);
    
    @Autowired(required = false)
    private MessageListeners messageListener;

    /**
     * {@inheritDoc}
     */
    public boolean preHandle(HttpServletRequest request, 
            HttpServletResponse response, Object handler) throws Exception {
        handleRequest(getRequestContent(request));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        handleResponse(getResponseContent(response, handler));
    }

    /**
     * {@inheritDoc}
     */
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
    
    /**
     * Handle request message and write request to logger.
     * @param request
     */
    public void handleRequest(String request) {
        if (messageListener != null) {
            log.info("Received Http request");
            messageListener.onInboundMessage(new RawMessage(request), null);
        } else {
            log.info("Received Http request:" + NEWLINE + request);
        }
    }
    
    /**
     * Handle response message and write content to logger.
     * @param response
     */
    public void handleResponse(String response) {
        if (messageListener != null) {
            log.info("Sending Http response");
            messageListener.onOutboundMessage(new RawMessage(response), null);
        } else {
            log.info("Sending Http response:" + NEWLINE + response);
        }
    }
    
    /**
     * Builds raw request message content from Http servlet request.
     * @param request
     * @return
     * @throws IOException 
     */
    private String getRequestContent(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        builder.append(request.getProtocol());
        builder.append(" ");
        builder.append(request.getMethod());
        builder.append(" ");
        builder.append(request.getRequestURI());
        builder.append(NEWLINE);
        
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement().toString();
            
            builder.append(headerName);
            builder.append(":");
            
            Enumeration<?> headerValues = request.getHeaders(headerName);
            if (headerValues.hasMoreElements()) {
                builder.append(headerValues.nextElement());
            }
            
            while (headerValues.hasMoreElements()) {
                builder.append(",");
                builder.append(headerValues.nextElement());
            }
            
            builder.append(NEWLINE);
        }
        
        builder.append(NEWLINE);
        builder.append(FileUtils.readToString(request.getInputStream()));
        
        return builder.toString();
    }
    
    /**
     * @param response
     * @return
     */
    private String getResponseContent(HttpServletResponse response, Object handler) {
        StringBuilder builder = new StringBuilder();

        builder.append(response);

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getBean() instanceof HttpMessageController) {
                ResponseEntity<String> responseEntity =
                        ((HttpMessageController) handlerMethod.getBean()).getResponseCache();
                if (responseEntity != null) {
                    builder.append(NEWLINE);
                    builder.append(responseEntity.getBody());
                }
            }
        }

        return builder.toString();
    }

}
