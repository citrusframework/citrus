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

package org.citrusframework.ws.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.yaml.SchemaProperty;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * @since 2.5
 */
public class WebServiceServerBuilder extends AbstractServerBuilder<WebServiceServer, WebServiceServerBuilder> {

    /**
     * Endpoint target
     */
    private final WebServiceServer endpoint = new WebServiceServer();

    private String servletHandler;
    private String securityHandler;
    private String messageConverter;
    private final List<String> interceptors = new ArrayList<>();
    private final List<String> connectors = new ArrayList<>();

    @Override
    public WebServiceServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(servletHandler)) {
                servletHandler(referenceResolver.resolve(servletHandler, ServletHandler.class));
            }

            if (StringUtils.hasText(securityHandler)) {
                securityHandler(referenceResolver.resolve(securityHandler, SecurityHandler.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, WebServiceMessageConverter.class));
            }

            if (!interceptors.isEmpty()) {
                interceptors(this.interceptors
                        .stream()
                        .map(entry -> referenceResolver.resolve(entry, EndpointInterceptor.class))
                        .toList());
            }

            for (String connector : connectors) {
                connector(referenceResolver.resolve(connector, Connector.class));
            }
        }

        return super.build();
    }

    @Override
    protected WebServiceServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public WebServiceServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @SchemaProperty(description = "The SOAP WebService server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the context config location.
     */
    public WebServiceServerBuilder contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the path to the Spring context configuration for this server.")
    public void setContextConfigLocation(String configLocation) {
        contextConfigLocation(configLocation);
    }

    /**
     * Sets the resource base.
     */
    public WebServiceServerBuilder resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the resource base path where the server reads resources from.")
    public void setResourceBase(String resourceBase) {
        resourceBase(resourceBase);
    }

    /**
     * Enables/disables the root parent context.
     */
    public WebServiceServerBuilder rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server uses the root Spring application context.")
    public void setRootParentContext(boolean rootParentContext) {
        rootParentContext(rootParentContext);
    }

    /**
     * Sets the connectors.
     */
    public WebServiceServerBuilder connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[0]));
        return this;
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
    public WebServiceServerBuilder connector(Connector connector) {
        endpoint.setConnector(connector);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Add a connector to this server.")
    public void setConnector(String connector) {
        this.connectors.add(connector);
    }

    /**
     * Sets the servlet name.
     */
    public WebServiceServerBuilder servletName(String servletName) {
        endpoint.setServletName(servletName);
        return this;
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
    public WebServiceServerBuilder servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return this;
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
    public WebServiceServerBuilder contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return this;
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
    public WebServiceServerBuilder servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return this;
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
    public WebServiceServerBuilder securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return this;
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
    public WebServiceServerBuilder messageConverter(WebServiceMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the Http message converter bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public WebServiceServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the server timeout while waiting for incoming requests.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    /**
     * Sets the interceptors.
     */
    public WebServiceServerBuilder interceptors(List<EndpointInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets the list of endpoint interceptor bean references.")
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    /**
     * Sets the interceptors.
     */
    public WebServiceServerBuilder interceptors(EndpointInterceptor... interceptors) {
        endpoint.setInterceptors(Arrays.asList(interceptors));
        return this;
    }

    /**
     * Sets a single handler interceptor.
     */
    public WebServiceServerBuilder interceptor(EndpointInterceptor interceptor) {
        endpoint.setInterceptors(Collections.singletonList(interceptor));
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets a endpoint interceptor.")
    public void setInterceptor(String interceptor) {
        this.interceptors.add(interceptor);
    }

    /**
     * Sets the message factory.
     */
    public WebServiceServerBuilder messageFactory(String messageFactory) {
        endpoint.setMessageFactoryName(messageFactory);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom message factory.")
    public void setMessageFactory(String messageFactory) {
        messageFactory(messageFactory);
    }

    /**
     * Sets the keepSoapEnvelope property.
     */
    public WebServiceServerBuilder keepSoapEnvelope(boolean flag) {
        endpoint.setKeepSoapEnvelope(flag);
        return this;
    }

    @SchemaProperty(
            advanced = true,
            description = "When enabled the server does not remove the SOAP envelope before processing messages.")
    public void setKeepSoapEnvelope(boolean flag) {
        keepSoapEnvelope(flag);
    }

    /**
     * Sets the handleMimeHeaders property.
     */
    public WebServiceServerBuilder handleMimeHeaders(boolean flag) {
        endpoint.setHandleMimeHeaders(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles mime headers.")
    public void setHandleMimeHeaders(boolean handleMimeHeaders) {
        handleMimeHeaders(handleMimeHeaders);
    }

    /**
     * Sets the handleAttributeHeaders property.
     */
    public WebServiceServerBuilder handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server handles attribute headers.")
    public void setHandleAttributeHeaders(boolean handleAttributeHeaders) {
        handleAttributeHeaders(handleAttributeHeaders);
    }

    /**
     * Sets the SOAP header namespace.
     */
    public WebServiceServerBuilder soapHeaderNamespace(String namespace) {
        endpoint.setSoapHeaderNamespace(namespace);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the SOAP header namespace.")
    public void setSoapHeaderNamespace(String namespace) {
        soapHeaderNamespace(namespace);
    }

    /**
     * Sets the SOAP header prefix.
     */
    public WebServiceServerBuilder soapHeaderPrefix(String prefix) {
        endpoint.setSoapHeaderPrefix(prefix);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the SOAP header namespace prefix.")
    public void setSoapHeaderPrefix(String prefix) {
        soapHeaderPrefix(prefix);
    }
}
