/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.citrus.rmi.config.annotation;

import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.config.annotation.AnnotationConfigParser;
import org.citrusframework.citrus.endpoint.EndpointAdapter;
import org.citrusframework.citrus.message.MessageCorrelator;
import org.citrusframework.citrus.rmi.message.RmiMessageConverter;
import org.citrusframework.citrus.rmi.server.RmiServer;
import org.citrusframework.citrus.rmi.server.RmiServerBuilder;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerConfigParser implements AnnotationConfigParser<RmiServerConfig, RmiServer> {

    @Override
    public RmiServer parse(RmiServerConfig annotation, ReferenceResolver referenceResolver) {
        RmiServerBuilder builder = new RmiServerBuilder();

        builder.autoStart(annotation.autoStart());

        if (StringUtils.hasText(annotation.serverUrl())) {
            builder.serverUrl(annotation.serverUrl());
        }

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (StringUtils.hasText(annotation.binding())) {
            builder.binding(annotation.binding());
        }

        builder.createRegistry(annotation.createRegistry());

        builder.remoteInterfaces(annotation.remoteInterfaces());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), RmiMessageConverter.class));
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
