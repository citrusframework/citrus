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
import java.util.Arrays;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class AbstractCamelRouteAction extends AbstractTestAction {

    /** Target Camel context */
    protected final CamelContext camelContext;

    /** The Camel route to start */
    protected final List<String> routeIds;

    protected AbstractCamelRouteAction(String name, Builder<?, ?> builder) {
        super(name, builder);

        this.camelContext = builder.camelContext;
        this.routeIds = builder.routeIds;
    }

    /**
     * Gets the target camel context.
     * @return
     */
    public CamelContext getCamelContext() {
        return camelContext;
    }

    /**
     * Gets the Camel routes.
     * @return
     */
    public List<String> getRouteIds() {
        return routeIds;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends AbstractCamelRouteAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        protected ReferenceResolver referenceResolver;
        protected CamelContext camelContext;
        protected List<String> routeIds = new ArrayList<>();

        /**
         * Sets the Camel context.
         * @param camelContext
         * @return
         */
        public B context(CamelContext camelContext) {
            this.camelContext = camelContext;
            return self;
        }

        /**
         * Adds route ids.
         * @param routeIds
         * @return
         */
        public B routes(String... routeIds) {
            return routeIds(Arrays.asList(routeIds));
        }

        /**
         * Add list of route ids.
         * @param routeIds
         * @return
         */
        public B routeIds(List<String> routeIds) {
            this.routeIds.addAll(routeIds);
            return self;
        }

        @Override
        public final T build() {
            if (camelContext == null) {
                ObjectHelper.assertNotNull(referenceResolver, "Citrus bean reference resolver is not initialized!");

                if (referenceResolver.isResolvable("citrusCamelContext")) {
                    camelContext = referenceResolver.resolve("citrusCamelContext", ModelCamelContext.class);
                } else {
                    camelContext = referenceResolver.resolve(ModelCamelContext.class);
                }
            }

            return doBuild();
        }

        /**
         * Subclass builds action.
         * @return
         */
        protected abstract T doBuild();

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }

}
