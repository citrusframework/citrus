/*
 * Copyright 2006-2016 the original author or authors.
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

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusEndpointProperty;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class AbstractEndpointBuilder<T extends Endpoint> implements EndpointBuilder<T> {

    /**
     * Sets the endpoint name.
     * @param endpointName
     * @return
     */
    public AbstractEndpointBuilder<T> name(String endpointName) {
        getEndpoint().setName(endpointName);
        return this;
    }

    /**
     * Sets the endpoint actor.
     * @param actor
     * @return
     */
    public AbstractEndpointBuilder<T> actor(TestActor actor) {
        getEndpoint().setActor(actor);
        return this;
    }

    @Override
    public T build(CitrusEndpoint endpointAnnotation) {
        ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(this.getClass(), "name"), this, endpointAnnotation.name());

        for (CitrusEndpointProperty endpointProperty : endpointAnnotation.properties()) {
            Method propertyMethod = ReflectionUtils.findMethod(this.getClass(), "name");
            if (propertyMethod != null) {
                ReflectionUtils.invokeMethod(propertyMethod, this, getTypedParameterValue(endpointProperty.type(), endpointProperty.value()));
            }
        }

        return build();
    }

    /**
     * Convert parameter value string to required type from setter method argument.
     * @param fieldType
     * @param value
     * @return
     */
    private Object getTypedParameterValue(Class<?> fieldType, String value) {
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
        }

        throw new CitrusRuntimeException(String.format("Unable to convert parameter '%s' to required type '%s'", value, fieldType.getName()));
    }

    @Override
    public T build() {
        T endpoint = getEndpoint();

        if (endpoint instanceof InitializingBean) {
            try {
                ((InitializingBean) endpoint).afterPropertiesSet();
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to build endpoint", e);
            }
        }

        return endpoint;
    }

    /**
     * Gets the target endpoint instance.
     * @return
     */
    protected abstract T getEndpoint();
}
