package com.consol.citrus.ws;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.servlet.*;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.AbstractServer;

public class JettyWebServer extends AbstractServer {

    private int port = 8080;
    
    private String resourceBase = "src/main/resources";
    
    private Server jettyServer;
    
    @Override
    protected void shutdown() {
        if(jettyServer != null) {
            try {
                jettyServer.stop();
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @Override
    protected void startup() {
        jettyServer = new Server(port);
        
        HandlerCollection handlers = new HandlerCollection();
        
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        
        Context context = new Context();
        context.setContextPath("/");
        context.setResourceBase(resourceBase);
        
        ServletHandler servletHandler = new ServletHandler();
        
        ServletHolder servletHolder = new ServletHolder(new MessageDispatcherServlet());
        servletHolder.setName("spring-ws");
        servletHolder.setInitParameter("contextConfigLocation", "classpath:spring-ws-servlet.xml");
        
        servletHandler.addServlet(servletHolder);
        
        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName("spring-ws");
        servletMapping.setPathSpec("/*");
        
        servletHandler.addServletMapping(servletMapping);
        
        context.setServletHandler(servletHandler);
        
        contexts.addHandler(context);
        
        handlers.addHandler(contexts);
        
        handlers.addHandler(new DefaultHandler());
        handlers.addHandler(new RequestLogHandler());
        
        jettyServer.setHandler(handlers);
    }

    public void run() {
        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * @param resourceBase the resourceBase to set
     */
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

}
