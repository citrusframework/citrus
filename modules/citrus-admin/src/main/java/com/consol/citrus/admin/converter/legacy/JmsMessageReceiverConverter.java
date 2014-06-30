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
import com.consol.citrus.model.config.core.JmsMessageReceiver;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.jms.ConnectionFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class JmsMessageReceiverConverter extends AbstractEndpointConverter<JmsMessageReceiver> {
    @Override
    public EndpointData convert(JmsMessageReceiver definition) {
        EndpointData endpointData = new EndpointData(getEndpointType(), definition.getId(), getModelClass());

        if (StringUtils.hasText(definition.getDestinationName())) {
            endpointData.add(property("destinationName", "Destination", definition));
        } else {
            endpointData.add(property("destination", definition));
        }

        endpointData.add(property("connectionFactory", definition)
                .optionKey(ConnectionFactory.class.getName()));
        endpointData.add(property("jmsTemplate", definition)
                .optionKey(JmsTemplate.class.getName()));
        endpointData.add(property("pubSubDomain", definition, "false")
                .options("true", "false"));

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<JmsMessageReceiver> getModelClass() {
        return JmsMessageReceiver.class;
    }

    @Override
    public String getEndpointType() {
        return "jms";
    }
}
