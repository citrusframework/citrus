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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.endpoint.adapter.mapping.EndpointAdapterMappingStrategy;
import org.citrusframework.endpoint.adapter.mapping.MappingKeyExtractor;
import org.citrusframework.message.Message;

/**
 * Base endpoint adapter implementation that dispatches incoming messages according to some extracted message value as mapping key and
 * a endpoint adapter mapping. Once adapter mapping identified proper endpoint adapter implementation the incoming request is forwarded
 * to this adapter for further processing steps.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class RequestDispatchingEndpointAdapter extends StaticEndpointAdapter {

    /** Extracts message value predicate for dispatching */
    private MappingKeyExtractor mappingKeyExtractor;

    /** Endpoint adapter mapping strategy */
    private EndpointAdapterMappingStrategy mappingStrategy;

    @Override
    protected Message handleMessageInternal(Message message) {
        return dispatchMessage(message, mappingKeyExtractor.extractMappingKey(message));
    }

    /**
     * Consolidate mapping strategy in order to find dispatch incoming request to endpoint adapter according
     * to mapping key that was extracted before from message content.
     * @param request
     * @param mappingKey
     * @return
     */
    public Message dispatchMessage(Message request, String mappingKey) {
        return mappingStrategy.getEndpointAdapter(mappingKey).handleMessage(request);
    }

    /**
     * Gets the mapping name extractor.
     * @return
     */
    public MappingKeyExtractor getMappingKeyExtractor() {
        return mappingKeyExtractor;
    }

    /**
     * Sets the mapping name extractor implementation.
     * @param mappingKeyExtractor
     */
    public void setMappingKeyExtractor(MappingKeyExtractor mappingKeyExtractor) {
        this.mappingKeyExtractor = mappingKeyExtractor;
    }

    /**
     * Gets the endpoint adapter mapping strategy.
     * @return
     */
    public EndpointAdapterMappingStrategy getMappingStrategy() {
        return mappingStrategy;
    }

    /**
     * Sets the endpoint adapter mapping strategy.
     * @param mappingStrategy
     */
    public void setMappingStrategy(EndpointAdapterMappingStrategy mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }
}
