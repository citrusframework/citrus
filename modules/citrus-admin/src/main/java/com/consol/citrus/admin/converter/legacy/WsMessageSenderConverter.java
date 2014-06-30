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
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.model.config.ws.MessageSender;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class WsMessageSenderConverter extends AbstractEndpointConverter<MessageSender> {

    @Override
    public EndpointData convert(MessageSender definition) {
        EndpointData endpointData = new EndpointData(getEndpointType(), definition.getId(), getModelClass());

        endpointData.add(property("requestUrl", definition));
        endpointData.add(property("webServiceTemplate", definition)
                .optionKey(WebServiceTemplate.class.getName()));
        endpointData.add(property("messageFactory", definition)
                .optionKey(SoapMessageFactory.class.getName()));
        endpointData.add(property("messageSender", definition));
        endpointData.add(property("messageSenders", definition));
        endpointData.add(property("replyMessageCorrelator", definition)
                .optionKey(ReplyMessageCorrelator.class.getName()));
        endpointData.add(property("interceptors", definition));
        endpointData.add(property("endpointResolver", definition)
                .optionKey(EndpointUriResolver.class.getName()));
        endpointData.add(property("addressingHeaders", definition));
        endpointData.add(property("faultStrategy", definition, ErrorHandlingStrategy.THROWS_EXCEPTION.name())
                .options(getErrorHandlingStrategyOptions()));

        endpointData.add(property("actor", "TestActor", definition));

        return endpointData;
    }

    /**
     * Gets the error handling strategy names as list.
     * @return
     */
    private List<String> getErrorHandlingStrategyOptions() {
        List<String> strategyNames = new ArrayList<String>();
        for (ErrorHandlingStrategy errorHandlingStrategy : ErrorHandlingStrategy.values()) {
            strategyNames.add(errorHandlingStrategy.getName());
        }
        return strategyNames;
    }

    @Override
    public Class<MessageSender> getModelClass() {
        return MessageSender.class;
    }
}
