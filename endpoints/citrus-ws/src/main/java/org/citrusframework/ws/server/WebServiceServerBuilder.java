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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;
import org.eclipse.jetty.ee11.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-ws")
@XmlType(name = "", propOrder = {})
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
    @XmlAttribute
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
    @XmlAttribute(name = "context-config-location")
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
    @XmlAttribute(name = "resource-base")
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
    @XmlAttribute(name = "root-parent-context")
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
    public WebServiceServerBuilder connector(Connector connector) {
        endpoint.setConnector(connector);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:servlet") },
            description = "Add a connector to this server.")
    @XmlAttribute
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
    @XmlAttribute
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
    @XmlAttribute(name = "servlet-mapping-path")
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
    @XmlAttribute(name = "context-path")
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
    @XmlAttribute(name = "servlet-handler")
    public void setServletHandler(String servletHandler) {
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
    @XmlAttribute(name = "security-handler")
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
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public WebServiceServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the server timeout while waiting for incoming requests.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    /**
     * Sets the interceptors.
     */
    @SuppressWarnings("unchecked")
    public WebServiceServerBuilder interceptors(List<EndpointInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets the list of endpoint interceptor bean references.")
    @XmlTransient
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @XmlAttribute
    public void setInterceptors(String interceptors) {
        setInterceptors(Arrays.asList(interceptors.split(",")));
    }
    /**
     * Sets the interceptors.
     */
    @SuppressWarnings("unchecked")
    public WebServiceServerBuilder interceptors(EndpointInterceptor... interceptors) {
        endpoint.setInterceptors(new ArrayList<>(Arrays.asList(interceptors)));
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
    @XmlAttribute
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
    @XmlAttribute(name = "message-factory")
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
    @XmlAttribute(name = "keep-soap-envelope")
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
    @XmlAttribute(name = "handle-mime-headers")
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
    @XmlAttribute(name = "handle-attribute-headers")
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
    @XmlAttribute(name = "soap-header-namespace")
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
    @XmlAttribute(name = "soap-header-prefix")
    public void setSoapHeaderPrefix(String prefix) {
        soapHeaderPrefix(prefix);
    }
}
