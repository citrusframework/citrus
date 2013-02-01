package com.consol.citrus.admin.servlet;

import com.consol.citrus.admin.websocket.LoggingWebSocket2;
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
public class LoggingWebSocketServlet2 extends WebSocketServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocketServlet2.class);

    private LoggingWebSocket2 loggingWebSocket2;

    @Override
    public void init() throws ServletException {
        super.init();
        loggingWebSocket2 = getLoggingWebSocket();
    }

    /**
     * {@inheritDoc}
     */
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        LOG.info("Accepted a new connection");
        return loggingWebSocket2;
    }

    private LoggingWebSocket2 getLoggingWebSocket() {
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        return springContext.getBean(LoggingWebSocket2.class);
    }


}
