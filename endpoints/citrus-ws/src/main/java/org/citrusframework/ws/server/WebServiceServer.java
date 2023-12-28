/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.ws.server;

import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.context.ParentDelegatingWebApplicationContext;
import org.citrusframework.ws.interceptor.LoggingEndpointInterceptor;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.servlet.CitrusMessageDispatcherServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.ServletMapping;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.util.Arrays;

import static java.nio.file.Paths.get;

/**
 * Jetty server implementation wrapping a {@link Server} with Citrus server behaviour, so
 * server can be started/stopped by Citrus.
 *
 * @author Christoph Deppisch
 */
public class WebServiceServer extends AbstractServer {

    /**
     * Server port
     */
    private int port = 8080;

    /**
     * Server resource base
     */
    private String resourceBase = "src/main/resources";

    /**
     * Application context location for payload mappings etc.
     */
    private String contextConfigLocation = "classpath:org/citrusframework/ws/citrus-servlet-context.xml";

    /**
     * Server instance to be wrapped
     */
    private Server jettyServer;

    /**
     * Use root application context as parent to build WebApplicationContext
     */
    private boolean useRootContextAsParent = false;

    /**
     * Do only start one instance after another so we need a static lock object
     */
    private static Object serverLock = new Object();

    /**
     * Set custom connector with custom idle time and other configuration options
     */
    private Connector connector;

    /**
     * Set list of custom connectors with custom configuration options
     */
    private Connector[] connectors;

    /**
     * Servlet mapping path
     */
    private String servletMappingPath = "/*";

    /**
     * Optional servlet name customization
     */
    private String servletName;

    /**
     * Context path
     */
    private String contextPath = "/";

    /**
     * Optional security handler for basic auth
     */
    private SecurityHandler securityHandler;

    /**
     * Optional servlet handler customization
     */
    private ServletHandler servletHandler;

    /**
     * Should handle Http mime headers
     */
    private boolean handleMimeHeaders = false;

    /**
     * Should handle Http attribute headers
     */
    private boolean handleAttributeHeaders = false;

    /**
     * Should keep soap envelope when creating internal message
     */
    private boolean keepSoapEnvelope = false;

    /**
     * Message converter implementation
     */
    private WebServiceMessageConverter messageConverter = new SoapMessageConverter();

    /**
     * Web service message factory bean name
     */
    private String messageFactoryName = MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME;

    /**
     * Default SOAP header namespace and prefix
     */
    private String soapHeaderNamespace;
    private String soapHeaderPrefix = "";

