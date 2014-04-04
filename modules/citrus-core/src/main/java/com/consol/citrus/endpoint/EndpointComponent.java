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

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContextAware;

/**
 * Endpoint component registers with name in Spring application context. Can create endpoint instance from
 * dynamic endpoint uri. Creates endpoint instance on the fly from endpoint uri.
 *
 * @author Christoph Deppisch
 */
public interface EndpointComponent extends BeanNameAware, ApplicationContextAware {

    /**
     * Creates proper endpoint instance from endpoint uri.
     * @param endpointUri
     * @return
     */
    Endpoint createEndpoint(String endpointUri);

    /**
     * Gets the name of this endpoint component.
     * @return
     */
    String getName();

    /**
     * Sets the endpoint component name.
     * @param name
     */
    void setName(String name);
}
