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
public class RemoveCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(RemoveCamelRouteAction.class);

    /**
     * Default constructor.
     */
    public RemoveCamelRouteAction(Builder builder) {
        super("remove-routes", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        for (String routeId : routeIds) {
            String route = context.replaceDynamicContentInString(routeId);

            try {
                if (camelContext instanceof AbstractCamelContext
                        && !((AbstractCamelContext) camelContext).getRouteStatus(route).isStopped()) {
                    throw new CitrusRuntimeException("Camel routes must be stopped before removal!");
                }

                if (camelContext.removeRoute(route)) {
                    logger.info(String.format("Removed Camel route '%s' from context '%s'", route, camelContext.getName()));
                } else {
                    throw new CitrusRuntimeException(String.format("Failed to remove Camel route '%s' from context '%s'", route, camelContext.getName()));
                }
            } catch (CitrusRuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new CitrusRuntimeException(String.format("Failed to remove Camel route '%s' from context '%s'", route, camelContext.getName()), e);
            }
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelRouteAction.Builder<RemoveCamelRouteAction, Builder> {
        @Override
        public RemoveCamelRouteAction doBuild() {
            return new RemoveCamelRouteAction(this);
        }
    }
}
