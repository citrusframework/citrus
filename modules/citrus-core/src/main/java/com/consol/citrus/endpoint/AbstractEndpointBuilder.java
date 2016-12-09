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
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

    /**
     * Initializes the endpoint.
     * @return
     */
    public AbstractEndpointBuilder<T> initialize() {
        if (getEndpoint() instanceof InitializingBean) {
            try {
                ((InitializingBean) getEndpoint()).afterPropertiesSet();
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to initialize endpoint", e);
            }
        }

        return this;
    }

    /**
     * Sets the Spring application context.
     * @param applicationContext
     * @return
     */
    public AbstractEndpointBuilder<T> applicationContext(ApplicationContext applicationContext) {
        if (getEndpoint() instanceof ApplicationContextAware) {
            ((ApplicationContextAware) getEndpoint()).setApplicationContext(applicationContext);
        }

        if (getEndpoint() instanceof BeanFactoryAware) {
            ((BeanFactoryAware) getEndpoint()).setBeanFactory(applicationContext);
        }

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
        return getEndpoint();
    }

    /**
     * Gets the target endpoint instance.
     * @return
     */
    protected abstract T getEndpoint();
}
