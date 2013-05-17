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

package com.consol.citrus.admin.websocket;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Logging WebSocket
 *
 * @author Martin.Maher@consol.de
 * @since 2013.01.29
 */
public class LoggingWebSocketServlet extends WebSocketServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocketServlet.class);

    private LoggingWebSocket loggingWebSocket;

    @Override
    public void init() throws ServletException {
        super.init();
        loggingWebSocket = getLoggingWebSocket();
    }

    /**
     * {@inheritDoc}
     */
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        LOG.info("Accepted a new connection");
        return loggingWebSocket;
    }

    private LoggingWebSocket getLoggingWebSocket() {
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        return springContext.getBean(LoggingWebSocket.class);
    }


}
