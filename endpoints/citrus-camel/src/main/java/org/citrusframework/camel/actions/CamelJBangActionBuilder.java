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
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builder.
 */
public class CamelJBangActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelJBangAction>
        implements org.citrusframework.actions.camel.CamelJBangActionBuilder<AbstractCamelJBangAction, CamelJBangActionBuilder> {

    private String camelVersion;
    private String kameletsVersion;

    @Override
    public CamelJBangActionBuilder camelVersion(String camelVersion) {
        this.camelVersion = camelVersion;
        return this;
    }

    @Override
    public CamelJBangActionBuilder kameletsVersion(String kameletsVersion) {
        this.kameletsVersion = kameletsVersion;
        return this;
    }

    @Override
    public CamelRunIntegrationAction.Builder run() {
        CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelVerifyIntegrationAction.Builder verify() {
        CamelVerifyIntegrationAction.Builder builder = new CamelVerifyIntegrationAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelRunIntegrationAction.Builder run(String name, String sourceCode) {
        CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder()
                .integrationName(name)
                .integration(sourceCode);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelCustomAction.Builder custom(String... commands) {
        CamelCustomAction.Builder builder = new CamelCustomAction.Builder()
                .commands(commands);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelStopIntegrationAction.Builder stop() {
        CamelStopIntegrationAction.Builder builder = new CamelStopIntegrationAction.Builder();

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelPluginActionBuilder plugin() {
        CamelPluginActionBuilder builder = new CamelPluginActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelCmdActionBuilder cmd() {
        CamelCmdActionBuilder builder = new CamelCmdActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelKubernetesPluginActionBuilder kubernetes() {
        CamelKubernetesPluginActionBuilder builder = new CamelKubernetesPluginActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelJBangActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelJBangAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        if (delegate instanceof AbstractCamelJBangAction.Builder<?,?> camelJBangActionBuilder) {
            if (camelVersion != null) {
                camelJBangActionBuilder.camelVersion(camelVersion);
            }

            if (kameletsVersion != null) {
                camelJBangActionBuilder.kameletsVersion(kameletsVersion);
            }
        }

        return delegate.build();
    }
}
