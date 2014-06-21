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
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.model.config.ws.MessageSender;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class WsMessageSenderConverter extends AbstractEndpointConverter<MessageSender> {

    @Override
    public EndpointData convert(MessageSender definition) {
        EndpointData endpointData = new EndpointData("ws");

        endpointData.setName(definition.getId());
        endpointData.add(property("requestUrl", definition));
        endpointData.add(property("webServiceTemplate", definition));
        endpointData.add(property("messageFactory", definition));
        endpointData.add(property("messageSender", definition));
        endpointData.add(property("messageSenders", definition));
        endpointData.add(property("interceptors", definition));
        endpointData.add(property("endpointResolver", definition));
        endpointData.add(property("addressingHeaders", definition));
        endpointData.add(property("faultStrategy", definition, ErrorHandlingStrategy.THROWS_EXCEPTION.name()));

        endpointData.add(property("actor", "TestActor", definition));

        return endpointData;
    }

    @Override
    public Class<MessageSender> getModelClass() {
        return MessageSender.class;
    }
}
