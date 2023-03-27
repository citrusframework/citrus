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

package org.citrusframework.citrus.vertx.config.annotation;

import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.config.annotation.AnnotationConfigParser;
import org.citrusframework.citrus.message.MessageCorrelator;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.vertx.endpoint.VertxSyncEndpoint;
import org.citrusframework.citrus.vertx.endpoint.VertxSyncEndpointBuilder;
import org.citrusframework.citrus.vertx.factory.VertxInstanceFactory;
import org.citrusframework.citrus.vertx.message.VertxMessageConverter;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class VertxSyncEndpointConfigParser implements AnnotationConfigParser<VertxSyncEndpointConfig, VertxSyncEndpoint> {

    @Override
    public VertxSyncEndpoint parse(VertxSyncEndpointConfig annotation, ReferenceResolver referenceResolver) {
        VertxSyncEndpointBuilder builder = new VertxSyncEndpointBuilder();

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.address())) {
            builder.address(annotation.address());
        }

        builder.pubSubDomain(annotation.pubSubDomain());

        builder.vertxFactory(referenceResolver.resolve(annotation.vertxFactory(), VertxInstanceFactory.class));

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), VertxMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
