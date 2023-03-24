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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Endpoint adapter mapping strategy simply holds a map of mapping keys and adapter instances. Searches for available mapping key
 * and returns respective adapter implementation.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class SimpleMappingStrategy implements EndpointAdapterMappingStrategy {

    /** Simple map holds mapping key and endpoint adapters */
    private Map<String, EndpointAdapter> adapterMappings = new HashMap<String, EndpointAdapter>();

    @Override
    public EndpointAdapter getEndpointAdapter(String mappingKey) {
        if (adapterMappings.containsKey(mappingKey)) {
            return adapterMappings.get(mappingKey);
        } else {
            throw new CitrusRuntimeException("Unable to find matching endpoint adapter with mapping key '" + mappingKey + "'");
        }
    }

    /**
     * Sets the endpoint adapter mappings.
     * @param mappings
     */
    public void setAdapterMappings(Map<String, EndpointAdapter> mappings) {
        this.adapterMappings = mappings;
    }
}
