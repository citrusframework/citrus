package org.citrusframework.spi;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface ReferenceResolverAware {

    /**
     * Sets the reference resolver.
     * @param referenceResolver
     */
    void setReferenceResolver(ReferenceResolver referenceResolver);
}
