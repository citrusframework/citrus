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
import com.consol.citrus.message.*;
import com.consol.citrus.model.config.http.HttpClientDefinition;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class HttpClientConverter extends AbstractEndpointConverter<HttpClientDefinition> {

    @Override
    public EndpointData convert(HttpClientDefinition client) {
        EndpointData endpointData = new EndpointData(getEndpointType(), client.getId(), getModelClass());

        endpointData.add(property("requestUrl", client));
        endpointData.add(property("requestMethod", client, HttpMethod.POST.name())
                .options(getHttpMethodOptions()));
        endpointData.add(property("errorStrategy", client, ErrorHandlingStrategy.PROPAGATE.getName())
                .options(getErrorHandlingStrategyOptions()));
        endpointData.add(property("pollingInterval", client, "500"));
        endpointData.add(property("messageCorrelator", client)
                .optionKey(MessageCorrelator.class.getName()));
        endpointData.add(property("messageConverter", client)
                .optionKey(MessageConverter.class.getName()));
        endpointData.add(property("requestFactory", client)
                .optionKey(ClientHttpRequestFactory.class.getName()));
        endpointData.add(property("restTemplate", client)
                .optionKey(RestTemplate.class.getName()));
        endpointData.add(property("charset", client));
        endpointData.add(property("contentType", client));
        endpointData.add(property("interceptors", client));

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

    /**
     * Gets the available Http request method names as list.
     * @return
     */
    private List<String> getHttpMethodOptions() {
        List<String> methodNames = new ArrayList<String>();
        for (HttpMethod method : HttpMethod.values()) {
            methodNames.add(method.name());
        }
        return methodNames;
    }

    @Override
    public Class<HttpClientDefinition> getModelClass() {
        return HttpClientDefinition.class;
    }

    @Override
    public String getEndpointType() {
        return "http-client";
    }
}
