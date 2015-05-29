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
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.*;
import com.consol.citrus.model.config.ws.WebServiceClientDefinition;
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
public class WebServiceClientConverter extends AbstractEndpointConverter<WebServiceClientDefinition> {

    @Override
    public EndpointData convert(WebServiceClientDefinition client) {
        EndpointData endpointData = new EndpointData(getEndpointType(), client.getId(), getModelClass());

        endpointData.add(property("requestUrl", client));
        endpointData.add(property("webServiceTemplate", client)
                .optionKey(WebServiceTemplate.class.getName()));
        endpointData.add(property("messageFactory", client)
                .optionKey(SoapMessageFactory.class.getName()));
        endpointData.add(property("messageSender", client));
        endpointData.add(property("messageSenders", client));
        endpointData.add(property("messageCorrelator", client)
                .optionKey(MessageCorrelator.class.getName()));
        endpointData.add(property("interceptors", client));
        endpointData.add(property("endpointResolver", client)
                .optionKey(EndpointUriResolver.class.getName()));
        endpointData.add(property("messageConverter", client)
                .optionKey(MessageConverter.class.getName()));
        endpointData.add(property("faultStrategy", client, ErrorHandlingStrategy.THROWS_EXCEPTION.name())
                .options(getErrorHandlingStrategyOptions()));
        endpointData.add(property("pollingInterval", client));

        addEndpointProperties(endpointData, client);

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
    public Class<WebServiceClientDefinition> getModelClass() {
        return WebServiceClientDefinition.class;
    }

    @Override
    public String getEndpointType() {
        return "ws-client";
    }
}
