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

package com.consol.citrus.http.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.Filter;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpServerConfigParser extends AbstractAnnotationConfigParser<HttpServerConfig, HttpServer> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public HttpServerConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public HttpServer parse(HttpServerConfig annotation) {
        HttpServerBuilder builder = new HttpServerBuilder();

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());
        builder.handleAttributeHeaders(annotation.handleAttributeHeaders());
        builder.handleCookies(annotation.handleCookies());

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(getReferenceResolver().resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(getReferenceResolver().resolve(annotation.interceptors(), HandlerInterceptor.class));

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        if (StringUtils.hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        builder.rootParentContext(annotation.rootParentContext());

        builder.connectors(getReferenceResolver().resolve(annotation.connectors(), Connector.class));

        List<Filter> filterBeans = getReferenceResolver().resolve(annotation.filters(), Filter.class);
        Map<String, Filter> filters = new HashMap<>();
        for (int i = 0; i < annotation.filters().length; i++) {
            filters.put(annotation.filters()[i], filterBeans.get(i));
        }
        builder.filters(filters);

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
            builder.connector(getReferenceResolver().resolve(annotation.connector(), Connector.class));
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
            builder.servletHandler(getReferenceResolver().resolve(annotation.servletHandler(), ServletHandler.class));
        }

        if (StringUtils.hasText(annotation.securityHandler())) {
            builder.securityHandler(getReferenceResolver().resolve(annotation.securityHandler(), SecurityHandler.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        builder.defaultStatus(annotation.defaultStatus());

        return builder.initialize().build();
    }
}
