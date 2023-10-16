/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.config.annotation;

import org.apache.camel.CamelContext;
import org.citrusframework.TestActor;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.endpoint.CamelSyncEndpointBuilder;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class CamelSyncEndpointConfigParser implements AnnotationConfigParser<CamelSyncEndpointConfig, CamelSyncEndpoint> {

    @Override
    public CamelSyncEndpoint parse(CamelSyncEndpointConfig annotation, ReferenceResolver referenceResolver) {
        CamelSyncEndpointBuilder builder = new CamelSyncEndpointBuilder();

        builder.endpointUri(annotation.endpointUri());

        if (StringUtils.hasText(annotation.camelContext())) {
            builder.camelContext(referenceResolver.resolve(annotation.camelContext(), CamelContext.class));
        } else if (referenceResolver.isResolvable("camelContext")) {
            builder.camelContext(referenceResolver.resolve("camelContext", CamelContext.class));
        } else {
            builder.camelContext(referenceResolver.resolve(CamelContext.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), CamelMessageConverter.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        return builder.initialize().build();
    }
}
