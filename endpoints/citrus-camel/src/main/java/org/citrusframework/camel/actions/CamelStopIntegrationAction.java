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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops given Camel integration with Camel JBang tooling.
 */
public class CamelStopIntegrationAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelStopIntegrationAction.class);

    /** Route id */
    private final String integrationName;

    /**
     * Default constructor.
     */
    public CamelStopIntegrationAction(Builder builder) {
        super("stop-integration", builder);

        this.integrationName = builder.integrationName;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        if (name.equals("*")) {
            logger.info("Stopping all Camel integrations ...");

            camelJBang().camelApp().run("stop");

            logger.info("Stopped all Camel integrations");
        } else {
            logger.info("Stopping Camel integration '%s' ...".formatted(name));

            Long pid;
            if (context.getVariables().containsKey(name + ":pid")) {
                pid = context.getVariable(name + ":pid", Long.class);
            } else {
                pid = camelJBang().getAll().stream()
                        .filter(props -> name.equals(props.get("NAME")) && !props.getOrDefault("PID", "").isBlank())
                        .map(props -> Long.valueOf(props.get("PID"))).findFirst()
                        .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing process id for Camel integration %s:pid", name)));
            }

            camelJBang().stop(pid);

            if (context.getVariables().containsKey("%s:process:%d".formatted(name, pid))) {
                // check if process is still alive
                ProcessAndOutput pao = context.getVariable(name + ":process:" + pid, ProcessAndOutput.class);
                if (pao.getProcess().isAlive()) {
                    // Check if there is a descendant process to be stopped
                    List<Long> descendants = pao.getDescendants();
                    for (Long descendantPid : descendants) {
                        camelJBang().stop(descendantPid);
                        logger.info("Stopped Camel integration '%s' (%s - %s)".formatted(name, pid, descendantPid));
                    }
                }
            }

            logger.info("Stopped Camel integration '%s' (%s)".formatted(name, pid));
        }
    }

    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelStopIntegrationAction, Builder> {

        private String integrationName = "*";

        /**
         * Stop Camel JBang process for this integration.
         * @param name
         * @return
         */
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        /**
         * Sets the integration name.
         * @param name
         * @return
         */
        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public CamelStopIntegrationAction doBuild() {
            return new CamelStopIntegrationAction(this);
        }
    }
}
