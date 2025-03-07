/*
 * Copyright the original author or authors.
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

package org.citrusframework.camel.config.annotation;

import org.apache.camel.CamelContext;
import org.citrusframework.TestActor;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.camel.endpoint.CamelEndpointBuilder;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

public class CamelEndpointConfigParser implements AnnotationConfigParser<CamelEndpointConfig, CamelEndpoint> {

    @Override
    public CamelEndpoint parse(CamelEndpointConfig annotation, ReferenceResolver referenceResolver) {
        CamelEndpointBuilder builder = new CamelEndpointBuilder();

        builder.endpointUri(annotation.endpointUri());

        if (StringUtils.hasText(annotation.camelContext())) {
            builder.camelContext(referenceResolver.resolve(annotation.camelContext(), CamelContext.class));
        } else if (referenceResolver.isResolvable(CamelSettings.getContextName())) {
            builder.camelContext(referenceResolver.resolve(CamelSettings.getContextName(), CamelContext.class));
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

        return builder.initialize().build();
    }
}
