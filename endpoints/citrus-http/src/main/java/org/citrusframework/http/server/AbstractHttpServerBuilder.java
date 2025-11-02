/*
 * Copyright the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class AbstractHttpServerBuilder<T extends HttpServer, B extends AbstractHttpServerBuilder<T, B>> extends AbstractServerBuilder<T, B> {

    /**
     * Endpoint target
     */
    private final T endpoint;

    private int securePort = 8443;
    private final List<String> connectors = new ArrayList<>();
    private final Map<String, String> filters = new LinkedHashMap<>();
    private String servletHandler;
    private String securityHandler;
    private String messageConverter;
    private final List<String> interceptors = new ArrayList<>();

    private String authentication;
    private String securedConnection;

    private final B self;

    protected AbstractHttpServerBuilder(T server) {
        this.endpoint = server;
        this.self = (B) this;
    }

    @Override
    public T build() {
        if (referenceResolver != null) {
            for (String connector : connectors) {
                connector(referenceResolver.resolve(connector, Connector.class));
            }

            if (StringUtils.hasText(servletHandler)) {
                servletHandler(referenceResolver.resolve(servletHandler, ServletHandler.class));
            }

            if (StringUtils.hasText(securityHandler)) {
                securityHandler(referenceResolver.resolve(securityHandler, SecurityHandler.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, HttpMessageConverter.class));
            }

            if (StringUtils.hasText(authentication)) {
                authentication(referenceResolver.resolve(authentication, HttpAuthentication.class));
            }

            if (StringUtils.hasText(securedConnection)) {
                secured(referenceResolver.resolve(messageConverter, HttpSecureConnection.class));
            }

            if (!filters.isEmpty()) {
                filters(this.filters.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> referenceResolver.resolve(entry.getValue(), Filter.class))));
            }

            if (!interceptors.isEmpty()) {
                interceptors(this.interceptors
                        .stream()
                        .map(entry -> referenceResolver.resolve(entry, HandlerInterceptor.class))
                        .toList());
            }
        }

        return super.build();
    }

    @Override
    protected T getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public B port(int port) {
        endpoint.setPort(port);
        return self;
    }

    @SchemaProperty(description = "The Http server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the secure port property.
     */
    public B securePort(int port) {
        this.securePort = port;
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The secured port.")
    public void setSecurePort(int port) {
        securePort(port);
    }

    /**
     * Sets the context config location.
     */
    public B contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the Spring context configuration loaded for this Http server.")
    public void setContextConfigLocation(String configLocation) {
        contextConfigLocation(configLocation);
    }

    /**
     * Sets the resource base.
     */
    public B resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return self;
    }

    @SchemaProperty(advanced = true, description = "The server resource base path where resources get loaded from.")
    public void setResourceBase(String resourceBase) {
        resourceBase(resourceBase);
    }

    /**
     * Enables/disables the root parent context.
     */
    public B rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server uses the root Spring application context.")
    public void setRootParentContext(boolean rootParentContext) {
        rootParentContext(rootParentContext);
    }

    /**
     * When enabled server uses default servlet filters such as request caching filter.
     */
    public B useDefaultFilters(boolean useDefaultFilters) {
        endpoint.setUseDefaultFilters(useDefaultFilters);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:filter") },
            description = "When enabled the server uses the default set of Http servlet filters.")
    public void setUseDefaultFilters(boolean useDefaultFilters) {
        useDefaultFilters(useDefaultFilters);
    }

    /**
     * Sets the connectors.
     */
    public B connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[0]));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Sets a list of connectors for this server.")
    public void setConnectors(List<String> connectors) {
        this.connectors.addAll(connectors);
    }

    /**
     * Sets the connector.
     */
    public B connector(Connector connector) {
        endpoint.setConnector(connector);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Add a connector to this server.")
    public void setConnector(String connector) {
        this.connectors.add(connector);
    }

    /**
     * Sets the filters.
     */
    public B filters(Map<String, Filter> filters) {
        endpoint.setFilters(filters);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:filter") },
            description = "Map of Http filters used on this server.")
    public void setFilters(Map<String, String> filters) {
        this.filters.putAll(filters);
    }

    /**
     * Sets the filterMappings.
     */
    public B filterMappings(Map<String, String> filterMappings) {
        endpoint.setFilterMappings(filterMappings);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:filter") },
            description = "Filter mapping used on this server.")
    public void setFilterMappings(Map<String, String> filterMappings) {
        filterMappings(filterMappings);
    }

    /**
     * Sets the binaryMediaTypes.
     */
    public B binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.setBinaryMediaTypes(binaryMediaTypes);
        return self;
    }

    @SchemaProperty(advanced = true, description = "List of supported media types on this server.")
    public void setBinaryMediaTypes(List<String> binaryMediaTypes) {
        binaryMediaTypes(binaryMediaTypes.stream().map(MediaType::valueOf).toList());
    }

    /**
     * Sets the servlet name.
     */
    public B servletName(String servletName) {
        endpoint.setServletName(servletName);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Sets the servlet name.")
    public void setServletName(String servletName) {
        servletName(servletName);
    }

    /**
     * Sets the servlet mapping path.
     */
    public B servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Sets the servlet mapping path.")
    public void setServletMappingPath(String servletMappingPath) {
        servletMappingPath(servletMappingPath);
    }

    /**
     * Sets the context path.
     */
    public B contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Sets the context path on this server.")
    public void setContextPath(String contextPath) {
        contextPath(contextPath);
    }

    /**
     * Sets the servlet handler.
     */
    public B servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Sets a custom servlet handler as a bean reference.")
    public void serServletHandler(String servletHandler) {
        this.servletHandler = servletHandler;
    }

    /**
     * Sets the security handler.
     */
    public B securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the security handler as a bean reference.")
    public void setSecurityHandler(String securityHandler) {
        this.securityHandler = securityHandler;
    }

    /**
     * Sets the message converter.
     */
    public B messageConverter(HttpMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the Http message converter bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the handleAttributeHeaders property.
     */
    public B handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles attribute headers.")
    public void setHandleAttributeHeaders(boolean flag) {
        handleAttributeHeaders(flag);
    }

    /**
     * Sets the handleCookies property.
     */
    public B handleCookies(boolean flag) {
        endpoint.setHandleCookies(flag);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles cookies.")
    public void setHandleCookies(boolean flag) {
        handleCookies(flag);
    }

    /**
     * Sets the semicolon handling property.
     */
    public B removeSemicolonPathContent(boolean removeSemicolonPathContent) {
        endpoint.setRemoveSemicolonPathContent(removeSemicolonPathContent);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server removes semicolon path content from headers.")
    public void setRemoveSemicolonPathContent(boolean removeSemicolonPathContent) {
        removeSemicolonPathContent(removeSemicolonPathContent);
    }

    /**
     * Sets the default status code property.
     */
    public B defaultStatus(HttpStatus status) {
        endpoint.setDefaultStatusCode(status.value());
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the default status returned by the server.")
    public void setDefaultStatus(HttpStatus status) {
        defaultStatus(status);
    }

    /**
     * Sets the default response cache size on this server instance.
     */
    public B responseCacheSize(int size) {
        endpoint.setResponseCacheSize(size);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the response cache size.")
    public void setResponseCacheSize(int size) {
        responseCacheSize(size);
    }

    /**
     * Sets the interceptors.
     */
    public B interceptors(List<HandlerInterceptor> interceptors) {
        endpoint.setInterceptors(Arrays.asList(interceptors.toArray()));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets the list of handler interceptor bean references.")
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    /**
     * Sets a single handler interceptor.
     */
    public B interceptor(HandlerInterceptor interceptor) {
        endpoint.setInterceptors(Collections.singletonList(interceptor));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets a handler interceptor.")
    public void setInterceptor(String interceptor) {
        this.interceptors.add(interceptor);
    }

    @Override
    public B timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return self;
    }

    @SchemaProperty(description = "Sets the server timeout while waiting for incoming requests.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    public B authentication(HttpAuthentication auth) {
        endpoint.setSecurityHandler(auth.getSecurityHandler("/*"));
        return self;
    }

    public B authentication(String resourcePath, HttpAuthentication auth) {
        endpoint.setSecurityHandler(auth.getSecurityHandler(resourcePath));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Use given authentication mechanism for all request paths.")
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public B secured(HttpSecureConnection conn) {
        return secured(securePort, conn);
    }

    public B secured(int securePort, HttpSecureConnection conn) {
        this.securePort = securePort;
        endpoint.setConnector(conn.getServerConnector(securePort));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Secure the connections on given secured port with given security mechanism.")
    public void setSecuredConnection(String connection) {
        this.securedConnection = connection;
    }
}
