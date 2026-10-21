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
package org.citrusframework.http.config.annotation;

import io.vertx.core.Vertx;
import java.util.ArrayList;
import java.util.List;
import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.http.server.vertx.VertxHttpServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.springframework.http.MediaType;

import static org.citrusframework.util.StringUtils.hasText;

/**
 * Creates the Vert.x based Http server from the {@link VertxHttpServerConfig} annotation.
 *
 * @since 5.1.0
 */
public class VertxHttpServerConfigParser implements AnnotationConfigParser<VertxHttpServerConfig, VertxHttpServer> {

    @Override
    public VertxHttpServer parse(VertxHttpServerConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        VertxHttpServerBuilder builder = new VertxHttpServerBuilder();
        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());
        builder.debugLogging(annotation.debugLogging());

        if (hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());
        builder.securePort(annotation.securePort());

        if (hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        builder.handleCookies(annotation.handleCookies());
        builder.removeSemicolonPathContent(annotation.removeSemicolonPathContent());
        builder.defaultStatus(annotation.defaultStatus());
        builder.useDefaultFilters(annotation.useDefaultFilters());

        List<MediaType> binaryMediaTypes = new ArrayList<>();
        for (String mediaType : annotation.binaryMediaTypes()) {
            binaryMediaTypes.add(MediaType.valueOf(mediaType));
        }

        if (!binaryMediaTypes.isEmpty()) {
            builder.binaryMediaTypes(binaryMediaTypes);
        }

        List<Object> resolvedInterceptors = referenceResolver.resolve(annotation.interceptors(), Object.class);
        builder.interceptors(resolvedInterceptors);

        if (hasText(annotation.authentication())) {
            builder.authentication(annotation.securedPath(), referenceResolver.resolve(annotation.authentication(), HttpAuthentication.class));
        }

        if (hasText(annotation.secured())) {
            builder.secured(annotation.securePort(), referenceResolver.resolve(annotation.secured(), HttpSecureConnection.class));
        }

        if (hasText(annotation.vertx())) {
            builder.vertx(referenceResolver.resolve(annotation.vertx(), Vertx.class));
        }

        builder.workerPoolSize(annotation.workerPoolSize());

        return builder.initialize().build();
    }
}
