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

package com.consol.citrus.ws.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.client.WebServiceClientBuilder;
import com.consol.citrus.ws.message.converter.WebServiceMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebServiceClientConfigParser extends AbstractAnnotationConfigParser<WebServiceClientConfig, WebServiceClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public WebServiceClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public WebServiceClient parse(WebServiceClientConfig annotation) {
        WebServiceClientBuilder builder = new WebServiceClientBuilder();

        builder.defaultUri(annotation.requestUrl());

        if (StringUtils.hasText(annotation.webServiceTemplate()) && (StringUtils.hasText(annotation.messageFactory()) ||
                StringUtils.hasText(annotation.messageSender()))) {
            throw new CitrusRuntimeException("When providing a 'web-service-template' reference, none of " +
                    "'message-factory', 'message-sender' should be set");
        }

        if (!StringUtils.hasText(annotation.requestUrl()) && !StringUtils.hasText(annotation.endpointResolver())) {
            throw new CitrusRuntimeException("One of the properties 'request-url' or 'endpoint-resolver' is required!");
        }

        if (StringUtils.hasText(annotation.webServiceTemplate())) {
            builder.webServiceTemplate(getReferenceResolver().resolve(annotation.webServiceTemplate(), WebServiceTemplate.class));
        }

        if (StringUtils.hasText(annotation.messageFactory())) {
            builder.messageFactory(getReferenceResolver().resolve(annotation.messageFactory(), WebServiceMessageFactory.class));
        } else {
            builder.messageFactory(getReferenceResolver().resolve("messageFactory", WebServiceMessageFactory.class));
        }

        if (StringUtils.hasText(annotation.messageSender())) {
            builder.messageSender(getReferenceResolver().resolve(annotation.messageSender(), WebServiceMessageSender.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), WebServiceMessageConverter.class));
        }

        builder.interceptors(getReferenceResolver().resolve(annotation.interceptors(), ClientInterceptor.class));

        if (StringUtils.hasText(annotation.interceptor())) {
            builder.interceptor(getReferenceResolver().resolve(annotation.interceptor(), ClientInterceptor.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(getReferenceResolver().resolve(annotation.correlator(), MessageCorrelator.class));
        }

        if (StringUtils.hasText(annotation.endpointResolver())) {
            builder.endpointResolver(getReferenceResolver().resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        builder.faultStrategy(annotation.faultStrategy());
        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
