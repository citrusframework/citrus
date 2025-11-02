/*
 * Copyright the original author or authors.
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
import org.citrusframework.common.Named;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public abstract class AbstractEndpointBuilder<T extends Endpoint> implements EndpointBuilder<T>, ReferenceResolverAware, Named {

    protected ReferenceResolver referenceResolver;

    /**
     * Sets the endpoint name.
     */
    public AbstractEndpointBuilder<T> name(String endpointName) {
        getEndpoint().setName(endpointName);
        return this;
    }

    @SchemaProperty(description = "The name of the endpoint")
    public void setName(String endpointName) {
        name(endpointName);
    }

    /**
     * Sets the endpoint actor.
     */
    public AbstractEndpointBuilder<T> actor(TestActor actor) {
        getEndpoint().setActor(actor);
        return this;
    }

    /**
     * Initializes the endpoint.
     */
    public AbstractEndpointBuilder<T> initialize() {
        if (getEndpoint() instanceof InitializingPhase initializingBean) {
            try {
                initializingBean.initialize();
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to initialize server", e);
            }
        }

        return this;
    }

    /**
     * Sets the reference resolver.
     */
    public AbstractEndpointBuilder<T> referenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public T build() {
        T endpoint = getEndpoint();
        if (endpoint instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return endpoint;
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return getEndpoint().getClass().equals(endpointType);
    }

    /**
     * Gets the target endpoint instance.
     */
    protected abstract T getEndpoint();
}
