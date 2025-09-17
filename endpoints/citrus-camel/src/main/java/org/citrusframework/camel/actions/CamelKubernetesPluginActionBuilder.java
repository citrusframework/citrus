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

import org.citrusframework.actions.camel.CamelJBangKubernetesActionBuilder;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

public class CamelKubernetesPluginActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelJBangAction>
        implements CamelJBangKubernetesActionBuilder<AbstractCamelJBangAction, CamelKubernetesPluginActionBuilder> {

    @Override
    public CamelKubernetesRunIntegrationAction.Builder run() {
        CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelKubernetesDeleteIntegrationAction.Builder delete() {
        CamelKubernetesDeleteIntegrationAction.Builder builder = new CamelKubernetesDeleteIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelKubernetesVerifyIntegrationAction.Builder verify() {
        CamelKubernetesVerifyIntegrationAction.Builder builder = new CamelKubernetesVerifyIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelKubernetesRunIntegrationAction.Builder export() {
        CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
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
