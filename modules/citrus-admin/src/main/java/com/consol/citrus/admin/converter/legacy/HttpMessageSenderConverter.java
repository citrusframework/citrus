/*
 * Copyright 2006-2014 the original author or authors.
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
import com.consol.citrus.model.config.http.MessageSender;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class HttpMessageSenderConverter extends AbstractEndpointConverter<MessageSender> {

    @Override
    public EndpointData convert(MessageSender definition) {
        EndpointData endpointData = new EndpointData(getEndpointType(), definition.getId());

        endpointData.add(property("requestUrl", definition));
        endpointData.add(property("requestMethod", definition, HttpMethod.POST.name())
                .options(getHttpMethodOptions()));
        endpointData.add(property("errorStrategy", definition, ErrorHandlingStrategy.PROPAGATE.getName())
                .options(getErrorHandlingStrategyOptions()));
        endpointData.add(property("replyMessageCorrelator", definition)
                .optionKey(ReplyMessageCorrelator.class.getName()));
        endpointData.add(property("endpointResolver", definition)
                .optionKey(EndpointUriResolver.class.getName()));
        endpointData.add(property("requestFactory", definition)
                .optionKey(ClientHttpRequestFactory.class.getName()));
        endpointData.add(property("restTemplate", definition)
                .optionKey(RestTemplate.class.getName()));
        endpointData.add(property("charset", definition));
        endpointData.add(property("contentType", definition));
        endpointData.add(property("interceptors", definition));

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
    public Class<MessageSender> getModelClass() {
        return MessageSender.class;
    }
}
