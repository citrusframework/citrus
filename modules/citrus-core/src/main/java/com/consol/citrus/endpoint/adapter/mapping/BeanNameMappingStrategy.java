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

package com.consol.citrus.endpoint.adapter.mapping;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Endpoint adapter mapping strategy uses Spring application context and tries to find appropriate Spring bean in
 * context for the mapping key. Bean id or name has to match the given mapping key and bean must be of type
 * {@link com.consol.citrus.endpoint.EndpointAdapter}
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class BeanNameMappingStrategy implements EndpointAdapterMappingStrategy, ApplicationContextAware {

    /** Application context holding available endpoint adapters */
    protected ApplicationContext applicationContext;

    @Override
    public EndpointAdapter getEndpointAdapter(String mappingKey) {
        try {
            return applicationContext.getBean(mappingKey, EndpointAdapter.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find matching endpoint adapter with bean name '" +
                    mappingKey + "' in Spring bean application context", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
