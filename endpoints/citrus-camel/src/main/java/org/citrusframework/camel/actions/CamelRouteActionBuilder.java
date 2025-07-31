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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builder.
 */
public class CamelRouteActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelRouteAction>
        implements org.citrusframework.actions.camel.CamelRouteActionBuilder<AbstractCamelRouteAction, CamelRouteActionBuilder> {

    private CamelContext camelContext;
    private String camelContextName;

    /**
     * Processor calling given Camel route as part of the message processing.
     */
    public CamelRouteProcessor.Builder processor() {
        return CamelRouteProcessor.Builder.route()
                .camelContext(resolveCamelContext());
    }

    /**
     * Sets the Camel context to use.
     */
    public CamelRouteActionBuilder context(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    @Override
    public CamelRouteActionBuilder context(String camelContext) {
        this.camelContextName = camelContext;
        return this;
    }

    @Override
    public CamelRouteActionBuilder context(Object o) {
        if (o instanceof CamelContext context) {
            this.camelContext = context;
        } else {
            throw new CitrusRuntimeException("Expected a CamelContext, but got %s".formatted(o.getClass().getName()));
        }

        return this;
    }

    /**
     * Creates new Camel routes in route builder.
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

    @Override
    public CreateCamelRouteAction.Builder create() {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(camelContext);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CreateCamelRouteAction.Builder create(Object o) {
        if (o instanceof RouteBuilder routeBuilder) {
            return create(routeBuilder);
        } else {
            throw new CitrusRuntimeException("Expected a RouteBuilder, but got %s".formatted(o.getClass().getName()));
        }
    }

    @Override
    public CreateCamelRouteAction.Builder create(String routeSpec) {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .route(routeSpec);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelControlBusAction.Builder controlBus() {
        CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder()
                .context(camelContext);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StartCamelRouteAction.Builder start(String... routes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    @Override
    public StopCamelRouteAction.Builder stop(String... routes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    @Override
    public RemoveCamelRouteAction.Builder remove(String... routes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routes(routes);

        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelRouteActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public AbstractCamelRouteAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        resolveCamelContext();

        if (delegate instanceof AbstractCamelAction.Builder<?,?> contextAware) {
            contextAware.context(camelContext);
        }

        return delegate.build();
    }

    private CamelContext resolveCamelContext() {
        if (camelContext == null) {
            ObjectHelper.assertNotNull(referenceResolver, "Insufficient Camel action configuration - " +
                    "either set Camel context or proper reference resolver!");

            if (camelContextName != null) {
                camelContext = referenceResolver.resolve(camelContextName, CamelContext.class);
            } else {
                camelContext = CamelUtils.resolveCamelContext(referenceResolver, null);
            }
        }

        return camelContext;
    }
}