    @Override
    protected void shutdown() {
        if (jettyServer != null) {
            try {
                synchronized (serverLock) {
                    jettyServer.stop();
                }
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @Override
    protected void startup() {
        synchronized (serverLock) {
            if (connectors != null && connectors.length > 0) {
                jettyServer = connectors[0].getServer();
                jettyServer.setConnectors(connectors);
            } else if (connector != null) {
                jettyServer = connector.getServer();
                jettyServer.addConnector(connector);
            } else {
                jettyServer = new Server(port);
            }

            final Handler.Sequence handlers = new Handler.Sequence();

            ContextHandlerCollection contextCollection = new ContextHandlerCollection();

            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setBaseResourceAsPath(get(resourceBase));

            //add the root application context as parent to the constructed WebApplicationContext
            if (useRootContextAsParent && getReferenceResolver() instanceof SpringBeanReferenceResolver springBeanReferenceResolver) {
                contextHandler.setAttribute(
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                        new ParentDelegatingWebApplicationContext(springBeanReferenceResolver.getApplicationContext())
                );
            }

            if (servletHandler == null) {
                servletHandler = new ServletHandler();
                addDispatcherServlet();
            }

            contextHandler.setServletHandler(servletHandler);

            if (securityHandler != null) {
                contextHandler.setSecurityHandler(securityHandler);
            }

            configure(contextHandler);
            contextCollection.addHandler(contextHandler);

            handlers.addHandler(contextCollection);

            handlers.addHandler(new DefaultHandler());

            jettyServer.setHandler(handlers);

            try {
                jettyServer.start();
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Subclasses may add additional configuration on context handler.
     *
     * @param contextHandler
     */
    protected void configure(ServletContextHandler contextHandler) {
    }

    @Override
    public void initialize() {
        super.initialize();

        if (getReferenceResolver() != null && getReferenceResolver().resolveAll(MessageListeners.class).size() == 1) {
            MessageListeners messageListeners = getReferenceResolver().resolve(MessageListeners.class);

            getInterceptors().stream()
                    .filter(LoggingEndpointInterceptor.class::isInstance)
                    .map(LoggingEndpointInterceptor.class::cast)
                    .filter(interceptor -> !interceptor.hasMessageListeners())
                    .forEach(interceptor -> interceptor.setMessageListener(messageListeners));
        }
    }

    /**
     * Adds Citrus message dispatcher servlet.
     */
    private void addDispatcherServlet() {
        ServletHolder servletHolder = new ServletHolder(new CitrusMessageDispatcherServlet(this));
        servletHolder.setName(getServletName());
        servletHolder.setInitParameter("contextConfigLocation", contextConfigLocation);

        servletHandler.addServlet(servletHolder);

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName(getServletName());
        servletMapping.setPathSpec(servletMappingPath);

        servletHandler.addServletMapping(servletMapping);
    }

    /**
     * Gets the customized servlet name or default name if not set.
     *
     * @return the servletName
     */
    public String getServletName() {
        if (StringUtils.hasText(servletName)) {
            return servletName;
        } else {
            return getName() + "-servlet";
        }
    }

    /**
     * Gets the port.
     *
     * @return the port the port to get.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the resourceBase.
     *
     * @return the resourceBase the resourceBase to get.
     */
    public String getResourceBase() {
        return resourceBase;
    }

    /**
     * Sets the resourceBase.
     *
     * @param resourceBase the resourceBase to set
     */
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    /**
     * Gets the contextConfigLocation.
     *
     * @return the contextConfigLocation the contextConfigLocation to get.
     */
    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    /**
     * Sets the contextConfigLocation.
     *
     * @param contextConfigLocation the contextConfigLocation to set
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * Gets the connector.
     *
     * @return the connector the connector to get.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Sets the connector.
     *
     * @param connector the connector to set
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * Gets the connectors.
     *
     * @return the connectors
     */
    public Connector[] getConnectors() {
        if (connectors != null) {
            return Arrays.copyOf(connectors, connectors.length);
        } else {
            return new Connector[]{};
        }
    }

    /**
     * Sets the connectors.
     *
     * @param connectors the connectors to set
     */
    public void setConnectors(Connector[] connectors) {
        this.connectors = Arrays.copyOf(connectors, connectors.length);
    }

    /**
     * Gets the servletMappingPath.
     *
     * @return the servletMappingPath the servletMappingPath to get.
     */
    public String getServletMappingPath() {
        return servletMappingPath;
    }

    /**
     * Sets the servletMappingPath.
     *
     * @param servletMappingPath the servletMappingPath to set
     */
    public void setServletMappingPath(String servletMappingPath) {
        this.servletMappingPath = servletMappingPath;
    }

    /**
     * Gets the contextPath.
     *
     * @return the contextPath the contextPath to get.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath.
     *
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Gets the securityHandler.
     *
     * @return the securityHandler the securityHandler to get.
     */
    public SecurityHandler getSecurityHandler() {
        return securityHandler;
    }

    /**
     * Sets the securityHandler.
     *
     * @param securityHandler the securityHandler to set
     */
    public void setSecurityHandler(SecurityHandler securityHandler) {
        this.securityHandler = securityHandler;
    }

    /**
     * Gets the servletHandler.
     *
     * @return the servletHandler to get.
     */
    public ServletHandler getServletHandler() {
        return servletHandler;
    }

    /**
     * Sets the servletHandler.
     *
     * @param servletHandler the servletHandler to set
     */
    public void setServletHandler(ServletHandler servletHandler) {
        this.servletHandler = servletHandler;
    }

    /**
     * Sets the servletName.
     *
     * @param servletName the servletName to set
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * Gets the useRootContextAsParent.
     *
     * @return if to use the root context path as parent, or not.
     */
    public boolean isUseRootContextAsParent() {
        return useRootContextAsParent;
    }

    /**
     * Sets the useRootContextAsParent.
     *
     * @param useRootContextAsParent the useRootContextAsParent to set
     */
    public void setUseRootContextAsParent(boolean useRootContextAsParent) {
        this.useRootContextAsParent = useRootContextAsParent;
    }

    /**
     * Should handle mime headers.
     *
     * @return
     */
    public boolean isHandleMimeHeaders() {
        return handleMimeHeaders;
    }

    /**
     * Enable mime headers in request message which is passed to endpoint adapter.
     *
     * @param handleMimeHeaders the handleMimeHeaders to set
     */
    public void setHandleMimeHeaders(boolean handleMimeHeaders) {
        this.handleMimeHeaders = handleMimeHeaders;
    }

    /**
     * Gets the handleAttributeHeaders.
     *
     * @return
     */
    public boolean isHandleAttributeHeaders() {
        return handleAttributeHeaders;
    }

    /**
     * Sets the handleAttributeHeaders.
     *
     * @param handleAttributeHeaders
     */
    public void setHandleAttributeHeaders(boolean handleAttributeHeaders) {
        this.handleAttributeHeaders = handleAttributeHeaders;
    }

    /**
     * Gets the keep soap envelope flag.
     *
     * @return
     */
    public boolean isKeepSoapEnvelope() {
        return keepSoapEnvelope;
    }

    /**
     * Sets the keep soap header flag.
     *
     * @param keepSoapEnvelope
     */
    public void setKeepSoapEnvelope(boolean keepSoapEnvelope) {
        this.keepSoapEnvelope = keepSoapEnvelope;
    }

    /**
     * Gets the default soap header namespace.
     *
     * @return
     */
    public String getSoapHeaderNamespace() {
        return soapHeaderNamespace;
    }

    /**
     * Sets the default soap header namespace.
     *
     * @param soapHeaderNamespace
     */
    public void setSoapHeaderNamespace(String soapHeaderNamespace) {
        this.soapHeaderNamespace = soapHeaderNamespace;
    }

    /**
     * Gets the default soap header prefix.
     *
     * @return
     */
    public String getSoapHeaderPrefix() {
        return soapHeaderPrefix;
    }

    /**
     * Sets the default soap header prefix.
     *
     * @param soapHeaderPrefix
     */
    public void setSoapHeaderPrefix(String soapHeaderPrefix) {
        this.soapHeaderPrefix = soapHeaderPrefix;
    }

    /**
     * Gets the message converter.
     *
     * @return
     */
    public WebServiceMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     */
    public void setMessageConverter(WebServiceMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the message factory name.
     *
     * @return
     */
    public String getMessageFactoryName() {
        return messageFactoryName;
    }

    /**
     * Sets the message factory name.
     *
     * @param messageFactoryName
     */
    public void setMessageFactoryName(String messageFactoryName) {
        this.messageFactoryName = messageFactoryName;
    }
}
