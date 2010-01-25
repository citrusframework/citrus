/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.servlet.*;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.AbstractServer;

/**
 * Jetty server implementation wrapping a {@link Server} with Citrus server behaviour, so
 * server can be started/stopped by Citrus.
 * 
 * @author Christoph Deppisch
 */
public class JettyServer extends AbstractServer {

    /** Server port */
    private int port = 8080;
    
    /** Server resource base */
    private String resourceBase = "src/main/resources";
    
    /** Application context location for payload mappings etc. */
    private String contextConfigLocation = "classpath:citrus-ws-servlet.xml";
    
    /** Server instance to be wrapped */
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
        servletHolder.setInitParameter("contextConfigLocation", contextConfigLocation);
        
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

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Get the server port.
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the server port.
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Set the server resource base.
     * @param resourceBase the resourceBase to set
     */
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    /**
     * Set the context config location.
     * @param contextConfigLocation the contextConfigLocation to set
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }
}
