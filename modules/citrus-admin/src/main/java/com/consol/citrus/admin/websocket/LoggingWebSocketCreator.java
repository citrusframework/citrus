package com.consol.citrus.admin.websocket;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Martin.Maher@consol.de on 19/06/15.
 */
public class LoggingWebSocketCreator implements WebSocketCreator {

    @Autowired
    private LoggingWebSocket loggingWebSocket;

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        return loggingWebSocket;
    }

}
