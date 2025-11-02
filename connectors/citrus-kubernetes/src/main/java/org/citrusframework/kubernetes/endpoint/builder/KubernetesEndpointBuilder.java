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

package org.citrusframework.kubernetes.endpoint.builder;

import jakarta.annotation.Nullable;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.client.KubernetesClientBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class KubernetesEndpointBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private EndpointBuilder<?> delegate;
    private ReferenceResolver referenceResolver;

    @SchemaProperty(description = "Sets the Kubernetes client endpoint.")
    public void setClient(KubernetesClientBuilder client) {
        this.delegate = client;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Client/server endpoint builder has not been initialized");
        }

        if (referenceResolver != null && delegate instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return delegate.supports(endpointType);
    }

    @Override
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
