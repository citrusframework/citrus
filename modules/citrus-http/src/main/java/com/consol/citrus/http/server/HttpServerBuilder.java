/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.http.server;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.http.message.HttpMessageConverter;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.Filter;
import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpServerBuilder extends AbstractEndpointBuilder<HttpServer> {

    /** Endpoint target */
    private HttpServer endpoint = new HttpServer();

    @Override
    protected HttpServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public HttpServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public HttpServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    /**
     * Sets the context config location.
     * @param configLocation
     * @return
     */
    public HttpServerBuilder contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return this;
    }

    /**
     * Sets the resource base.
     * @param resourceBase
     * @return
     */
    public HttpServerBuilder resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return this;
    }

    /**
     * Enables/disables the root parent context.
     * @param rootParentContext
     * @return
     */
    public HttpServerBuilder rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return this;
    }

    /**
     * Sets the connectors.
     * @param connectors
     * @return
     */
    public HttpServerBuilder connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[connectors.size()]));
        return this;
    }

    /**
     * Sets the connector.
     * @param connector
     * @return
     */
    public HttpServerBuilder connector(Connector connector) {
        endpoint.setConnector(connector);
        return this;
    }

    /**
     * Sets the filters.
     * @param filters
     * @return
     */
    public HttpServerBuilder filters(Map<String, Filter> filters) {
        endpoint.setFilters(filters);
        return this;
    }

    /**
     * Sets the filterMappings.
     * @param filterMappings
     * @return
     */
    public HttpServerBuilder filterMappings(Map<String, String> filterMappings) {
        endpoint.setFilterMappings(filterMappings);
        return this;
    }

    /**
     * Sets the binaryMediaTypes.
     * @param binaryMediaTypes
     * @return
     */
    public HttpServerBuilder binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.setBinaryMediaTypes(binaryMediaTypes);
        return this;
    }

    /**
     * Sets the servlet name.
     * @param servletName
     * @return
     */
    public HttpServerBuilder servletName(String servletName) {
        endpoint.setServletName(servletName);
        return this;
    }

    /**
     * Sets the servlet mapping path.
     * @param servletMappingPath
     * @return
     */
    public HttpServerBuilder servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return this;
    }

    /**
     * Sets the context path.
     * @param contextPath
     * @return
     */
    public HttpServerBuilder contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return this;
    }

    /**
     * Sets the servlet handler.
     * @param servletHandler
     * @return
     */
    public HttpServerBuilder servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return this;
    }

    /**
     * Sets the security handler.
     * @param securityHandler
     * @return
     */
    public HttpServerBuilder securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public HttpServerBuilder messageConverter(HttpMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the handleAttributeHeaders property.
     * @param flag
     * @return
     */
    public HttpServerBuilder handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return this;
    }

    /**
     * Sets the handleCookies property.
     * @param flag
     * @return
     */
    public HttpServerBuilder handleCookies(boolean flag) {
        endpoint.setHandleCookies(flag);
        return this;
    }

    /**
     * Sets the default status code property.
     * @param status
     * @return
     */
    public HttpServerBuilder defaultStatus(HttpStatus status) {
        endpoint.setDefaultStatusCode(status.value());
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public HttpServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public HttpServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public HttpServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }

    /**
     * Sets the interceptors.
     * @param interceptors
     * @return
     */
    public HttpServerBuilder interceptors(List<HandlerInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return this;
    }
}
