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
import org.citrusframework.spi.ReferenceResolver;

/**
 * Endpoint adapter mapping strategy uses bean reference resolver and tries to find appropriate endpoint adapter
 * for the given mapping key. Bean id or name has to match the given mapping key and bean must be of type
 * {@link org.citrusframework.endpoint.EndpointAdapter}
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class BeanNameMappingStrategy implements EndpointAdapterMappingStrategy {

    /** Bean reference resolver holding available endpoint adapters */
    protected final ReferenceResolver referenceResolver;

    /**
     * Default constructor initializes with reference resolver.
     * @param referenceResolver
     */
    public BeanNameMappingStrategy(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public EndpointAdapter getEndpointAdapter(String mappingKey) {
        return referenceResolver.resolve(mappingKey, EndpointAdapter.class);
    }
}
