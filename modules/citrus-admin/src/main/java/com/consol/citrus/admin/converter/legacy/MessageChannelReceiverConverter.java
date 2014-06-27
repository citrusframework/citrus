/*
 * Copyright 2006-2014 the original author or authors.
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
import com.consol.citrus.model.config.core.MessageChannelReceiver;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.channel.ChannelResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class MessageChannelReceiverConverter extends AbstractEndpointConverter<MessageChannelReceiver> {

    @Override
    public EndpointData convert(MessageChannelReceiver definition) {
        EndpointData endpointData = new EndpointData(getEndpointType(), definition.getId());

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
        endpointData.add(property("receiveTimeout", definition));

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<MessageChannelReceiver> getModelClass() {
        return MessageChannelReceiver.class;
    }

    @Override
    public String getEndpointType() {
        return "channel";
    }
}
