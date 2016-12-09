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

package com.consol.citrus.vertx.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.vertx.endpoint.VertxEndpoint;
import com.consol.citrus.vertx.endpoint.VertxEndpointBuilder;
import com.consol.citrus.vertx.factory.VertxInstanceFactory;
import com.consol.citrus.vertx.message.VertxMessageConverter;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class VertxEndpointConfigParser extends AbstractAnnotationConfigParser<VertxEndpointConfig, VertxEndpoint> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public VertxEndpointConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public VertxEndpoint parse(VertxEndpointConfig annotation) {
        VertxEndpointBuilder builder = new VertxEndpointBuilder();

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.address())) {
            builder.address(annotation.address());
        }

        builder.vertxFactory(getReferenceResolver().resolve(annotation.vertxFactory(), VertxInstanceFactory.class));

        builder.pubSubDomain(annotation.pubSubDomain());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), VertxMessageConverter.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
