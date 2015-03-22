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
import org.springframework.beans.factory.BeanNameAware;

import java.util.Map;

/**
 * Endpoint component registers with bean name in Spring application context and is then responsible to create proper endpoints dynamically from
 * endpoint uri values. Creates endpoint instance by parsing the dynamic endpoint uri with special properties and parameters. Creates proper endpoint
 * configuration instance on the fly.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public interface EndpointComponent extends BeanNameAware {

    /**
     * Creates proper endpoint instance from endpoint uri.
     * @param endpointUri
     * @param context
     * @return
     */
    Endpoint createEndpoint(String endpointUri, TestContext context);

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

    /**
     * Construct endpoint name from endpoint uri.
     * @param endpointUri
     * @return
     */
    Map<String, String> getParameters(String endpointUri);
}
