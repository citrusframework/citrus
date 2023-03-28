/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.endpoint.builder;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public abstract class AbstractEndpointBuilder<B extends EndpointBuilder<? extends Endpoint>> {

    protected final B builder;

    /**
     * Default constructor using browser builder implementation.
     * @param builder
     */
    public AbstractEndpointBuilder(B builder) {
        this.builder = builder;
    }
}
