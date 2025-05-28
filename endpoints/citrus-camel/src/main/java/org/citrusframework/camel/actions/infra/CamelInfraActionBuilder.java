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

package org.citrusframework.camel.actions.infra;

import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public class CamelInfraActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelAction> {

    /**
     * Runs an available Camel infra service.
     */
    public CamelRunInfraAction.Builder run() {
        CamelRunInfraAction.Builder builder = new CamelRunInfraAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Stops a running Camel infra instance.
     */
    public CamelStopInfraAction.Builder stop() {
        CamelStopInfraAction.Builder builder = new CamelStopInfraAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public CamelInfraActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }
}
