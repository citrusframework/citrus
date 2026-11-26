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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.camel.dsl.CamelSupport.camel;

/**
 * Watches a running Camel integration process in the background and adds an exception
 * to the test context when the integration exits unexpectedly (non-zero exit code).
 * The watcher does not block or interrupt the test thread.
 * <p>
 * When {@link #autoRemove} is enabled (the default), an {@link CamelUnwatchIntegrationAction}
 * is automatically registered as a finally-action so the watcher is cleaned up at the end of the test.
 */
public class CamelWatchIntegrationAction extends AbstractCamelCliAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelWatchIntegrationAction.class);

    private final String integrationName;
    private final boolean autoRemove;

    public CamelWatchIntegrationAction(Builder builder) {
        super("watch", builder);

        this.integrationName = builder.integrationName;
        this.autoRemove = builder.autoRemove;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        String processKeyPrefix = "%s:%s".formatted(name, CamelRunTestVariables.PROCESS.suffix());
        String pidKey = "%s:%s".formatted(name, CamelRunTestVariables.PID.suffix());

        if (!context.getVariables().containsKey(pidKey)) {
            throw new CitrusRuntimeException(
                    "Cannot watch integration '%s' - no process found in test context. Make sure the integration has been started first.".formatted(name));
        }

        Long pid = context.getVariable(pidKey, Long.class);
        String processKey = "%s:%d".formatted(processKeyPrefix, pid);

        if (!context.getVariables().containsKey(processKey)) {
            throw new CitrusRuntimeException(
                    "Cannot watch integration '%s' - process handle not found in test context.".formatted(name));
        }

        ProcessAndOutput pao = context.getVariable(processKey, ProcessAndOutput.class);

        logger.info("Watching Camel integration '%s' (pid %s) for unexpected exit".formatted(name, pid));

        CamelIntegrationExitWatcher watcher = CamelIntegrationExitWatcher.getOrCreate(context);
        watcher.watch(name, pao, context);

        if (autoRemove) {
            context.doFinally(camel()
                    .cli()
                    .unwatch()
                    .integration(name));
        }
    }

    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelCliAction.Builder<CamelWatchIntegrationAction, Builder> {

        private String integrationName;
        private boolean autoRemove = true;

        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        public Builder autoRemove(boolean enabled) {
            this.autoRemove = enabled;
            return this;
        }

        @Override
        public CamelWatchIntegrationAction doBuild() {
            return new CamelWatchIntegrationAction(this);
        }
    }
}
