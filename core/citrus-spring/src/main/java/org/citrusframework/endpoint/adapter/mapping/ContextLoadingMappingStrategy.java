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

package org.citrusframework.endpoint.adapter.mapping;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ObjectHelper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Endpoint adapter mapping strategy loads new Spring Application contexts defined by one or more locations
 * and tries to find matching Spring bean with given name or id.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ContextLoadingMappingStrategy implements EndpointAdapterMappingStrategy {

    /** Application context configuration location holding available endpoint adapters */
    protected String contextConfigLocation;

    /** Should application context be loaded once or with every mapping call */
    protected boolean loadOnce = true;

    /** Cached application context */
    private ApplicationContext applicationContext;

    @Override
    public EndpointAdapter getEndpointAdapter(String mappingKey) {
        ObjectHelper.assertNotNull(contextConfigLocation, "Spring bean application context location must be set properly");

        ApplicationContext ctx;
        if (loadOnce) {
            if (applicationContext == null) {
                applicationContext = createApplicationContext();
            }

            ctx = applicationContext;
        } else {
            ctx = createApplicationContext();
        }

        try {
            return ctx.getBean(mappingKey, EndpointAdapter.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find matching endpoint adapter with bean name '" +
                    mappingKey + "' in Spring bean application context", e);
        }
    }

    /**
     * Creates a new Spring application context using the context config location. Create classpath
     * or file system application context.
     * @return
     */
    private ApplicationContext createApplicationContext() {
        if (contextConfigLocation.startsWith("classpath")) {
            return new ClassPathXmlApplicationContext(contextConfigLocation);
        } else {
            return new FileSystemXmlApplicationContext(contextConfigLocation);
        }
    }

    /**
     * Sets the context config location for building the Spring application context.
     * @param contextConfigLocation
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }
}
