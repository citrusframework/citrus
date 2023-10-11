/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.channel.ChannelEndpointBuilder;
import org.citrusframework.channel.ChannelMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ChannelEndpointConfigParser implements AnnotationConfigParser<ChannelEndpointConfig, ChannelEndpoint> {

    @Override
    public ChannelEndpoint parse(ChannelEndpointConfig annotation, ReferenceResolver referenceResolver) {
        ChannelEndpointBuilder builder = new ChannelEndpointBuilder();

        String channel = annotation.channel();
        String channelName = annotation.channelName();

        if (StringUtils.hasText(channel)) {
            builder.channel(referenceResolver.resolve(annotation.channel(), MessageChannel.class));
        }

        if (StringUtils.hasText(channelName)) {
            builder.channel(annotation.channelName());
        }

        if (StringUtils.hasText(annotation.messagingTemplate())) {
            //messagingTemplate
            String messagingTemplate = "messagingTemplate"; //default value

            if (StringUtils.hasText(annotation.messagingTemplate())) {
                messagingTemplate = annotation.messagingTemplate();
            }

            builder.messagingTemplate(referenceResolver.resolve(messagingTemplate, MessagingTemplate.class));
        }

        builder.useObjectMessages(annotation.useObjectMessages());
        builder.filterInternalHeaders(annotation.filterInternalHeaders());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), ChannelMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.channelResolver())) {
            builder.channelResolver(referenceResolver.resolve(annotation.channelResolver(), DestinationResolver.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
