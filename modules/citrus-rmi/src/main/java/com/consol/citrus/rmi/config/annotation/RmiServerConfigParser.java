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

package com.consol.citrus.rmi.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.rmi.message.RmiMessageConverter;
import com.consol.citrus.rmi.server.RmiServer;
import com.consol.citrus.rmi.server.RmiServerBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerConfigParser extends AbstractAnnotationConfigParser<RmiServerConfig, RmiServer> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public RmiServerConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public RmiServer parse(RmiServerConfig annotation) {
        RmiServerBuilder builder = new RmiServerBuilder();

        if (StringUtils.hasText(annotation.serverUrl())) {
            builder.serverUrl(annotation.serverUrl());
        }

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.binding())) {
            builder.binding(annotation.binding());
        }

        builder.createRegistry(annotation.createRegistry());

        builder.remoteInterfaces(annotation.remoteInterfaces());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), RmiMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(getReferenceResolver().resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.build();
    }
}
