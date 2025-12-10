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
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.camel.CamelActionBuilderBase;
import org.citrusframework.camel.CamelTestActor;
import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public abstract class AbstractCamelAction extends AbstractTestAction implements ReferenceResolverAware {

    /** Target Camel context */
    protected final CamelContext camelContext;
    protected ReferenceResolver referenceResolver;

    protected AbstractCamelAction(String name, Builder<?, ?> builder) {
        super(name.startsWith("camel:") ? name : "camel:" + name, builder);

        this.camelContext = builder.camelContext;
        this.referenceResolver = builder.referenceResolver;
    }

    /**
     * Gets the target camel context.
     * @return
     */
    public CamelContext getCamelContext() {
        if (camelContext == null) {
            if (referenceResolver != null && referenceResolver.isResolvable(CamelContext.class)) {
                return referenceResolver.resolve(CamelContext.class);
            }
        }

        return camelContext;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends AbstractCamelAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B>
            implements ReferenceResolverAware, CamelActionBuilderBase<T, B> {

        protected ReferenceResolver referenceResolver;
        protected CamelContext camelContext;
        protected String camelContextName;

        public Builder() {
            actor(new CamelTestActor());
        }

        /**
         * Sets the Camel context.
         */
        public B context(CamelContext camelContext) {
            this.camelContext = camelContext;
            return self;
        }

        @Override
        public B context(String context) {
            this.camelContextName = context;
            return self;
        }

        @Override
        public B context(Object o) {
            if (o instanceof CamelContext context) {
                this.camelContext = context;
            } else {
                throw new CitrusRuntimeException("Expected a CamelContext, but got %s".formatted(o.getClass().getName()));
            }

            return self;
        }

        @Override
        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public final T build() {
            if (camelContext == null) {
                ObjectHelper.assertNotNull(referenceResolver, "Insufficient Camel action configuration - " +
                        "either set Camel context or proper reference resolver!");

                if (camelContextName != null) {
                    camelContext = referenceResolver.resolve(camelContextName, CamelContext.class);
                } else {
                    camelContext = CamelUtils.resolveCamelContext(referenceResolver, null);
                }
            }

            if (referenceResolver == null) {
                referenceResolver = new CamelReferenceResolver(camelContext);
            } else if (!(referenceResolver instanceof CamelReferenceResolver)) {
                this.referenceResolver = new CamelReferenceResolver(camelContext).withFallback(referenceResolver);
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
            if (referenceResolver instanceof CamelReferenceResolver || camelContext == null) {
                this.referenceResolver = referenceResolver;
            } else {
                this.referenceResolver = new CamelReferenceResolver(camelContext).withFallback(referenceResolver);
            }
        }
    }
}
