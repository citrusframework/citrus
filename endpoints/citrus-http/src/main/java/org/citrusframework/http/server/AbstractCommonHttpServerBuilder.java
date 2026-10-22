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
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.base.server.AbstractServerBuilder;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Transport neutral base class for Http server builders. Holds all configuration options that are
 * independent of the underlying Http server technology.
 *
 * @param <T> the type of the Http server
 * @param <B> the type of the builder
 * @since 5.2
 */
public class AbstractCommonHttpServerBuilder<T extends AbstractHttpServer, B extends AbstractCommonHttpServerBuilder<T, B>> extends AbstractServerBuilder<T, B> {

    /**
     * Endpoint target
     */
    protected final T endpoint;

    /**
     * Secure port used for secured connections
     */
    private int securePort = 8443;

    /**
     * Message converter reference
     */
    private String messageConverter;

    /**
     * Authentication reference
     */
    private String authentication;

    /**
     * Secure connection reference
     */
    private String securedConnection;

    /**
     * Self reference for fluent API
     */
    protected final B self;

    @SuppressWarnings("unchecked")
    protected AbstractCommonHttpServerBuilder(T server) {
        this.endpoint = server;
        this.self = (B) this;
    }

    @Override
    public T build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, HttpMessageConverter.class));
            }

            if (StringUtils.hasText(authentication)) {
                authentication(referenceResolver.resolve(authentication, HttpAuthentication.class));
            }

            if (StringUtils.hasText(securedConnection)) {
                secured(referenceResolver.resolve(securedConnection, HttpSecureConnection.class));
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
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the secure port property.
     */
    public B securePort(int port) {
        this.securePort = port;
        endpoint.setSecurePort(port);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The secured port.")
    @XmlAttribute(name = "secure-port")
    public void setSecurePort(int port) {
        securePort(port);
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
    @XmlAttribute(name = "context-path")
    public void setContextPath(String contextPath) {
        contextPath(contextPath);
    }

    /**
     * When enabled server uses default features such as response compression.
     */
    public B useDefaultFilters(boolean useDefaultFilters) {
        endpoint.setUseDefaultFilters(useDefaultFilters);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:filter") },
            description = "When enabled the server uses the default set of Http server features.")
    @XmlAttribute(name = "use-default-filters")
    public void setUseDefaultFilters(boolean useDefaultFilters) {
        useDefaultFilters(useDefaultFilters);
    }

    /**
     * Sets the binaryMediaTypes.
     */
    public B binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.setBinaryMediaTypes(binaryMediaTypes);
        return self;
    }

    @SchemaProperty(advanced = true, description = "List of supported media types on this server.")
    @XmlTransient
    public void setBinaryMediaTypes(List<String> binaryMediaTypes) {
        binaryMediaTypes(binaryMediaTypes.stream().map(MediaType::valueOf).toList());
    }

    @XmlAttribute(name = "binary-media-types")
    public void setBinaryMediaTypes(String binaryMediaTypes) {
        setBinaryMediaTypes(Arrays.asList(binaryMediaTypes.split(",")));
    }

    /**
     * Sets the message converter.
     */
    public B messageConverter(HttpMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets the Http message converter bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the handleCookies property.
     */
    public B handleCookies(boolean flag) {
        endpoint.setHandleCookies(flag);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles cookies.")
    @XmlAttribute(name = "handle-cookies")
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
    @XmlAttribute(name = "remove-semicolon-path-content")
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
    @XmlAttribute(name = "default-status")
    public void setDefaultStatus(HttpStatus status) {
        defaultStatus(status);
    }

    @Override
    public B timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return self;
    }

    @SchemaProperty(description = "Sets the server timeout while waiting for incoming requests.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    /**
     * Sets the server side authentication for all request paths.
     */
    public B authentication(HttpAuthentication auth) {
        return authentication("/*", auth);
    }

    /**
     * Sets the server side authentication for the given request path.
     */
    public B authentication(String resourcePath, HttpAuthentication auth) {
        endpoint.setAuthentication(auth);
        endpoint.setSecuredPath(resourcePath);
        return self;
    }

    /**
     * Adds a Citrus server interceptor that runs around the request handling.
     */
    public B interceptor(HttpServerInterceptor interceptor) {
        List<HttpServerInterceptor> interceptors = new ArrayList<>(endpoint.getServerInterceptors());
        interceptors.add(interceptor);
        endpoint.setServerInterceptors(interceptors);
        return self;
    }

    /**
     * Sets the list of Citrus server interceptors that run around the request handling.
     */
    public B serverInterceptors(List<HttpServerInterceptor> interceptors) {
        endpoint.setServerInterceptors(new ArrayList<>(interceptors));
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Use given authentication mechanism for all request paths.")
    @XmlAttribute
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    /**
     * Secures the connections on the configured secure port.
     */
    public B secured(HttpSecureConnection conn) {
        return secured(securePort, conn);
    }

    /**
     * Secures the connections on the given secure port.
     */
    public B secured(int securePort, HttpSecureConnection conn) {
        this.securePort = securePort;
        endpoint.setSecurePort(securePort);
        endpoint.setSecureConnection(conn);
        return self;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Secure the connections on given secured port with given security mechanism.")
    @XmlAttribute(name = "secured-connection")
    public void setSecuredConnection(String connection) {
        this.securedConnection = connection;
    }
}
