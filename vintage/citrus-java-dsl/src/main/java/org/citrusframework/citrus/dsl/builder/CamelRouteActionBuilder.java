package org.citrusframework.citrus.dsl.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.camel.actions.AbstractCamelRouteAction;
import org.citrusframework.citrus.camel.actions.CamelControlBusAction;
import org.citrusframework.citrus.camel.actions.CreateCamelRouteAction;
import org.citrusframework.citrus.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.citrus.camel.actions.StartCamelRouteAction;
import org.citrusframework.citrus.camel.actions.StopCamelRouteAction;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

/**
 * @author Christoph Deppisch
 */
public class CamelRouteActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<AbstractCamelRouteAction>, ReferenceResolverAware {

    private final org.citrusframework.citrus.camel.actions.CamelRouteActionBuilder delegate = new org.citrusframework.citrus.camel.actions.CamelRouteActionBuilder();

    private final List<String> routeIds = new ArrayList<>();

    public CamelRouteActionBuilder context(String camelContext) {
        delegate.context(camelContext);
        return this;
    }

    public CamelRouteActionBuilder context(CamelContext camelContext) {
        delegate.context(camelContext);
        return this;
    }

    public CamelRouteActionBuilder routes(String... routeIds) {
        return routes(Arrays.asList(routeIds));
    }

    public CamelRouteActionBuilder routes(List<String> routeIds) {
        this.routeIds.addAll(routeIds);
        return this;
    }

    public CreateCamelRouteAction.Builder create(RouteBuilder routeBuilder) {
        return delegate.create(routeBuilder);
    }

    public CreateCamelRouteAction.Builder create(String routeContext) {
        return delegate.create(routeContext);
    }

    public CamelControlBusAction.Builder controlBus() {
        return delegate.controlBus();
    }

    public StartCamelRouteAction.Builder start(String ... routes) {
        return delegate.start(routes)
                .routeIds(routeIds);
    }

    public StopCamelRouteAction.Builder stop(String ... routes) {
        return delegate.stop(routes)
                .routeIds(routeIds);
    }

    public RemoveCamelRouteAction.Builder remove(String ... routes) {
        return delegate.remove(routes)
                .routeIds(routeIds);
    }

    public CamelRouteActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public AbstractCamelRouteAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
