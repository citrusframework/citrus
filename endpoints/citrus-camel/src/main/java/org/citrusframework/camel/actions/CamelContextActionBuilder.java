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

/**
 * Action builder.
 */
public class CamelContextActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelAction>
        implements org.citrusframework.actions.camel.CamelContextActionBuilder<AbstractCamelAction, CamelContextActionBuilder> {

    @Override
    public CreateCamelContextAction.Builder create() {
        CreateCamelContextAction.Builder builder = new CreateCamelContextAction.Builder();
        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CreateCamelContextAction.Builder create(String contextName) {
        CreateCamelContextAction.Builder builder = new CreateCamelContextAction.Builder()
                .contextName(contextName);

        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StartCamelContextAction.Builder start() {
        StartCamelContextAction.Builder builder = new StartCamelContextAction.Builder();
        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StartCamelContextAction.Builder start(String contextName) {
        StartCamelContextAction.Builder builder = new StartCamelContextAction.Builder()
                .contextName(contextName);

        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StopCamelContextAction.Builder stop() {
        StopCamelContextAction.Builder builder = new StopCamelContextAction.Builder();
        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StopCamelContextAction.Builder stop(String contextName) {
        StopCamelContextAction.Builder builder = new StopCamelContextAction.Builder()
                .contextName(contextName);

        builder.setReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelContextActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }
}
