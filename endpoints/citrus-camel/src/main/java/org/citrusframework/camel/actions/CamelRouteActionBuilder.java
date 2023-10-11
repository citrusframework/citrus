package org.citrusframework.camel.actions;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builder.
 */
public class CamelRouteActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<AbstractCamelRouteAction>, ReferenceResolverAware {

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    private CamelContext camelContext;

    private TestActionBuilder<? extends AbstractCamelRouteAction> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static CamelRouteActionBuilder camel() {
        return new CamelRouteActionBuilder();
    }

    /**
     * Processor calling given Camel route as part of the message processing.
     * @return
     */
    public CamelRouteProcessor.Builder processor() {
        return CamelRouteProcessor.Builder.route()
                .camelContext(camelContext);
    }

    /**
     * Sets the Camel context to use.
     * @param camelContext
     * @return
     */
    public CamelRouteActionBuilder context(String camelContext) {
        ObjectHelper.assertNotNull(referenceResolver, "Citrus bean reference resolver is not initialized!");
        this.camelContext = referenceResolver.resolve(camelContext, ModelCamelContext.class);
        return this;
    }

    /**
     * Sets the Camel context to use.
     * @param camelContext
     * @return
     */
    public CamelRouteActionBuilder context(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    /**
     * Creates new Camel routes in route builder.
     * @param routeBuilder
     * @return
     */
    public CreateCamelRouteAction.Builder create(RouteBuilder routeBuilder) {
        if (camelContext == null) {
            context(routeBuilder.getContext());
        }

        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .route(routeBuilder);

        this.delegate = builder;
        return builder;
    }

    /**
     * Creates new Camel routes from route context XML.
     * @param routeContext
     * @return
     */
    public CreateCamelRouteAction.Builder create(String routeContext) {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routeContext(routeContext);

        this.delegate = builder;
        return builder;
    }

    /**
     * Execute control bus Camel operations.
     * @return
     */
    public CamelControlBusAction.Builder controlBus() {
        CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder()
                .context(camelContext);

        this.delegate = builder;
        return builder;
    }

    /**
     * Start these Camel routes.
     */
    public StartCamelRouteAction.Builder start(String ... routes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    /**
     * Stop these Camel routes.
     */
    public StopCamelRouteAction.Builder stop(String ... routes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    /**
     * Remove these Camel routes.
     */
    public RemoveCamelRouteAction.Builder remove(String ... routes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public CamelRouteActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelRouteAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     * @param referenceResolver
     */
    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        if (referenceResolver == null) {
            this.referenceResolver = referenceResolver;

            if (delegate instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) delegate).setReferenceResolver(referenceResolver);
            }
        }
    }
}
