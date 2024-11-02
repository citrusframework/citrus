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
import org.apache.camel.model.ModelCamelContext;
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builder.
 */
public class CamelRouteActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<AbstractCamelRouteAction> {

    private CamelContext camelContext;

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
     * Creates new Camel routes from route specification using one of the supported languages.
     * @param routeSpec
     * @return
     */
    public CreateCamelRouteAction.Builder create(String routeSpec) {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .route(routeSpec);

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
}
