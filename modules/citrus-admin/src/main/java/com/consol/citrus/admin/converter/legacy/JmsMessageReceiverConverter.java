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

import com.consol.citrus.admin.converter.AbstractEndpointConverter;
import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.JmsMessageReceiver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class JmsMessageReceiverConverter extends AbstractEndpointConverter<JmsMessageReceiver> {
    @Override
    public EndpointData convert(JmsMessageReceiver definition) {
        EndpointData endpointData = new EndpointData("jms-receiver");

        endpointData.setName(definition.getId());

        if (StringUtils.hasText(definition.getDestinationName())) {
            endpointData.add("destination", resolvePropertyExpression(definition.getDestinationName()));
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
    public Class<JmsMessageReceiver> getModelClass() {
        return JmsMessageReceiver.class;
    }
}
