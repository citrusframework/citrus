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
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.model.config.ws.Client;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class WebServiceClientConverter extends AbstractEndpointConverter<Client> {

    @Override
    public EndpointData convert(Client client) {
        EndpointData endpointData = new EndpointData("soap");

        endpointData.setName(client.getId());
        endpointData.add(property("requestUrl", client));
        endpointData.add(property("webServiceTemplate", client));
        endpointData.add(property("messageFactory", client));
        endpointData.add(property("messageSender", client));
        endpointData.add(property("messageSenders", client));
        endpointData.add(property("messageCorrelator", client));
        endpointData.add(property("interceptors", client));
        endpointData.add(property("endpointResolver", client));
        endpointData.add(property("addressingHeaders", client));
        endpointData.add(property("faultStrategy", client, ErrorHandlingStrategy.THROWS_EXCEPTION.name()));
        endpointData.add(property("pollingInterval", client));

        addEndpointProperties(endpointData, client);

        return endpointData;
    }

    @Override
    public Class<Client> getModelClass() {
        return Client.class;
    }
}
