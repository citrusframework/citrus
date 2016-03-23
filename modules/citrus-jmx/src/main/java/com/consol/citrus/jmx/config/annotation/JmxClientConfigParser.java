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

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.client.JmxClientBuilder;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.message.MessageCorrelator;
import org.springframework.util.StringUtils;

import javax.management.NotificationFilter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClientConfigParser extends AbstractAnnotationConfigParser<JmxClientConfig, JmxClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public JmxClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public JmxClient parse(JmxClientConfig annotation) {
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
            builder.notificationFilter(getReferenceResolver().resolve(annotation.notificationFilter(), NotificationFilter.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), JmxMessageConverter.class));
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
