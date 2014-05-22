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

import com.consol.citrus.admin.converter.EndpointConverter;
import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.JmsMessageSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class JmsMessageSenderConverter implements EndpointConverter<JmsMessageSender> {

    @Override
    public EndpointData convert(JmsMessageSender jmsMessageSender) {
        EndpointData endpointData = new EndpointData();

        endpointData.setName(jmsMessageSender.getId());

        if (StringUtils.hasText(jmsMessageSender.getDestinationName())) {
            endpointData.setDestination(jmsMessageSender.getDestinationName());
        } else {
            endpointData.setDestination("ref:" + jmsMessageSender.getDestination());
        }

        endpointData.setType("jms-sender");

        return endpointData;
    }

    @Override
    public Class<JmsMessageSender> getModelClass() {
        return JmsMessageSender.class;
    }
}
