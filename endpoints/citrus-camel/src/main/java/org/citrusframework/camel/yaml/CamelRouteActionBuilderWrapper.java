package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.AbstractCamelRouteAction;

/**
 * Special wrapper holding a Camel route action builder for future reference.
 * @param <B>
 */
public interface CamelRouteActionBuilderWrapper<B extends AbstractCamelRouteAction.Builder<?, ?>> {

    /**
     * Gets the wrapped Camel route action builder.
     * @return
     */
    B getBuilder();

}
