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
package org.citrusframework.graphql.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.graphql.server.GraphQlServerBuilder;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.spi.ReferenceResolver;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.citrusframework.util.StringUtils.hasText;

public class GraphQlServerConfigParser implements AnnotationConfigParser<GraphQlServerConfig, GraphQlServer> {

    @Override
    public GraphQlServer parse(GraphQlServerConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        GraphQlServerBuilder builder = new GraphQlServerBuilder();

        builder.port(annotation.port());
        builder.path(context.replaceDynamicContentInString(annotation.path()));
        if (annotation.schema().length > 0) {
            builder.schema(annotation.schema());
        }

        builder.strict(annotation.strict());
        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());
        builder.debugLogging(annotation.debugLogging());

        if (hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (annotation.interceptors().length > 0) {
            builder.interceptors(referenceResolver.resolve(annotation.interceptors(), HandlerInterceptor.class));
        }

        if (hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (hasText(annotation.authentication())) {
            builder.authentication(annotation.securedPath(), referenceResolver.resolve(annotation.authentication(), HttpAuthentication.class));
        }

        if (hasText(annotation.secured())) {
            builder.secured(annotation.securePort(), referenceResolver.resolve(annotation.secured(), HttpSecureConnection.class));
        }

        return builder.initialize().build();
    }
}
