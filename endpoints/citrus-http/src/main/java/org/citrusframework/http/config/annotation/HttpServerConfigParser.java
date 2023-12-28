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

package org.citrusframework.http.config.annotation;

import jakarta.servlet.Filter;
import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.stream.Streams.of;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpServerConfigParser implements AnnotationConfigParser<HttpServerConfig, HttpServer> {

    @Override
    public HttpServer parse(HttpServerConfig annotation, ReferenceResolver referenceResolver) {
        HttpServerBuilder builder = new HttpServerBuilder();

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());
        builder.handleAttributeHeaders(annotation.handleAttributeHeaders());
        builder.handleCookies(annotation.handleCookies());

        builder.debugLogging(annotation.debugLogging());

        if (hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(referenceResolver.resolve(annotation.interceptors(), HandlerInterceptor.class));

        if (hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        if (hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        if (hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        builder.rootParentContext(annotation.rootParentContext());

        builder.connectors(referenceResolver.resolve(annotation.connectors(), Connector.class));

        if (annotation.filters().length > 0) {
            builder.filters(referenceResolver.resolveAll(Filter.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> of(annotation.filters()).anyMatch(f -> f.equals(entry.getKey())))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        Map<String, String> filterMappings = new HashMap<>();
        for (String filterMapping : annotation.filterMappings()) {
            String[] pair = filterMapping.split("=");
            if (pair.length != 2) {
                throw new CitrusRuntimeException("Invalid filter mapping: " + filterMapping);
            }
            filterMappings.put(pair[0], pair[1]);
        }
        builder.filterMappings(filterMappings);

        List<MediaType> binaryMediaTypes = new ArrayList<>();
        for (String mediaType : annotation.binaryMediaTypes()) {
            binaryMediaTypes.add(MediaType.valueOf(mediaType));
        }

        if (!binaryMediaTypes.isEmpty()) {
            builder.binaryMediaTypes(binaryMediaTypes);
        }

        if (hasText(annotation.connector())) {
            builder.connector(referenceResolver.resolve(annotation.connector(), Connector.class));
        }

        if (hasText(annotation.servletName())) {
            builder.servletName(annotation.servletName());
        }

        if (hasText(annotation.servletMappingPath())) {
            builder.servletMappingPath(annotation.servletMappingPath());
        }

        if (hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (hasText(annotation.servletHandler())) {
            builder.servletHandler(referenceResolver.resolve(annotation.servletHandler(), ServletHandler.class));
        }

        if (hasText(annotation.securityHandler())) {
            builder.securityHandler(referenceResolver.resolve(annotation.securityHandler(), SecurityHandler.class));
        }

        if (hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        builder.defaultStatus(annotation.defaultStatus());
        builder.responseCacheSize(annotation.responseCacheSize());

        if (hasText(annotation.authentication())) {
            builder.authentication(annotation.securedPath(), referenceResolver.resolve(annotation.authentication(), HttpAuthentication.class));
        }

        if (hasText(annotation.secured())) {
            builder.secured(annotation.securePort(), referenceResolver.resolve(annotation.secured(), HttpSecureConnection.class));
        }

        return builder.initialize().build();
    }
}
