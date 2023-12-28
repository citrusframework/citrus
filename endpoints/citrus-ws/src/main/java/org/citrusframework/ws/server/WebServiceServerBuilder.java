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

import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.ws.server.EndpointInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebServiceServerBuilder extends AbstractServerBuilder<WebServiceServer, WebServiceServerBuilder> {

    /**
     * Endpoint target
     */
    private final WebServiceServer endpoint = new WebServiceServer();

    @Override
    protected WebServiceServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     *
     * @param port
     * @return
     */
    public WebServiceServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the context config location.
     *
     * @param configLocation
     * @return
     */
    public WebServiceServerBuilder contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return this;
    }

    /**
     * Sets the resource base.
     *
     * @param resourceBase
     * @return
     */
    public WebServiceServerBuilder resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return this;
    }

    /**
     * Enables/disables the root parent context.
     *
     * @param rootParentContext
     * @return
     */
    public WebServiceServerBuilder rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return this;
    }

    /**
     * Sets the connectors.
     *
     * @param connectors
     * @return
     */
    public WebServiceServerBuilder connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[connectors.size()]));
        return this;
    }

    /**
     * Sets the connector.
     *
     * @param connector
     * @return
     */
    public WebServiceServerBuilder connector(Connector connector) {
        endpoint.setConnector(connector);
        return this;
    }

    /**
     * Sets the servlet name.
     *
     * @param servletName
     * @return
     */
    public WebServiceServerBuilder servletName(String servletName) {
        endpoint.setServletName(servletName);
        return this;
    }

    /**
     * Sets the servlet mapping path.
     *
     * @param servletMappingPath
     * @return
     */
    public WebServiceServerBuilder servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return this;
    }

    /**
     * Sets the context path.
     *
     * @param contextPath
     * @return
     */
    public WebServiceServerBuilder contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return this;
    }

    /**
     * Sets the servlet handler.
     *
     * @param servletHandler
     * @return
     */
    public WebServiceServerBuilder servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return this;
    }

    /**
     * Sets the security handler.
     *
     * @param securityHandler
     * @return
     */
    public WebServiceServerBuilder securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return this;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     * @return
     */
    public WebServiceServerBuilder messageConverter(WebServiceMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    @Override
    public WebServiceServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    /**
     * Sets the interceptors.
     *
     * @param interceptors
     * @return
     */
    public WebServiceServerBuilder interceptors(List<EndpointInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return this;
    }

    /**
     * Sets the interceptors.
     *
     * @param interceptors
     * @return
     */
    public WebServiceServerBuilder interceptors(EndpointInterceptor... interceptors) {
        endpoint.setInterceptors(Arrays.asList(interceptors));
        return this;
    }

    /**
     * Sets the message factory.
     *
     * @param messageFactory
     * @return
     */
    public WebServiceServerBuilder messageFactory(String messageFactory) {
        endpoint.setMessageFactoryName(messageFactory);
        return this;
    }

    /**
     * Sets the keepSoapEnvelope property.
     *
     * @param flag
     * @return
     */
    public WebServiceServerBuilder keepSoapEnvelope(boolean flag) {
        endpoint.setKeepSoapEnvelope(flag);
        return this;
    }

    /**
     * Sets the handleMimeHeaders property.
     *
     * @param flag
     * @return
     */
    public WebServiceServerBuilder handleMimeHeaders(boolean flag) {
        endpoint.setHandleMimeHeaders(flag);
        return this;
    }

    /**
     * Sets the handleAttributeHeaders property.
     *
     * @param flag
     * @return
     */
    public WebServiceServerBuilder handleAttributeHeaders(boolean flag) {
        endpoint.setHandleAttributeHeaders(flag);
        return this;
    }

    /**
     * Sets the SOAP header namespace.
     *
     * @param namespace
     * @return
     */
    public WebServiceServerBuilder soapHeaderNamespace(String namespace) {
        endpoint.setSoapHeaderNamespace(namespace);
        return this;
    }

    /**
     * Sets the SOAP header prefix.
     *
     * @param prefix
     * @return
     */
    public WebServiceServerBuilder soapHeaderPrefix(String prefix) {
        endpoint.setSoapHeaderPrefix(prefix);
        return this;
    }
}
