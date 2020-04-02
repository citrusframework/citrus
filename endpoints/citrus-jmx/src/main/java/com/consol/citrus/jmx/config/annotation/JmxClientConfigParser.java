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

package com.consol.citrus.jmx.config.annotation;

import javax.management.NotificationFilter;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.client.JmxClientBuilder;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClientConfigParser implements AnnotationConfigParser<JmxClientConfig, JmxClient> {

    @Override
    public JmxClient parse(JmxClientConfig annotation, ReferenceResolver referenceResolver) {
        JmxClientBuilder builder = new JmxClientBuilder();

        builder.serverUrl(annotation.serverUrl());

        if (StringUtils.hasText(annotation.username())) {
            builder.username(annotation.username());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        builder.autoReconnect(annotation.autoReconnect());
        builder.reconnectDelay(annotation.reconnectDelay());

        if (StringUtils.hasText(annotation.notificationFilter())) {
            builder.notificationFilter(referenceResolver.resolve(annotation.notificationFilter(), NotificationFilter.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), JmxMessageConverter.class));
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
