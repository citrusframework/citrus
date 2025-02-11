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

package org.citrusframework.camel.actions;

import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

public class CamelKubernetesPluginActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelJBangAction> {

    /**
     * Build and deploy a Camel project to Kubernetes
     */
    public CamelKubernetesRunIntegrationAction.Builder run() {
        CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Delete a deployed Camel application using the previous exported Kubernetes manifest.
     */
    public CamelKubernetesDeleteAction.Builder delete() {
        CamelKubernetesDeleteAction.Builder builder = new CamelKubernetesDeleteAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Verify the Kubernetes pod status and logs of a deployed Camel integration.
     */
    public CamelKubernetesVerifyAction.Builder verify() {
        CamelKubernetesVerifyAction.Builder builder = new CamelKubernetesVerifyAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Export a Camel project from given Camel integration.
     */
    public CamelKubernetesRunIntegrationAction.Builder export() {
        CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public CamelKubernetesPluginActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelJBangAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

}
