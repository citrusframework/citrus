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

package org.citrusframework.jms.config.annotation;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.endpoint.JmsEndpointBuilder;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmsEndpointConfigParser implements AnnotationConfigParser<JmsEndpointConfig, JmsEndpoint> {

    @Override
    public JmsEndpoint parse(JmsEndpointConfig annotation, ReferenceResolver referenceResolver) {
        JmsEndpointBuilder builder = new JmsEndpointBuilder();

        String jmsTemplate = annotation.jmsTemplate();
        String destination = annotation.destination();
        String destinationName = annotation.destinationName();

        if (StringUtils.hasText(destination) || StringUtils.hasText(destinationName)) {
            //connectionFactory
            String connectionFactory = "connectionFactory"; //default value

            if (StringUtils.hasText(annotation.connectionFactory())) {
                connectionFactory = annotation.connectionFactory();
            }

            if (!StringUtils.hasText(connectionFactory)) {
                throw new CitrusRuntimeException("Required connection-factory is missing for jms configuration");
            }

            builder.connectionFactory(referenceResolver.resolve(connectionFactory, ConnectionFactory.class));

            //destination
            if (StringUtils.hasText(destination)) {
                builder.destination(referenceResolver.resolve(annotation.destination(), Destination.class));
            } else {
                builder.destination(annotation.destinationName());
            }
        } else if (StringUtils.hasText(jmsTemplate)) {
            if (StringUtils.hasText(annotation.connectionFactory()) ||
                    StringUtils.hasText(destination) ||
                    StringUtils.hasText(destinationName)) {
                throw new CitrusRuntimeException("When providing a jms-template, none of " +
                        "connection-factory, destination, or destination-name should be provided");
            }

            builder.jmsTemplate(referenceResolver.resolve(jmsTemplate, JmsTemplate.class));
        } else {
            throw new CitrusRuntimeException("Either a jms-template reference " +
                    "or one of destination or destination-name must be provided");
        }

        if (annotation.autoStart() && !annotation.pubSubDomain()) {
            throw new CitrusRuntimeException("When providing auto start enabled,  " +
                    "pubSubDomain should also be enabled");
        }

        if (annotation.durableSubscription() && !annotation.pubSubDomain()) {
            throw new CitrusRuntimeException("When providing durable subscription enabled,  " +
                    "pubSubDomain should also be enabled");
        }

        builder.pubSubDomain(annotation.pubSubDomain());
        builder.autoStart(annotation.autoStart());
        builder.durableSubscription(annotation.durableSubscription());
        if (StringUtils.hasText(annotation.durableSubscriberName())) {
            builder.durableSubscriberName(annotation.durableSubscriberName());
        }

        builder.useObjectMessages(annotation.useObjectMessages());
        builder.filterInternalHeaders(annotation.filterInternalHeaders());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), JmsMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.destinationResolver())) {
            builder.destinationResolver(referenceResolver.resolve(annotation.destinationResolver(), DestinationResolver.class));
        }

        if (StringUtils.hasText(annotation.destinationNameResolver())) {
            builder.destinationNameResolver(referenceResolver.resolve(annotation.destinationNameResolver(), EndpointUriResolver.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
