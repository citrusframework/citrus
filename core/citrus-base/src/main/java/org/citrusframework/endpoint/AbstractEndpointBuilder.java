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

package org.citrusframework.endpoint;

import org.citrusframework.TestActor;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

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
        if (getEndpoint() instanceof InitializingPhase) {
            try {
                ((InitializingPhase) getEndpoint()).initialize();
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to initialize server", e);
            }
        }

        return this;
    }

    /**
     * Sets the reference resolver.
     * @param referenceResolver
     * @return
     */
    public AbstractEndpointBuilder<T> referenceResolver(ReferenceResolver referenceResolver) {
        if (getEndpoint() instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) getEndpoint()).setReferenceResolver(referenceResolver);
        }

        return this;
    }

    @Override
    public T build() {
        return getEndpoint();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return getEndpoint().getClass().equals(endpointType);
    }

    /**
     * Gets the target endpoint instance.
     * @return
     */
    protected abstract T getEndpoint();
}
