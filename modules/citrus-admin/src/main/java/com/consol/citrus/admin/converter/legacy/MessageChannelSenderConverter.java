/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.converter.legacy;

import com.consol.citrus.admin.converter.endpoint.AbstractEndpointConverter;
import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.MessageChannelSender;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.channel.ChannelResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class MessageChannelSenderConverter extends AbstractEndpointConverter<MessageChannelSender> {

    @Override
    public EndpointData convert(MessageChannelSender definition) {
        EndpointData endpointData = new EndpointData(getEndpointType());

        endpointData.setName(definition.getId());

        if (StringUtils.hasText(definition.getChannelName())) {
            endpointData.add(property("channelName", "Channel", definition));
        } else {
            endpointData.add(property("channel", definition));
        }

        endpointData.add(property("messageChannelTemplate", definition)
                .optionKey(MessagingTemplate.class.getName()));
        endpointData.add(property("messagingTemplate", definition)
                .optionKey(MessagingTemplate.class.getName()));
        endpointData.add(property("channelResolver", definition)
                .optionKey(ChannelResolver.class.getName()));

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<MessageChannelSender> getModelClass() {
        return MessageChannelSender.class;
    }

    @Override
    public String getEndpointType() {
        return "channel";
    }
}
