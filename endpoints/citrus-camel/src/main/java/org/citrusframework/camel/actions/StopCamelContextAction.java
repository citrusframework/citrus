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
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopCamelContextAction extends AbstractCamelAction {

    private final String contextName;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StopCamelContextAction.class);

    /**
     * Default constructor.
     */
    public StopCamelContextAction(Builder builder) {
        super("stop-context", builder);

        this.contextName = builder.contextName;
    }

    @Override
    public void doExecute(TestContext context) {
        CamelContext camelContext;
        if (referenceResolver != null && referenceResolver.isResolvable(contextName, CamelContext.class)) {
            camelContext = referenceResolver.resolve(contextName, CamelContext.class);
        } else if (context.getReferenceResolver().isResolvable(contextName, CamelContext.class)) {
            camelContext = context.getReferenceResolver().resolve(contextName, CamelContext.class);
        } else {
            throw new CitrusRuntimeException("Unable to resolve Camel context '%s'".formatted(contextName));
        }

        try {
            camelContext.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop Camel context '%s'".formatted(contextName), e);
        }

        logger.info("Stopped Camel context '%s'".formatted(contextName));
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelAction.Builder<StopCamelContextAction, Builder> {

        private String contextName = CamelSettings.getContextName();

        public Builder contextName(String name) {
            this.contextName = name;
            return this;
        }

        @Override
        public StopCamelContextAction doBuild() {
            return new StopCamelContextAction(this);
        }
    }
}
