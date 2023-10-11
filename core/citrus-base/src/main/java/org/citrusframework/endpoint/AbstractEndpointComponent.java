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

package org.citrusframework.endpoint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.TypeConversionUtils;
import org.citrusframework.util.StringUtils;

/**
 * Default endpoint component reads component name from endpoint uri and parses parameters from uri using
 * the HTTP uri pattern.
 *
 * http://localhost:8080?param1=value1&param2=value2&param3=value3
 * jms:queue.name?connectionFactory=specialConnectionFactory
 * soap:localhost:8080?soapAction=sayHello
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public abstract class AbstractEndpointComponent implements EndpointComponent {

    /** Component name usually the Spring bean id */
    private final String name;

    /**
     * Default constructor using the name for this component.
     * @param name
     */
    public AbstractEndpointComponent(String name) {
        this.name = name;
    }

    @Override
    public Endpoint createEndpoint(String endpointUri, TestContext context) {
        try {
            URI uri = new URI(endpointUri);
            String path = uri.getSchemeSpecificPart();

            if (path.startsWith("//")) {
                path = path.substring(2);
            }

            if (path.contains("?")) {
                path = path.substring(0, path.indexOf('?'));
            }

            Map<String, String> parameters = getParameters(endpointUri);
            String endpointName = null;
            if (parameters.containsKey(ENDPOINT_NAME)) {
                endpointName = parameters.remove(ENDPOINT_NAME);
            }

            Endpoint endpoint = createEndpoint(path, parameters, context);

            if (endpoint instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) endpoint).setReferenceResolver(context.getReferenceResolver());
            }

            if (StringUtils.hasText(endpointName)) {
                endpoint.setName(endpointName);
            }

            return endpoint;
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException(String.format("Unable to parse endpoint uri '%s'", endpointUri), e);
        }
    }

    @Override
    public Map<String, String> getParameters(String endpointUri) {
        Map<String, String> parameters = new LinkedHashMap<>();

        if (endpointUri.contains("?")) {
            String parameterString = endpointUri.substring(endpointUri.indexOf('?') + 1);

            StringTokenizer tok = new StringTokenizer(parameterString, "&");
            while (tok.hasMoreElements()) {
                String[] parameterValue = tok.nextToken().split("=");
                if (parameterValue.length == 1) {
                    parameters.put(parameterValue[0], null);
                } else if (parameterValue.length == 2) {
                    parameters.put(parameterValue[0], parameterValue[1]);
                } else {
                    throw new CitrusRuntimeException(String.format("Invalid parameter key/value combination '%s'", Arrays.toString(parameterValue)));
                }
            }
        }

        return parameters;
    }

    /**
     * Sets properties on endpoint configuration using method reflection.
     * @param endpointConfiguration
     * @param parameters
     * @param context
     */
    protected void enrichEndpointConfiguration(EndpointConfiguration endpointConfiguration, Map<String, String> parameters, TestContext context) {
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionHelper.findField(endpointConfiguration.getClass(), parameterEntry.getKey());

            if (field == null) {
                throw new CitrusRuntimeException(String.format("Unable to find parameter field on endpoint configuration '%s'", parameterEntry.getKey()));
            }

            Method setter = ReflectionHelper.findMethod(endpointConfiguration.getClass(), "set" + parameterEntry.getKey().substring(0, 1).toUpperCase() + parameterEntry.getKey().substring(1), field.getType());

            if (setter == null) {
                throw new CitrusRuntimeException(String.format("Unable to find parameter setter on endpoint configuration '%s'",
                        "set" + parameterEntry.getKey().substring(0, 1).toUpperCase() + parameterEntry.getKey().substring(1)));
            }

            if (parameterEntry.getValue() != null) {
                ReflectionHelper.invokeMethod(setter, endpointConfiguration, TypeConversionUtils.convertStringToType(parameterEntry.getValue(), field.getType(), context));
            } else {
                ReflectionHelper.invokeMethod(setter, endpointConfiguration, field.getType().cast(null));
            }
        }
    }

    /**
     * Removes non config parameters from list of endpoint parameters according to given endpoint configuration type. All
     * parameters that do not reside to a endpoint configuration setting are removed so the result is a qualified list
     * of endpoint configuration parameters.
     *
     * @param parameters
     * @param endpointConfigurationType
     * @return
     */
    protected Map<String, String> getEndpointConfigurationParameters(Map<String, String> parameters,
                                                                     Class<? extends EndpointConfiguration> endpointConfigurationType) {
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionHelper.findField(endpointConfigurationType, parameterEntry.getKey());

            if (field != null) {
                params.put(parameterEntry.getKey(), parameterEntry.getValue());
            }
        }

        return params;
    }

    /**
     * Filters non endpoint configuration parameters from parameter list and puts them
     * together as parameters string. According to given endpoint configuration type only non
     * endpoint configuration settings are added to parameter string.
     *
     * @param parameters
     * @param endpointConfigurationType
     * @return
     */
    protected String getParameterString(Map<String, String> parameters,
                                        Class<? extends EndpointConfiguration> endpointConfigurationType) {
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionHelper.findField(endpointConfigurationType, parameterEntry.getKey());

            if (field == null) {
                if (paramString.length() == 0) {
                    paramString.append("?").append(parameterEntry.getKey());
                    if (parameterEntry.getValue() != null) {
                        paramString.append("=").append(parameterEntry.getValue());
                    }
                } else {
                    paramString.append("&").append(parameterEntry.getKey());
                    if (parameterEntry.getValue() != null) {
                        paramString.append("=").append(parameterEntry.getValue());
                    }
                }
            }
        }

        return paramString.toString();
    }

    /**
     * Create endpoint instance from uri resource and parameters.
     * @param resourcePath
     * @param parameters
     * @param context
     * @return
     */
    protected abstract Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context);

    @Override
    public String getName() {
        return name;
    }
}
