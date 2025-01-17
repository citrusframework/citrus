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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.xml.bind.JAXBException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dsl.yaml.YamlRoutesBuilderLoader;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.xml.CamelRouteContextFactoryBean;
import org.apache.camel.support.ResourceHelper;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.dsl.GroovySupport;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.4
 */
public class CreateCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateCamelRouteAction.class);

    /** Camel route */
    private final List<RouteDefinition> routes;

    /** Route id */
    private final String routeId;

    /** Route specification using one of the supported languages XML or Groovy */
    private final String routeSpec;

    /**
     * Default constructor.
     */
    public CreateCamelRouteAction(Builder builder) {
        super("create-routes", builder);

        this.routes = builder.routes;
        this.routeId = builder.routeId;
        this.routeSpec = builder.routeSpec;
    }

    @Override
    public void doExecute(TestContext context) {
        final List<RouteDefinition> routesToUse = new ArrayList<>();
        RouteBuilder routeBuilder = null;

        if (StringUtils.hasText(routeSpec)) {
            if (IsXmlPredicate.getInstance().test(routeSpec)) {
                String routeContext = createRouteContext(routeId, context.replaceDynamicContentInString(routeSpec.trim()));

                // now let's parse the routes with JAXB
                try {
                    Object value = CamelUtils.getJaxbContext().createUnmarshaller().unmarshal(new StringSource(routeContext));
                    if (value instanceof CamelRouteContextFactoryBean factoryBean) {
                        routesToUse.addAll(factoryBean.getRoutes());
                    } else {
                        throw new CitrusRuntimeException(String.format("Failed to parse routes from given route context - expected %s but found %s",
                                CamelRouteContextFactoryBean.class, value.getClass()));
                    }
                } catch (JAXBException e) {
                    throw new CitrusRuntimeException("Failed to create the JAXB unmarshaller", e);
                }
            } else if (IsYamlRoutePredicate.getInstance().test(routeSpec)) {
                try (YamlRoutesBuilderLoader routesBuilderLoader = new YamlRoutesBuilderLoader()) {
                    routesBuilderLoader.setCamelContext(camelContext);
                    routesBuilderLoader.loadRoutesBuilder(ResourceHelper.fromString(routeId + "camel.yaml", routeSpec))
                            .addRoutesToCamelContext(camelContext);
                } catch (Exception e) {
                    throw new CitrusRuntimeException("Failed to load YAML route via routes loader", e);
                }
            } else {
                routeBuilder = new RouteBuilder(camelContext) {
                    @Override
                    public void configure() throws Exception {
                        new GroovySupport()
                                .withTestContext(context)
                                .withDelegate(this)
                                .load(routeSpec, "org.apache.camel.*");
                    }

                    @Override
                    protected void configureRoute(RouteDefinition route) {
                        if (routeId != null) {
                            route.routeId(routeId);
                        }
                    }
                };
            }
        }

        if (routes != null) {
            routesToUse.addAll(routes);
        }

        try {
            if (routeBuilder != null) {
                camelContext.addRoutes(routeBuilder);
            }

            if (!routesToUse.isEmpty()) {
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
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to create route definitions in context '%s'", camelContext.getName()), e);
        }
    }

    private String createRouteContext(String routeId, String routeSpec) {
        final String routeContextElement = "<routeContext xmlns=\"http://camel.apache.org/schema/spring\">%s</routeContext>";

        if (routeSpec.startsWith("<?xml")) {
            // cut off XML declaration and rerun the method logic
            return createRouteContext(routeId, routeSpec.substring(routeSpec.indexOf("?>") + 2).trim());
        } else if (routeSpec.startsWith("<route id=")) {
            return String.format(routeContextElement, routeSpec);
        } else if (routeSpec.startsWith("<route>") && routeId != null) {
            return String.format(routeContextElement, String.format("<route id=\"%s\">", routeId) + routeSpec.substring("<route>".length()));
        } else if (routeSpec.startsWith("<route>")) {
            return String.format(routeContextElement, routeSpec);
        } else if (routeSpec.startsWith("<routeContext>")) {
            return String.format(routeContextElement, routeSpec.substring("<routeContext>".length(), routeSpec.length() - "</routeContext>".length()));
        } else if (routeSpec.startsWith("<routeContext")) {
            return routeSpec;
        } else if (routeId != null) {
            return String.format(routeContextElement, String.format("<route id=\"%s\">", routeId) + routeSpec + "</route>");
        } else {
            return String.format(routeContextElement, "<route>" + routeSpec + "</route>");
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
     * Gets the routeSpec.
     * @return
     */
    public String getRouteSpec() {
        return routeSpec;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelRouteAction.Builder<CreateCamelRouteAction, Builder> {

        private final List<RouteDefinition> routes = new ArrayList<>();
        private String routeId;
        private String routeSpec;

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
         * Adds route using one of the supported languages XML or Groovy.
         * @param routeSpec
         * @return
         */
        @Deprecated
        public Builder routeContext(String routeSpec) {
            this.routeSpec = routeSpec;
            return this;
        }

        /**
         * Adds route using one of the supported languages XML or Groovy.
         * @param routeSpec
         * @return
         */
        public Builder route(String routeSpec) {
            this.routeSpec = routeSpec;
            return this;
        }

        /**
         * Adds route using the content of the given resource.
         * The file name is used as a route id.
         * @param routeResource
         * @return
         */
        public Builder route(Resource routeResource) {
            try {
                this.routeSpec = FileUtils.readToString(routeResource);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read Camel route from file resource", e);
            }

            if (routeId == null) {
                this.routeId = FileUtils.getBaseName(FileUtils.getFileName(routeResource.getLocation()));
            }

            return this;
        }

        /**
         * Adds route definition.
         * @param route
         * @return
         */
        public Builder route(RouteDefinition route) {
            this.routes.add(route);
            return this;
        }

        /**
         * Adds route using one of the supported languages XML or Groovy.
         * @param routeId
         * @param routeSpec
         * @return
         */
        public Builder route(String routeId, String routeSpec) {
            this.routeId = routeId;
            this.routeSpec = routeSpec;
            return this;
        }

        /**
         * Sets the route id.
         * @param id
         * @return
         */
        public Builder routeId(String id) {
            this.routeId = id;
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

    private static class IsYamlRoutePredicate implements Predicate<String> {

        private static final IsYamlRoutePredicate INSTANCE = new IsYamlRoutePredicate();

        private IsYamlRoutePredicate() {
            // Singleton
        }

        public static IsYamlRoutePredicate getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean test(String toTest) {
            if (toTest == null) {
                return false;
            }

            return toTest.trim().startsWith("- route:") || toTest.trim().startsWith("- from:");
        }
    }
}
