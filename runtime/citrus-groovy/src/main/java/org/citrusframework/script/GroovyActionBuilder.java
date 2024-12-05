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

package org.citrusframework.script;

import jakarta.annotation.Nullable;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public class GroovyActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> delegate;

    private GroovyActionBuilder() {
        // hide constructor of builder class - use static entrance method instead
    }

    public static GroovyActionBuilder groovy() {
        return new GroovyActionBuilder();
    }

    /**
     * Run Groovy script.
     * @return
     */
    public GroovyAction.Builder run() {
        GroovyAction.Builder builder = new GroovyAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Create endpoints from Groovy script.
     * @return
     */
    public CreateEndpointsAction.Builder endpoints() {
        CreateEndpointsAction.Builder builder = new CreateEndpointsAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Create beans in registry from Groovy script.
     * @return
     */
    public CreateBeansAction.Builder beans() {
        CreateBeansAction.Builder builder = new CreateBeansAction.Builder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public GroovyActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     */
    @Override
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        if (referenceResolver != null
                && delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }
    }
}
