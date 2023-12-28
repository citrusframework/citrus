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

package org.citrusframework.http.server;

import jakarta.servlet.Filter;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.context.ParentDelegatingWebApplicationContext;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.servlet.CitrusDispatcherServlet;
import org.citrusframework.http.servlet.GzipServletFilter;
import org.citrusframework.http.servlet.RequestCachingServletFilter;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.server.AbstractServer;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.FilterMapping;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static org.citrusframework.util.StringUtils.hasText;
import static org.springframework.http.MediaType.valueOf;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Simple Http server implementation starting an embedded Jetty server instance with
 * Spring Application context support. Incoming requests are handled with Spring MVC.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class HttpServer extends AbstractServer {
    /**
     * Server port
     */
    private int port = 8080;

    /**
     * Server resource base
     */
    private String resourceBase = "src/main/resources";

    /**
     * Application context location for request controllers
     */
    private String contextConfigLocation = "classpath:org/citrusframework/http/citrus-servlet-context.xml";

    /**
     * Server instance to be wrapped
     */
    private Server jettyServer;

    /**
     * Use root application context as parent to build WebApplicationContext
     */
    private boolean useRootContextAsParent = false;

    /**
     * Do only start one instance after another, so we need a static lock object
     */
    private static final Object serverLock = new Object();

    /**
     * Set custom connector with custom idle time and other configuration options
     */
    private Connector connector;

    /**
     * Set list of custom connectors with custom configuration options
     */
    private Connector[] connectors;

    /**
     * Set of custom servlet filters
     */
    private Map<String, Filter> filters = new HashMap<>();

    /**
     * Set of custom servlet filter mappings
     */
    private Map<String, String> filterMappings = new HashMap<>();

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
     * Should handle http attributes
     */
    private boolean handleAttributeHeaders = false;

    /**
     * Should handle http cookies
     */
    private boolean handleCookies = false;

    /**
     * Default status code returned by http server
     */
    private int defaultStatusCode = HttpStatus.OK.value();

    /**
     * Default size of in memory response cahce for message tracing reasons
     */
    private int responseCacheSize = HttpServerSettings.responseCacheSize();

    /**
     * List of media types that should be handled with binary content processing
     */
    private List<MediaType> binaryMediaTypes = asList(
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            MediaType.IMAGE_GIF,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            valueOf("application/zip")
    );

    /**
     * Message converter
     */
    private HttpMessageConverter messageConverter = new HttpMessageConverter();

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

            for (Map.Entry<String, Filter> filterEntry : filters.entrySet()) {
                String filterMappingPathSpec = filterMappings.get(filterEntry.getKey());
                FilterMapping filterMapping = new FilterMapping();
                filterMapping.setFilterName(filterEntry.getKey());
                filterMapping.setPathSpec(hasText(filterMappingPathSpec) ? filterMappingPathSpec : "/*");

                FilterHolder filterHolder = new FilterHolder();
                filterHolder.setName(filterEntry.getKey());
                filterHolder.setFilter(filterEntry.getValue());

                servletHandler.addFilter(filterHolder, filterMapping);
            }

            if (isEmpty(filters)) {
                addRequestCachingFilter();
                addGzipFilter();
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
                    .filter(LoggingHandlerInterceptor.class::isInstance)
                    .map(LoggingHandlerInterceptor.class::cast)
                    .filter(interceptor -> !interceptor.hasMessageListeners())
                    .forEach(interceptor -> interceptor.setMessageListener(messageListeners));
        }
    }

    /**
     * Adds default Spring dispatcher servlet with servlet mapping.
     */
    private void addDispatcherServlet() {
        ServletHolder servletHolder = new ServletHolder(getDispatcherServlet());
        servletHolder.setName(getServletName());
        servletHolder.setInitParameter("contextConfigLocation", contextConfigLocation);

        servletHandler.addServlet(servletHolder);

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName(getServletName());
        servletMapping.setPathSpec(servletMappingPath);

        servletHandler.addServletMapping(servletMapping);
    }

    /**
     * Gets the Citrus dispatcher servlet.
     *
     * @return
     */
    protected DispatcherServlet getDispatcherServlet() {
        return new CitrusDispatcherServlet(this);
    }

    /**
     * Adds request caching filter used for not using request data when
     * logging incoming requests
     */
    private void addRequestCachingFilter() {
        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setFilterName("request-caching-filter");
        filterMapping.setPathSpec("/*");

        FilterHolder filterHolder = new FilterHolder(new RequestCachingServletFilter());
        filterHolder.setName("request-caching-filter");
        servletHandler.addFilter(filterHolder, filterMapping);
    }

    /**
     * Adds gzip filter for automatic response messages compressing.
     */
    private void addGzipFilter() {
        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setFilterName("gzip-filter");
        filterMapping.setPathSpec("/*");

        FilterHolder filterHolder = new FilterHolder(new GzipServletFilter());
        filterHolder.setName("gzip-filter");
        servletHandler.addFilter(filterHolder, filterMapping);
    }

    /**
     * Gets the customized servlet name or default name if not set.
     *
     * @return the servletName
     */
    public String getServletName() {
        if (hasText(servletName)) {
            return servletName;
        } else {
            return getName() + "-servlet";
        }
    }

    /**
     * Gets the port.
     *
     * @return the port to get.
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
     * @return the resourceBase to get.
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
     * @return the contextConfigLocation to get.
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
     * @return the connector to get.
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
     * Sets the map of filters.
     *
     * @param filters
     */
    public void setFilters(Map<String, Filter> filters) {
        this.filters = filters;
    }

    /**
     * Gets the map of the filters.
     *
     * @return the filters
     */
    public Map<String, Filter> getFilters() {
        return filters;
    }

    /**
     * Sets the filterMappings property.
     *
     * @param filterMappings
     */
    public void setFilterMappings(Map<String, String> filterMappings) {
        this.filterMappings = filterMappings;
    }

    /**
     * Gets the value of the filterMappings property.
     *
     * @return the filterMappings
     */
    public Map<String, String> getFilterMappings() {
        return filterMappings;
    }

    /**
     * Gets the connectors.
     *
     * @return the connectors
     */
    public Connector[] getConnectors() {
        if (connectors != null) {
            return copyOf(connectors, connectors.length);
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
        this.connectors = copyOf(connectors, connectors.length);
    }

    /**
     * Gets the servletMappingPath.
     *
     * @return the servletMappingPath to get.
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
     * @return the contextPath to get.
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
     * @return the securityHandler to get.
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
     * @return the useRootContextAsParent to get.
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
     * Gets the message converter.
     *
     * @return
     */
    public HttpMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     */
    public void setMessageConverter(HttpMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
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
     * Gets the handleCookies.
     *
     * @return
     */
    public boolean isHandleCookies() {
        return handleCookies;
    }

    /**
     * Sets the handleCookies.
     *
     * @param handleCookies
     */
    public void setHandleCookies(boolean handleCookies) {
        this.handleCookies = handleCookies;
    }

    /**
     * Gets the response cache size.
     *
     * @return
     */
    public int getResponseCacheSize() {
        return responseCacheSize;
    }

    /**
     * Sets the response cache size.
     *
     * @param responseCacheSize
     */
    public void setResponseCacheSize(int responseCacheSize) {
        this.responseCacheSize = responseCacheSize;
    }

    /**
     * Gets the defaultStatusCode.
     *
     * @return
     */
    public int getDefaultStatusCode() {
        return defaultStatusCode;
    }

    /**
     * Sets the defaultStatusCode.
     *
     * @param defaultStatusCode
     */
    public void setDefaultStatusCode(int defaultStatusCode) {
        this.defaultStatusCode = defaultStatusCode;
    }

    /**
     * Gets the binaryMediaTypes.
     *
     * @return
     */
    public List<MediaType> getBinaryMediaTypes() {
        return binaryMediaTypes;
    }

    /**
     * Sets the binaryMediaTypes.
     *
     * @param binaryMediaTypes
     */
    public void setBinaryMediaTypes(List<MediaType> binaryMediaTypes) {
        this.binaryMediaTypes = binaryMediaTypes;
    }
}
