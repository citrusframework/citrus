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

package com.consol.citrus.admin.converter;

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
        EndpointData endpointData = new EndpointData("soap-client");

        endpointData.setName(client.getId());
        add("requestUrl", endpointData, client);
        add("webServiceTemplate", endpointData, client);
        add("messageFactory", endpointData, client);
        add("messageSender", endpointData, client);
        add("messageSenders", endpointData, client);
        add("messageCorrelator", endpointData, client);
        add("interceptors", endpointData, client);
        add("endpointResolver", endpointData, client);
        add("addressingHeaders", endpointData, client);
        add("faultStrategy", endpointData, client, ErrorHandlingStrategy.THROWS_EXCEPTION.name());
        add("pollingInterval", endpointData, client);

        addEndpointProperties(endpointData, client);

        return endpointData;
    }

    @Override
    public Class<Client> getModelClass() {
        return Client.class;
    }
}
