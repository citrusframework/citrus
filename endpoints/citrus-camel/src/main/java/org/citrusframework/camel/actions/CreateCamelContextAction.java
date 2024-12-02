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
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ReferenceResolver;

/**
 * Creates a new Camel context and binds it to the reference resolver registry.
 */
public class CreateCamelContextAction  extends AbstractCamelAction {

    private final String contextName;
    private final boolean autoStart;

    protected CreateCamelContextAction(Builder builder) {
        super("create-context", builder);

        this.contextName = builder.contextName;
        this.autoStart = builder.autoStart;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            CamelContext camelContext = new DefaultCamelContext();
            ReferenceResolver referenceResolver = context.getReferenceResolver();

            if (referenceResolver instanceof CamelReferenceResolver camelReferenceResolver) {
                camelReferenceResolver.setCamelContext(camelContext);
                camelReferenceResolver.getFallback().bind(contextName, camelContext);
            } else {
                referenceResolver.bind(contextName, camelContext);
                context.setReferenceResolver(new CamelReferenceResolver(camelContext)
                        .withFallback(referenceResolver));
            }

            if (autoStart) {
                camelContext.start();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Camel context", e);
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelAction.Builder<CreateCamelContextAction, Builder> {

        private String contextName = CamelSettings.getContextName();
        private boolean autoStart = true;

        /**
         * Static entry method for the fluent API.
         * @return
         */
        public static Builder createCamelContext() {
            return new Builder();
        }

        public Builder contextName(String name) {
            this.contextName = name;
            return this;
        }

        public Builder autoStart(boolean autoStart) {
            this.autoStart = autoStart;
            return this;
        }

        @Override
        public CreateCamelContextAction doBuild() {
            return new CreateCamelContextAction(this);
        }
    }
}
