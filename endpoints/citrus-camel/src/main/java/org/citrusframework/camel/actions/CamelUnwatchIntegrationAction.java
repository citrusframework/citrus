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

import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes the exit watcher for a Camel integration process and cancels the underlying
 * completable future so that a subsequent process exit does not add an exception to the test context.
 */
public class CamelUnwatchIntegrationAction extends AbstractCamelCliAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelUnwatchIntegrationAction.class);

    private final String integrationName;

    public CamelUnwatchIntegrationAction(Builder builder) {
        super("unwatch", builder);

        this.integrationName = builder.integrationName;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        if (!context.getVariables().containsKey(CamelIntegrationExitWatcher.CONTEXT_VARIABLE)) {
            logger.debug("No exit watcher found in test context - nothing to unwatch for integration '%s'".formatted(name));
            return;
        }

        CamelIntegrationExitWatcher watcher = context.getVariable(
                CamelIntegrationExitWatcher.CONTEXT_VARIABLE, CamelIntegrationExitWatcher.class);

        if (name.equals("*")) {
            logger.info("Unwatching all Camel integrations");
            watcher.unwatchAll();
        } else {
            logger.info("Unwatching Camel integration '%s'".formatted(name));
            watcher.unwatch(name);
        }
    }

    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelCliAction.Builder<CamelUnwatchIntegrationAction, Builder> {

        private String integrationName = "*";

        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public CamelUnwatchIntegrationAction doBuild() {
            return new CamelUnwatchIntegrationAction(this);
        }
    }
}
