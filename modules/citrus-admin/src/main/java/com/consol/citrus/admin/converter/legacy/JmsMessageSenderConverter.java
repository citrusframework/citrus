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

import com.consol.citrus.admin.converter.AbstractEndpointConverter;
import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.JmsMessageSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class JmsMessageSenderConverter extends AbstractEndpointConverter<JmsMessageSender> {

    @Override
    public EndpointData convert(JmsMessageSender definition) {
        EndpointData endpointData = new EndpointData("jms-sender");

        endpointData.setName(definition.getId());

        if (StringUtils.hasText(definition.getDestinationName())) {
            endpointData.add("destination", definition.getDestinationName());
        } else {
            endpointData.add("destination", definition.getDestination());
        }

        add("connectionFactory", endpointData, definition);
        add("jmsTemplate", endpointData, definition);
        add("pubSubDomain", endpointData, definition, "false");

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<JmsMessageSender> getModelClass() {
        return JmsMessageSender.class;
    }
}
