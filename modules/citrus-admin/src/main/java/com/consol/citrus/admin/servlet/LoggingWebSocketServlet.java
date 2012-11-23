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

package com.consol.citrus.admin.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.consol.citrus.admin.listener.TestEventListener;
import com.consol.citrus.admin.websocket.LoggingWebSocket;

/**
 * Web socket servlet accepts client requests opening a bidirectional TCP/IP connection. 
 * Once connection handshake is done server sends test action events to the client for logging purpose.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class LoggingWebSocketServlet extends WebSocketServlet {

    /** WebSocket for broadcasting logging events */
    private LoggingWebSocket socket = new LoggingWebSocket();
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        //set web socket as logging listener on test event provider coming from servlet context
        ((TestEventListener)getServletContext().getAttribute(TestEventListener.ATTRIBUTE)).addLoggingListener(socket);
    }
    
    /**
     * {@inheritDoc}
     */
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return socket;
    }

}
