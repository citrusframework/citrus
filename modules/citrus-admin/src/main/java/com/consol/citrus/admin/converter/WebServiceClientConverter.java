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
import com.consol.citrus.model.config.ws.Client;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class WebServiceClientConverter implements EndpointConverter<Client> {

    @Override
    public EndpointData convert(Client client) {
        EndpointData endpointData = new EndpointData();

        endpointData.setName(client.getId());
        endpointData.setDestination(client.getRequestUrl());
        endpointData.setType("soap-client");

        return endpointData;
    }

    @Override
    public Class<Client> getModelClass() {
        return Client.class;
    }
}
