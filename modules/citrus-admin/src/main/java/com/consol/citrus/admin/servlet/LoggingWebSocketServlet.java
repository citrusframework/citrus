package com.consol.citrus.admin.servlet;

import com.consol.citrus.admin.websocket.LoggingWebSocket;
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
