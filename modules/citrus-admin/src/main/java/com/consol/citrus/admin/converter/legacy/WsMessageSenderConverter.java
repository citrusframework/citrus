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
        EndpointData endpointData = new EndpointData("ws-sender");

        endpointData.setName(definition.getId());
        add("requestUrl", endpointData, definition);
        add("webServiceTemplate", endpointData, definition);
        add("messageFactory", endpointData, definition);
        add("messageSender", endpointData, definition);
        add("messageSenders", endpointData, definition);
        add("interceptors", endpointData, definition);
        add("endpointResolver", endpointData, definition);
        add("addressingHeaders", endpointData, definition);
        add("faultStrategy", endpointData, definition, ErrorHandlingStrategy.THROWS_EXCEPTION.name());

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<MessageSender> getModelClass() {
        return MessageSender.class;
    }
}
