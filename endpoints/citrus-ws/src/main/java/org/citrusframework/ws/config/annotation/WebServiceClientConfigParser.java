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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebServiceClientConfigParser implements AnnotationConfigParser<WebServiceClientConfig, WebServiceClient> {

    @Override
    public WebServiceClient parse(WebServiceClientConfig annotation, ReferenceResolver referenceResolver) {
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
            builder.webServiceTemplate(referenceResolver.resolve(annotation.webServiceTemplate(), WebServiceTemplate.class));
        }

        if (StringUtils.hasText(annotation.messageFactory())) {
            builder.messageFactory(referenceResolver.resolve(annotation.messageFactory(), WebServiceMessageFactory.class));
        } else {
            builder.messageFactory(referenceResolver.resolve("messageFactory", WebServiceMessageFactory.class));
        }

        if (StringUtils.hasText(annotation.messageSender())) {
            builder.messageSender(referenceResolver.resolve(annotation.messageSender(), WebServiceMessageSender.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), WebServiceMessageConverter.class));
        }

        if (annotation.interceptors().length > 0) {
            builder.interceptors(referenceResolver.resolve(annotation.interceptors(), ClientInterceptor.class));
        }

        if (StringUtils.hasText(annotation.interceptor())) {
            List<ClientInterceptor> interceptors = new ArrayList<>();
            interceptors.add(referenceResolver.resolve(annotation.interceptor(), ClientInterceptor.class));
            builder.interceptors(interceptors);
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        if (StringUtils.hasText(annotation.endpointResolver())) {
            builder.endpointResolver(referenceResolver.resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        builder.faultStrategy(annotation.faultStrategy());
        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
