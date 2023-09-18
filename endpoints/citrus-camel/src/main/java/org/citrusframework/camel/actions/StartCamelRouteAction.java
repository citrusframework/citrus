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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class StartCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StartCamelRouteAction.class);

    /**
     * Default constructor.
     */
    public StartCamelRouteAction(Builder builder) {
        super("start-routes", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        for (String routeId : routeIds) {
            String route = context.replaceDynamicContentInString(routeId);

            try {
                ((AbstractCamelContext) camelContext).startRoute(route);
                logger.info(String.format("Started Camel route '%s' on context '%s'", route, camelContext.getName()));
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to start Camel route: " + route, e);
            }
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelRouteAction.Builder<StartCamelRouteAction, Builder> {
        @Override
        public StartCamelRouteAction doBuild() {
            return new StartCamelRouteAction(this);
        }
    }
}
