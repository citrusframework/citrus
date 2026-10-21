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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import org.citrusframework.base.server.AbstractServerBuilder;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.eclipse.jetty.ee11.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.web.servlet.HandlerInterceptor;

public class AbstractHttpServerBuilder<T extends HttpServer, B extends AbstractHttpServerBuilder<T, B>> extends AbstractCommonHttpServerBuilder<T, B> {

    private final List<String> connectors = new ArrayList<>();
    private final Map<String, String> filters = new LinkedHashMap<>();
    private String servletHandler;
    private String securityHandler;
    private final List<String> interceptors = new ArrayList<>();

    @SuppressWarnings("unchecked")
    protected AbstractHttpServerBuilder(T server) {
        super(server);
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

    /**
     * Sets the context config location.
     *
     * @deprecated Only supported by the Jetty based server. The Vert.x based server does not
     * use a Spring servlet context.
     */
    @Deprecated(since = "5.1.0")
    @SuppressWarnings("deprecation")
    public B contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return self;
    }

    @Deprecated(since = "5.1.0")
    @SchemaProperty(advanced = true, description = "Sets the Spring context configuration loaded for this Http server.")
    @XmlAttribute(name = "context-config-location")
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
    @XmlAttribute(name = "resource-base")
    public void setResourceBase(String resourceBase) {
        resourceBase(resourceBase);
    }

    /**
     * Enables/disables the root parent context.
     *
     * @deprecated Only supported by the Jetty based server. The Vert.x based server does not
     * use a Spring servlet context.
     */
    @Deprecated(since = "5.1.0")
    @SuppressWarnings("deprecation")
    public B rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return self;
    }

    @Deprecated(since = "5.1.0")
    @SchemaProperty(advanced = true, description = "When enabled the server uses the root Spring application context.")
    @XmlAttribute(name = "root-parent-context")
    public void setRootParentContext(boolean rootParentContext) {
        rootParentContext(rootParentContext);
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
    @XmlTransient
    public void setConnectors(List<String> connectors) {
        this.connectors.addAll(connectors);
    }

    @XmlAttribute
    public void setConnectors(String connectors) {
        setConnectors(Arrays.asList(connectors.split(",")));
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
    @XmlAttribute
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
    @XmlTransient
    public void setFilters(Map<String, String> filters) {
        this.filters.putAll(filters);
    }

    @XmlAttribute
    public void setFilters(String filters) {
        setFilters(Arrays.stream(filters.split(","))
                .map(String::trim)
                .filter(expression -> expression.contains("="))
                .map(expression -> expression.split("=", 2))
                .collect(Collectors.toMap(tokens -> tokens[0].trim(), tokens -> tokens[1].trim())));
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
    @XmlTransient
    public void setFilterMappings(Map<String, String> filterMappings) {
        filterMappings(filterMappings);
    }

    @XmlAttribute
    public void setFilterMappings(String filterMappings) {
        setFilterMappings(Arrays.stream(filterMappings.split(","))
                .map(String::trim)
                .filter(expression -> expression.contains("="))
                .map(expression -> expression.split("=", 2))
                .collect(Collectors.toMap(tokens -> tokens[0].trim(), tokens -> tokens[1].trim())));
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
    @XmlAttribute(name = "servlet-name")
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
    @XmlAttribute(name = "servlet-mapping-path")
    public void setServletMappingPath(String servletMappingPath) {
        servletMappingPath(servletMappingPath);
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
    @XmlAttribute(name = "servlet-handler")
    public void setServletHandler(String servletHandler) {
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
    @XmlAttribute(name = "security-handler")
    public void setSecurityHandler(String securityHandler) {
        this.securityHandler = securityHandler;
    }

    /**
     * Sets the handleAttributeHeaders property.
     */
    public B handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles attribute headers.")
    @XmlAttribute(name = "handle-attribute-headers")
    public void setHandleAttributeHeaders(boolean flag) {
        handleAttributeHeaders(flag);
    }

    /**
     * Sets the default response cache size on this server instance.
     */
    public B responseCacheSize(int size) {
        endpoint.setResponseCacheSize(size);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the response cache size.")
    @XmlAttribute(name = "response-cache-size")
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
    @XmlTransient
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @XmlAttribute
    public void setInterceptors(String interceptors) {
        setInterceptors(Arrays.asList(interceptors.split(",")));
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
    @XmlAttribute
    public void setInterceptor(String interceptor) {
        this.interceptors.add(interceptor);
    }

    @Override
    @SuppressWarnings("deprecation")
    public B authentication(String resourcePath, HttpAuthentication auth) {
        super.authentication(resourcePath, auth);
        endpoint.setSecurityHandler(auth.getSecurityHandler(resourcePath));
        return self;
    }

    @Override
    @SuppressWarnings("deprecation")
    public B secured(int securePort, HttpSecureConnection conn) {
        super.secured(securePort, conn);
        endpoint.setConnector(conn.getServerConnector(securePort));
        return self;
    }
}
