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

package org.citrusframework.http.config.annotation;

import jakarta.servlet.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

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

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(referenceResolver.resolve(annotation.interceptors(), HandlerInterceptor.class));

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        if (StringUtils.hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        builder.rootParentContext(annotation.rootParentContext());

        builder.connectors(referenceResolver.resolve(annotation.connectors(), Connector.class));

        if (annotation.filters().length > 0) {
            builder.filters(referenceResolver.resolveAll(Filter.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> Stream.of(annotation.filters()).anyMatch(f -> f.equals(entry.getKey())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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

        if (StringUtils.hasText(annotation.connector())) {
            builder.connector(referenceResolver.resolve(annotation.connector(), Connector.class));
        }

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

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        builder.defaultStatus(annotation.defaultStatus());
        builder.responseCacheSize(annotation.responseCacheSize());

        return builder.initialize().build();
    }
}
