package com.consol.citrus.camel.actions;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.camel.message.CamelRouteProcessor;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.springframework.util.Assert;

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
        Assert.notNull(referenceResolver, "Citrus bean reference resolver is not initialized!");
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
        if (getCamelContext() == null) {
            context(routeBuilder.getContext());
        }

        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(getCamelContext())
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
                .context(getCamelContext())
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
                .context(getCamelContext());

        this.delegate = builder;
        return builder;
    }

    /**
     * Start these Camel routes.
     */
    public StartCamelRouteAction.Builder start(String ... routes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder()
                .context(getCamelContext())
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    /**
     * Stop these Camel routes.
     */
    public StopCamelRouteAction.Builder stop(String ... routes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder()
                .context(getCamelContext())
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    /**
     * Remove these Camel routes.
     */
    public RemoveCamelRouteAction.Builder remove(String ... routes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder()
                .context(getCamelContext())
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

    /**
     * Gets the camel context either explicitly set before or default
     * context from Spring application context.
     * @return
     */
    protected CamelContext getCamelContext() {
        if (camelContext == null) {
            Assert.notNull(referenceResolver, "Citrus bean reference resolver is not initialized!");

            if (referenceResolver.isResolvable("citrusCamelContext")) {
                camelContext = referenceResolver.resolve("citrusCamelContext", ModelCamelContext.class);
            } else {
                camelContext = referenceResolver.resolve(ModelCamelContext.class);
            }
        }

        return camelContext;
    }

    @Override
    public AbstractCamelRouteAction build() {
        Assert.notNull(delegate, "Missing delegate action to build");
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
