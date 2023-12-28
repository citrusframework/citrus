/*
 * Copyright 2020-2024 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.http.server;

import jakarta.servlet.Filter;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.server.AbstractServerBuilder;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class AbstractHttpServerBuilder<T extends HttpServer, B extends AbstractHttpServerBuilder<T, B>> extends AbstractServerBuilder<T, B> {

    /**
     * Endpoint target
     */
    private final T endpoint;

    private int securePort = 8443;

    private final B self;

    protected AbstractHttpServerBuilder(T server) {
        this.endpoint = server;
        this.self = (B) this;
    }

    @Override
    protected T getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     *
     * @param port
     * @return
     */
    public B port(int port) {
        endpoint.setPort(port);
        return self;
    }

    /**
     * Sets the secure port property.
     *
     * @param port
     * @return
     */
    public B securePort(int port) {
        this.securePort = port;
        return self;
    }

    /**
     * Sets the context config location.
     *
     * @param configLocation
     * @return
     */
    public B contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return self;
    }

    /**
     * Sets the resource base.
     *
     * @param resourceBase
     * @return
     */
    public B resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return self;
    }

    /**
     * Enables/disables the root parent context.
     *
     * @param rootParentContext
     * @return
     */
    public B rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return self;
    }

    /**
     * Sets the connectors.
     *
     * @param connectors
     * @return
     */
    public B connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[connectors.size()]));
        return self;
    }

    /**
     * Sets the connector.
     *
     * @param connector
     * @return
     */
    public B connector(Connector connector) {
        endpoint.setConnector(connector);
        return self;
    }

    /**
     * Sets the filters.
     *
     * @param filters
     * @return
     */
    public B filters(Map<String, Filter> filters) {
        endpoint.setFilters(filters);
        return self;
    }

    /**
     * Sets the filterMappings.
     *
     * @param filterMappings
     * @return
     */
    public B filterMappings(Map<String, String> filterMappings) {
        endpoint.setFilterMappings(filterMappings);
        return self;
    }

    /**
     * Sets the binaryMediaTypes.
     *
     * @param binaryMediaTypes
     * @return
     */
    public B binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.setBinaryMediaTypes(binaryMediaTypes);
        return self;
    }

    /**
     * Sets the servlet name.
     *
     * @param servletName
     * @return
     */
    public B servletName(String servletName) {
        endpoint.setServletName(servletName);
        return self;
    }

    /**
     * Sets the servlet mapping path.
     *
     * @param servletMappingPath
     * @return
     */
    public B servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return self;
    }

    /**
     * Sets the context path.
     *
     * @param contextPath
     * @return
     */
    public B contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return self;
    }

    /**
     * Sets the servlet handler.
     *
     * @param servletHandler
     * @return
     */
    public B servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return self;
    }

    /**
     * Sets the security handler.
     *
     * @param securityHandler
     * @return
     */
    public B securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return self;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     * @return
     */
    public B messageConverter(HttpMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return self;
    }

    /**
     * Sets the handleAttributeHeaders property.
     *
     * @param flag
     * @return
     */
    public B handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return self;
    }

    /**
     * Sets the handleCookies property.
     *
     * @param flag
     * @return
     */
    public B handleCookies(boolean flag) {
        endpoint.setHandleCookies(flag);
        return self;
    }

    /**
     * Sets the default status code property.
     *
     * @param status
     * @return
     */
    public B defaultStatus(HttpStatus status) {
        endpoint.setDefaultStatusCode(status.value());
        return self;
    }

    /**
     * Sets the default response cache size on this server instance.
     *
     * @param size
     * @return
     */
    public B responseCacheSize(int size) {
        endpoint.setResponseCacheSize(size);
        return self;
    }

    /**
     * Sets the interceptors.
     *
     * @param interceptors
     * @return
     */
    public B interceptors(List<HandlerInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return self;
    }

    @Override
    public B timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return self;
    }

    public B authentication(HttpAuthentication auth) {
        endpoint.setSecurityHandler(auth.getSecurityHandler("/*"));
        return self;
    }

    public B authentication(String resourcePath, HttpAuthentication auth) {
        endpoint.setSecurityHandler(auth.getSecurityHandler(resourcePath));
        return self;
    }

    public B secured(HttpSecureConnection conn) {
        return secured(securePort, conn);
    }

    public B secured(int securePort, HttpSecureConnection conn) {
        this.securePort = securePort;
        endpoint.setConnector(conn.getServerConnector(securePort));
        return self;
    }
}
