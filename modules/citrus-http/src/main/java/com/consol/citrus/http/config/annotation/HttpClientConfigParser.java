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
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.message.MessageCorrelator;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpClientConfigParser extends AbstractAnnotationConfigParser<HttpClientConfig, HttpClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public HttpClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public HttpClient parse(HttpClientConfig annotation) {
        HttpClientBuilder builder = new HttpClientBuilder();

        if (StringUtils.hasText(annotation.restTemplate()) && StringUtils.hasText(annotation.requestFactory())) {
            throw new CitrusRuntimeException("When providing a 'rest-template' property, " +
                    "no 'request-factory' should be set!");
        }

        if (!StringUtils.hasText(annotation.requestUrl()) && !StringUtils.hasText(annotation.endpointResolver())) {
            throw new CitrusRuntimeException("One of the properties 'request-url' or " +
                    "'endpoint-resolver' is required!");
        }

        if (StringUtils.hasText(annotation.restTemplate())){
            builder.restTemplate(getReferenceResolver().resolve(annotation.restTemplate(), RestTemplate.class));
        } else {
            builder.requestFactory(getReferenceResolver().resolve(annotation.requestFactory(), ClientHttpRequestFactory.class));
        }

        builder.requestUrl(annotation.requestUrl());
        builder.requestMethod(annotation.requestMethod());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(getReferenceResolver().resolve(annotation.correlator(), MessageCorrelator.class));
        }

        if (StringUtils.hasText(annotation.endpointResolver())) {
            builder.endpointResolver(getReferenceResolver().resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        builder.charset(annotation.charset());
        builder.contentType(annotation.contentType());
        builder.pollingInterval(annotation.pollingInterval());

        builder.errorHandlingStrategy(annotation.errorStrategy());

        builder.interceptors(getReferenceResolver().resolve(annotation.interceptors(), ClientHttpRequestInterceptor.class));

        // Set outbound header mapper
        builder.headerMapper(DefaultHttpHeaderMapper.outboundMapper());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.build();
    }
}
