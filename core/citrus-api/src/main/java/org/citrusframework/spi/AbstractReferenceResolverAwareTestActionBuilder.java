package org.citrusframework.spi;

import jakarta.annotation.Nullable;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public abstract class AbstractReferenceResolverAwareTestActionBuilder<T extends TestAction> implements TestActionBuilder.DelegatingTestActionBuilder<T>, ReferenceResolverAware {

    /** Bean reference resolver */
    protected ReferenceResolver referenceResolver;

    protected TestActionBuilder<? extends T> delegate;

    @Override
    public TestActionBuilder<? extends T> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     */
    @Override
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        if (referenceResolver != null) {
            this.referenceResolver = referenceResolver;

            if (delegate instanceof ReferenceResolverAware referenceResolverAware) {
                referenceResolverAware.setReferenceResolver(referenceResolver);
            }
        }
    }
}
