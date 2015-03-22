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

package com.consol.citrus.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

    public static final String ENDPOINT_NAME = "endpointName";

    /** Component name usually the Spring bean id */
    private String name;

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
        Map<String, String> parameters = new LinkedHashMap<String, String>();

        if (endpointUri.contains("?")) {
            String parameterString = endpointUri.substring(endpointUri.indexOf('?') + 1);

            StringTokenizer tok = new StringTokenizer(parameterString, "&");
            while (tok.hasMoreElements()) {
                String[] parameterValue = tok.nextToken().split("=");
                if (parameterValue.length != 2) {
                    throw new CitrusRuntimeException(String.format("Invalid parameter key/value combination '%s'", parameterValue));
                }

                parameters.put(parameterValue[0], parameterValue[1]);
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
            Field field = ReflectionUtils.findField(endpointConfiguration.getClass(), parameterEntry.getKey());

            if (field == null) {
                throw new CitrusRuntimeException(String.format("Unable to find parameter field on endpoint configuration '%s'", parameterEntry.getKey()));
            }

            Method setter = ReflectionUtils.findMethod(endpointConfiguration.getClass(), "set" + parameterEntry.getKey().substring(0, 1).toUpperCase() + parameterEntry.getKey().substring(1), field.getType());

            if (setter == null) {
                throw new CitrusRuntimeException(String.format("Unable to find parameter setter on endpoint configuration '%s'",
                        "set" + parameterEntry.getKey().substring(0, 1).toUpperCase() + parameterEntry.getKey().substring(1)));
            }

            ReflectionUtils.invokeMethod(setter, endpointConfiguration, getTypedParameterValue(field.getType(), parameterEntry.getValue(), context));
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
        Map<String, String> params = new HashMap<String, String>();

        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            Field field = ReflectionUtils.findField(endpointConfigurationType, parameterEntry.getKey());

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
            Field field = ReflectionUtils.findField(endpointConfigurationType, parameterEntry.getKey());

            if (field == null) {
                if (paramString.length() == 0) {
                    paramString.append("?").append(parameterEntry.getKey()).append("=").append(parameterEntry.getValue());
                } else {
                    paramString.append("&").append(parameterEntry.getKey()).append("=").append(parameterEntry.getValue());
                }
            }
        }

        return paramString.toString();
    }

    /**
     * Convert parameter value string to required type from setter method argument.
     * @param fieldType
     * @param value
     * @param context
     * @return
     */
    private Object getTypedParameterValue(Class<?> fieldType, String value, TestContext context) {
        if (fieldType.isPrimitive()) {
            if (fieldType.isAssignableFrom(int.class)) {
                return Integer.valueOf(value).intValue();
            } else if (fieldType.isAssignableFrom(short.class)) {
                return Short.valueOf(value).shortValue();
            }  else if (fieldType.isAssignableFrom(byte.class)) {
                return Byte.valueOf(value).byteValue();
            } else if (fieldType.isAssignableFrom(long.class)) {
                return Long.valueOf(value).longValue();
            } else if (fieldType.isAssignableFrom(boolean.class)) {
                return Boolean.valueOf(value).booleanValue();
            } else if (fieldType.isAssignableFrom(float.class)) {
                return Float.valueOf(value).floatValue();
            } else if (fieldType.isAssignableFrom(double.class)) {
                return Double.valueOf(value).doubleValue();
            }
        } else {
            if (fieldType.isAssignableFrom(String.class)) {
                return value;
            } else if (fieldType.isAssignableFrom(Integer.class)) {
                return Integer.valueOf(value);
            } else if (fieldType.isAssignableFrom(Short.class)) {
                return Short.valueOf(value);
            }  else if (fieldType.isAssignableFrom(Byte.class)) {
                return Byte.valueOf(value);
            }  else if (fieldType.isAssignableFrom(Long.class)) {
                return Long.valueOf(value);
            } else if (fieldType.isAssignableFrom(Boolean.class)) {
                return Boolean.valueOf(value);
            } else if (fieldType.isAssignableFrom(Float.class)) {
                return Float.valueOf(value);
            } else if (fieldType.isAssignableFrom(Double.class)) {
                return Double.valueOf(value);
            }

            // try to resolve bean in application context
            if (context.getApplicationContext() != null && context.getApplicationContext().containsBean(value)) {
                Object bean = context.getApplicationContext().getBean(value);
                if (fieldType.isAssignableFrom(bean.getClass())) {
                    return bean;
                }
            }
        }

        throw new CitrusRuntimeException(String.format("Unable to convert parameter '%s' to required type '%s'", value, fieldType.getName()));
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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

}
