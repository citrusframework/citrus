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
import com.consol.citrus.model.config.http.Client;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class HttpClientConverter extends AbstractEndpointConverter<Client> {

    @Override
    public EndpointData convert(Client client) {
        EndpointData endpointData = new EndpointData("http-client");

        endpointData.setName(client.getId());
        add("requestUrl", endpointData, client);
        add("requestMethod", endpointData, client, HttpMethod.POST.name());
        add("errorStrategy", endpointData, client, ErrorHandlingStrategy.PROPAGATE.getName());
        add("pollingInterval", endpointData, client, "500");
        add("messageCorrelator", endpointData, client);
        add("requestFactory", endpointData, client);
        add("restTemplate", endpointData, client);
        add("charset", endpointData, client);
        add("contentType", endpointData, client);
        add("interceptors", endpointData, client);

        addEndpointProperties(endpointData, client);

        return endpointData;
    }

    @Override
    public Class<Client> getModelClass() {
        return Client.class;
    }
}
