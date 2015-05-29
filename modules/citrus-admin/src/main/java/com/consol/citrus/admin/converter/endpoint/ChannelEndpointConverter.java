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

package com.consol.citrus.admin.converter.endpoint;

import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.ChannelEndpointDefinition;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class ChannelEndpointConverter extends AbstractEndpointConverter<ChannelEndpointDefinition> {

    @Override
    public EndpointData convert(ChannelEndpointDefinition definition) {
        EndpointData endpointData = new EndpointData(getEndpointType(), definition.getId(), getModelClass());

        if (StringUtils.hasText(definition.getChannelName())) {
            endpointData.add(property("channelName", "Channel", definition));
        } else {
            endpointData.add(property("channel", definition));
        }

        endpointData.add(property("messagingTemplate", definition)
                .optionKey(MessagingTemplate.class.getName()));
        endpointData.add(property("channelResolver", definition)
                .optionKey(DestinationResolver.class.getName()));

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<ChannelEndpointDefinition> getModelClass() {
        return ChannelEndpointDefinition.class;
    }

    @Override
    public String getEndpointType() {
        return "channel";
    }
}
