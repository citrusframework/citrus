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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpClientConfigParser implements AnnotationConfigParser<HttpClientConfig, HttpClient> {

    @Override
    public HttpClient parse(HttpClientConfig annotation, ReferenceResolver referenceResolver) {
        HttpClientBuilder builder = new HttpClientBuilder();

        if (StringUtils.hasText(annotation.restTemplate()) && StringUtils.hasText(annotation.requestFactory())) {
            throw new CitrusRuntimeException("When providing a 'rest-template' property, " +
                    "no 'request-factory' should be set!");
        }

        if (!StringUtils.hasText(annotation.requestUrl()) && !StringUtils.hasText(annotation.endpointResolver())) {
            throw new CitrusRuntimeException("One of the properties 'request-url' or " +
                    "'endpoint-resolver' is required!");
        }

        if (StringUtils.hasText(annotation.restTemplate())) {
            builder.restTemplate(referenceResolver.resolve(annotation.restTemplate(), RestTemplate.class));
        }

        if (StringUtils.hasText(annotation.requestFactory())) {
            builder.requestFactory(referenceResolver.resolve(annotation.requestFactory(), ClientHttpRequestFactory.class));
        }

        builder.requestUrl(annotation.requestUrl());
        builder.requestMethod(annotation.requestMethod());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        if (StringUtils.hasText(annotation.endpointResolver())) {
            builder.endpointResolver(referenceResolver.resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        builder.defaultAcceptHeader(annotation.defaultAcceptHeader());
        builder.handleCookies(annotation.handleCookies());
        builder.charset(annotation.charset());
        builder.contentType(annotation.contentType());
        builder.pollingInterval(annotation.pollingInterval());

        builder.errorHandlingStrategy(annotation.errorStrategy());
        if (StringUtils.hasText(annotation.errorHandler())) {
            builder.errorHandler(referenceResolver.resolve(annotation.errorHandler(), ResponseErrorHandler.class));
        }

        List<MediaType> binaryMediaTypes = new ArrayList<>();
        for (String mediaType : annotation.binaryMediaTypes()) {
            binaryMediaTypes.add(MediaType.valueOf(mediaType));
        }

        if (!binaryMediaTypes.isEmpty()) {
            builder.binaryMediaTypes(binaryMediaTypes);
        }

        if (annotation.interceptors().length > 0) {
            builder.interceptors(referenceResolver.resolve(annotation.interceptors(), ClientHttpRequestInterceptor.class));
        }

        // Set outbound header mapper
        builder.headerMapper(DefaultHttpHeaderMapper.outboundMapper());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (StringUtils.hasText(annotation.authentication())) {
            builder.authentication(referenceResolver.resolve(annotation.authentication(), HttpAuthentication.class));
        }

        if (StringUtils.hasText(annotation.secured())) {
            builder.secured(referenceResolver.resolve(annotation.secured(), HttpSecureConnection.class));
        }

        return builder.initialize().build();
    }
}
