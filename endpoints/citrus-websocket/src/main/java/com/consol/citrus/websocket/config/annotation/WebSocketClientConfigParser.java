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

package com.consol.citrus.websocket.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.websocket.client.WebSocketClient;
import com.consol.citrus.websocket.client.WebSocketClientBuilder;
import com.consol.citrus.websocket.message.WebSocketMessageConverter;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketClientConfigParser implements AnnotationConfigParser<WebSocketClientConfig, WebSocketClient> {

    @Override
    public WebSocketClient parse(WebSocketClientConfig annotation, ReferenceResolver referenceResolver) {
        WebSocketClientBuilder builder = new WebSocketClientBuilder();

        builder.requestUrl(annotation.requestUrl());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), WebSocketMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.endpointResolver())) {
            builder.endpointResolver(referenceResolver.resolve(annotation.endpointResolver(), EndpointUriResolver.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
