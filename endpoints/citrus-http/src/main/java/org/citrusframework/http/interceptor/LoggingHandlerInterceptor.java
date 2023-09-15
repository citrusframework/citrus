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

package org.citrusframework.http.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.http.controller.HttpMessageController;
import org.citrusframework.message.RawMessage;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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

    /** New line characters in logger files */
    private static final String NEWLINE = System.getProperty("line.separator");

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(LoggingHandlerInterceptor.class);

    private MessageListeners messageListener;

    private final TestContextFactory contextFactory = TestContextFactory.newInstance();

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        handleRequest(getRequestContent(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        handleResponse(getResponseContent(request, response, handler));
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    /**
     * Handle request message and write request to logger.
     * @param request
     */
    public void handleRequest(String request) {
        if (hasMessageListeners()) {
            logger.debug("Received Http request");
            messageListener.onInboundMessage(new RawMessage(request), contextFactory.getObject());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Received Http request:" + NEWLINE + request);
            }
        }
    }

    /**
     * Handle response message and write content to logger.
     * @param response
     */
    public void handleResponse(String response) {
        if (hasMessageListeners()) {
            logger.debug("Sending Http response");
            messageListener.onOutboundMessage(new RawMessage(response), contextFactory.getObject());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending Http response:" + NEWLINE + response);
            }
        }
    }

    /**
     * Checks if message listeners are present on this interceptor.
     * @return
     */
    public boolean hasMessageListeners() {
        return messageListener != null && !messageListener.isEmpty();
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
     * Retrieve response body content. In case given handler is an instance of the
     * default message controller the content is retrieved from the internal response cache.
     *
     * The servlet request is used as key to retrieve the response from that cache. This makes sure that
     * multiple logging interceptor handlers get the very same response content.
     *
     * @param request the servlet request
     * @param response the servlet response holding headers
     * @return the complete response data with headers and body content as String
     */
    private String getResponseContent(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StringBuilder builder = new StringBuilder();

        builder.append(response);

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getBean() instanceof HttpMessageController) {
                ResponseEntity<?> responseEntity = ((HttpMessageController) handlerMethod.getBean()).getResponseCache(request);
                if (responseEntity != null) {
                    builder.append(NEWLINE);
                    builder.append(responseEntity.getBody());
                }
            }
        }

        return builder.toString();
    }

    /**
     * Specifies the message listeners.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }
}
