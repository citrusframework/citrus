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
import com.consol.citrus.util.TypeConversionUtils;
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
            Method propertyMethod = ReflectionUtils.findMethod(this.getClass(), endpointProperty.name());
            if (propertyMethod != null) {
                ReflectionUtils.invokeMethod(propertyMethod, this, TypeConversionUtils.convertStringToType(endpointProperty.value(), endpointProperty.type()));
            }
        }

        return build();
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
