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

package org.citrusframework.ws.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.server.WebServiceServerBuilder;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebServiceServerConfigParser implements AnnotationConfigParser<WebServiceServerConfig, WebServiceServer> {

    @Override
    public WebServiceServer parse(WebServiceServerConfig annotation, ReferenceResolver referenceResolver) {
        WebServiceServerBuilder builder = new WebServiceServerBuilder();

        builder.handleMimeHeaders(annotation.handleMimeHeaders());
        builder.handleAttributeHeaders(annotation.handleAttributeHeaders());
        builder.keepSoapEnvelope(annotation.keepSoapEnvelope());

        if (StringUtils.hasText(annotation.soapHeaderNamespace())) {
            builder.soapHeaderNamespace(annotation.soapHeaderNamespace());
        }

        if (StringUtils.hasText(annotation.soapHeaderPrefix())) {
            builder.soapHeaderPrefix(annotation.soapHeaderPrefix());
        }

        if (StringUtils.hasText(annotation.messageFactory())) {
            builder.messageFactory(annotation.messageFactory());
        }

        builder.timeout(annotation.timeout());
        builder.port(annotation.port());
        builder.autoStart(annotation.autoStart());

        if (StringUtils.hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        if (StringUtils.hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        builder.connectors(referenceResolver.resolve(annotation.connectors(), Connector.class));

        if (StringUtils.hasText(annotation.connector())) {
            builder.connector(referenceResolver.resolve(annotation.connector(), Connector.class));
        }

        builder.rootParentContext(annotation.rootParentContext());

        if (StringUtils.hasText(annotation.servletName())) {
            builder.servletName(annotation.servletName());
        }

        if (StringUtils.hasText(annotation.servletMappingPath())) {
            builder.servletMappingPath(annotation.servletMappingPath());
        }

        if (StringUtils.hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (StringUtils.hasText(annotation.servletHandler())) {
            builder.servletHandler(referenceResolver.resolve(annotation.servletHandler(), ServletHandler.class));
        }

        if (StringUtils.hasText(annotation.securityHandler())) {
            builder.securityHandler(referenceResolver.resolve(annotation.securityHandler(), SecurityHandler.class));
        }

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(referenceResolver.resolve(annotation.interceptors(), EndpointInterceptor.class));

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), WebServiceMessageConverter.class));
        }

        return builder.initialize().build();
    }
}
