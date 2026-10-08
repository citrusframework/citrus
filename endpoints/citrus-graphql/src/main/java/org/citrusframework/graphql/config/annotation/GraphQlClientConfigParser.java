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
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.client.GraphQlClientBuilder;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.citrusframework.util.StringUtils.hasText;

public class GraphQlClientConfigParser implements AnnotationConfigParser<GraphQlClientConfig, GraphQlClient> {

    @Override
    public GraphQlClient parse(GraphQlClientConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        GraphQlClientBuilder builder = new GraphQlClientBuilder();

        if (hasText(annotation.restTemplate()) && hasText(annotation.requestFactory())) {
            throw new CitrusRuntimeException("When providing a 'rest-template' property, no 'request-factory' should be set!");
        }

        if (!hasText(annotation.requestUrl()) && !hasText(annotation.endpointResolver())) {
            throw new CitrusRuntimeException("One of the properties 'request-url' or 'endpoint-resolver' is required!");
        }

        if (hasText(annotation.restTemplate())) {
            builder.restTemplate(referenceResolver.resolve(annotation.restTemplate(), RestTemplate.class));
        }

        if (hasText(annotation.requestFactory())) {
            builder.requestFactory(referenceResolver.resolve(annotation.requestFactory(), ClientHttpRequestFactory.class));
        }

        builder.requestUrl(context.replaceDynamicContentInString(annotation.requestUrl()));
        if (hasText(annotation.path())) {
            builder.path(context.replaceDynamicContentInString(annotation.path()));
        }

        if (annotation.schema().length > 0) {
            builder.schema(annotation.schema());
        }

        builder.requestMethod(annotation.requestMethod());
        builder.strict(annotation.strict());

        if (hasText(annotation.endpointResolver())) {
            builder.endpointResolver(referenceResolver.resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        if (annotation.interceptors().length > 0) {
            builder.interceptors(referenceResolver.resolve(annotation.interceptors(), ClientHttpRequestInterceptor.class));
        }

        builder.timeout(annotation.timeout());
        builder.errorHandlingStrategy(annotation.errorStrategy());
        if (hasText(annotation.errorHandler())) {
            builder.errorHandler(referenceResolver.resolve(annotation.errorHandler(), ResponseErrorHandler.class));
        }

        builder.handleCookies(annotation.handleCookies());
        builder.disableRedirectHandling(annotation.disableRedirectHandling());
        builder.charset(annotation.charset());
        if (hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());
        builder.headerMapper(DefaultHttpHeaderMapper.outboundMapper());

        if (hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (hasText(annotation.authentication())) {
            builder.authentication(referenceResolver.resolve(annotation.authentication(), HttpAuthentication.class));
        }

        if (hasText(annotation.secured())) {
            builder.secured(referenceResolver.resolve(annotation.secured(), HttpSecureConnection.class));
        }

        return (GraphQlClient) builder.initialize().build();
    }
}
