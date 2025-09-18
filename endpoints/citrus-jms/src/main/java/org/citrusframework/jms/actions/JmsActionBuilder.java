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

package org.citrusframework.jms.actions;

import jakarta.annotation.Nullable;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public class JmsActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>,
        ReferenceResolverAware, org.citrusframework.actions.jms.JmsActionBuilder<TestAction, JmsActionBuilder> {

    private TestActionBuilder<? extends TestAction> delegate;

    private ReferenceResolver referenceResolver;

    /**
     * Fluent API action building entry method used in Java DSL.
     */
    public static JmsActionBuilder jms() {
        return new JmsActionBuilder();
    }

    @Override
    public PurgeJmsQueuesAction.Builder purgeQueues() {
        PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (referenceResolver != null &&
                delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public JmsActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    @Override
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        if (referenceResolver != null &&
                delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
            this.referenceResolver = referenceResolver;
        }
    }
}
