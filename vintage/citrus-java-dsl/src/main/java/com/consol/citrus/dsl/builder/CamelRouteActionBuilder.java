package com.consol.citrus.dsl.builder;

import java.util.Arrays;
import java.util.List;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.camel.actions.AbstractCamelRouteAction;
import com.consol.citrus.camel.actions.CamelControlBusAction;
import com.consol.citrus.camel.actions.CreateCamelRouteAction;
import com.consol.citrus.camel.actions.RemoveCamelRouteAction;
import com.consol.citrus.camel.actions.StartCamelRouteAction;
import com.consol.citrus.camel.actions.StopCamelRouteAction;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

/**
 * @author Christoph Deppisch
 */
public class CamelRouteActionBuilder implements TestActionBuilder<AbstractCamelRouteAction>, ReferenceResolverAware {

    private final com.consol.citrus.camel.actions.CamelRouteActionBuilder delegate = new com.consol.citrus.camel.actions.CamelRouteActionBuilder();


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
        delegate.routes(routeIds);
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
        return delegate.start(routes);
    }

    public StopCamelRouteAction.Builder stop(String ... routes) {
        return delegate.stop(routes);
    }

    public RemoveCamelRouteAction.Builder remove(String ... routes) {
        return delegate.remove(routes);
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
}
