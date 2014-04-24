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

package com.consol.citrus.http.client;

import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Component creates proper HTTP client endpoint from endpoint uri resource and parameters.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters) {
        HttpClient client = new HttpClient();

        client.getEndpointConfiguration().setRequestUrl("http://" + resourcePath + getParameterString(parameters));

        if (parameters.containsKey("requestMethod")) {
            String method = parameters.remove("requestMethod");
            client.getEndpointConfiguration().setRequestMethod(HttpMethod.valueOf(method));
        }

        if (parameters.containsKey("errorHandlingStrategy")) {
            String strategy = parameters.remove("errorHandlingStrategy");
            client.getEndpointConfiguration().setErrorHandlingStrategy(ErrorHandlingStrategy.fromName(strategy));
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(), getConfigParameters(parameters));
        return client;
    }

    /**
     * Removes non config parameters from list of endpoint parameters so the result is a qualified list
     * of endpoint configuration parameters.
     *
     * @param parameters
     * @return
     */
    private Map<String, String> getConfigParameters(Map<String, String> parameters) {
        Map<String, String> params = new HashMap<String, String>();

        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionUtils.findField(HttpEndpointConfiguration.class, parameterEntry.getKey());

            if (field != null) {
              params.put(parameterEntry.getKey(), parameterEntry.getValue());
            }
        }

        return params;
    }

    /**
     * Just reads non config parameters from endpoint parameters and put them together as HTTP parameters string.
     * @param parameters
     * @return
     */
    private String getParameterString(Map<String, String> parameters) {
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionUtils.findField(HttpEndpointConfiguration.class, parameterEntry.getKey());

            if (field == null) {
                if (paramString.toString().length() == 0) {
                    paramString.append("?").append(parameterEntry.getKey()).append("=").append(parameterEntry.getValue());
                } else {
                    paramString.append("&").append(parameterEntry.getKey()).append("=").append(parameterEntry.getValue());
                }
            }
        }

        return paramString.toString();
    }
}
