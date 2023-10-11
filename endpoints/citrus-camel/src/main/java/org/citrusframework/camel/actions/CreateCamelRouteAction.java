/*
 * Copyright 2006-2015 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.xml.CamelRouteContextFactoryBean;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CreateCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateCamelRouteAction.class);

    /** Camel route */
    private final List<RouteDefinition> routes;

    /** Route context as XML */
    private final String routeContext;

    /**
     * Default constructor.
     */
    public CreateCamelRouteAction(Builder builder) {
        super("create-routes", builder);

        this.routes = builder.routes;
        this.routeContext = builder.routeContext;
    }

    @Override
    public void doExecute(TestContext context) {
        final List<RouteDefinition> routesToUse;

        if (StringUtils.hasText(routeContext)) {
            // now let's parse the routes with JAXB
            try {
                Object value = CamelUtils.getJaxbContext().createUnmarshaller().unmarshal(new StringSource(context.replaceDynamicContentInString(routeContext)));
                if (value instanceof CamelRouteContextFactoryBean) {
                    CamelRouteContextFactoryBean factoryBean = (CamelRouteContextFactoryBean) value;
                    routesToUse = factoryBean.getRoutes();
                } else {
                    throw new CitrusRuntimeException(String.format("Failed to parse routes from given route context - expected %s but found %s",
                            CamelRouteContextFactoryBean.class, value.getClass()));
                }
            } catch (JAXBException e) {
                throw new CitrusRuntimeException("Failed to create the JAXB unmarshaller", e);
            }
        } else {
            routesToUse = routes;
        }

        try {
            camelContext.addRoutes(new RouteBuilder(camelContext) {
                @Override
                public void configure() throws Exception {
                    for (RouteDefinition routeDefinition : routesToUse) {
                        try {
                            getRouteCollection().getRoutes().add(routeDefinition);
                            logger.info(String.format("Created new Camel route '%s' in context '%s'", routeDefinition.getId(), camelContext.getName()));
                        } catch (Exception e) {
                            throw new CitrusRuntimeException(String.format("Failed to create route definition '%s' in context '%s'", routeDefinition.getId(), camelContext.getName()), e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to create route definitions in context '%s'", camelContext.getName()), e);
        }
    }

    /**
     * Gets the route definitions.
     * @return
     */
    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    /**
     * Gets the routeContext.
     *
     * @return
     */
    public String getRouteContext() {
        return routeContext;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelRouteAction.Builder<CreateCamelRouteAction, Builder> {

        private final List<RouteDefinition> routes = new ArrayList<>();
        private String routeContext;

        public Builder route(RouteBuilder routeBuilder) {
            try {
                if (routeBuilder.getContext() != null
                        && !routeBuilder.getContext().equals(camelContext)) {
                    routeBuilder.configureRoutes(camelContext);
                } else {
                    routeBuilder.configure();
                }

                routes(routeBuilder.getRouteCollection().getRoutes());
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to configure route definitions with camel context", e);
            }

            return this;
        }

        /**
         * Adds route definitions from route context XML.
         * @param routeContext
         * @return
         */
        public Builder routeContext(String routeContext) {
            this.routeContext = routeContext;
            return this;
        }

        /**
         * Adds route definitions.
         * @param routes
         * @return
         */
        public Builder routes(List<RouteDefinition> routes) {
            this.routes.addAll(routes);
            return this;
        }

        @Override
        public CreateCamelRouteAction doBuild() {
            return new CreateCamelRouteAction(this);
        }
    }
}
