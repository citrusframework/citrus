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

import java.util.Map;
import java.util.Objects;

import org.citrusframework.camel.CamelSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies Camel integration via Camel JBang. Checks route for running/stopped state and optionally waits for a log message to be present.
 * Raises errors when either the Camel integration is not in expected state or the log message is not available.
 * Both operations are automatically retried for a given amount of attempts.
 */
public class CamelVerifyIntegrationAction extends AbstractCamelJBangAction {

    private static final Logger INTEGRATION_STATUS_LOG = LoggerFactory.getLogger("INTEGRATION_STATUS");
    private static final Logger INTEGRATION_LOG = LoggerFactory.getLogger("INTEGRATION_LOGS");

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelVerifyIntegrationAction.class);

    private final String integrationName;

    private final String logMessage;
    private final int maxAttempts;
    private final long delayBetweenAttempts;

    private final String phase;
    private final boolean printLogs;

    private final boolean stopOnErrorStatus;

    /**
     * Default constructor.
     */
    public CamelVerifyIntegrationAction(Builder builder) {
        super("verify-integration", builder);

        this.integrationName = builder.integrationName;
        this.phase = builder.phase;
        this.logMessage = builder.logMessage;
        this.maxAttempts = builder.maxAttempts;
        this.delayBetweenAttempts = builder.delayBetweenAttempts;
        this.printLogs = builder.printLogs;
        this.stopOnErrorStatus = builder.stopOnErrorStatus;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        logger.info("Verify Camel integration '%s' ...".formatted(name));

        Long pid = verifyRouteStatus(name, context.replaceDynamicContentInString(phase), context);

        if (logMessage != null) {
            verifyRouteLogs(pid, name, context.replaceDynamicContentInString(logMessage.trim()), context);
        }

        logger.info("Successfully verified Camel integration '%s'".formatted(name));
    }

    private void verifyRouteLogs(Long pid, String name, String message, TestContext context) {
        if (printLogs) {
            INTEGRATION_LOG.info(String.format("Waiting for Camel integration '%s' to log message", name));
        }

        String log;
        int offset = 0;

        ProcessAndOutput pao = context.getVariable(name + ":process:" + pid, ProcessAndOutput.class);
        for (int i = 0; i < maxAttempts; i++) {
            log = pao.getOutput();

            if (printLogs && (offset < log.length())) {
                INTEGRATION_LOG.info(log.substring(offset));
                offset = log.length();
            }

            if (log.contains(message)) {
                logger.info("Verified Camel integration logs - All values OK!");
                return;
            }

            if (!printLogs) {
                logger.warn(String.format("Waiting for Camel integration '%s' to log message - retry in %s ms", name, delayBetweenAttempts));
            }

            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Camel integration logs", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify Camel integration '%s' - " +
                        "has not printed message '%s' after %d attempts", name, message, maxAttempts)));
    }

    private Long verifyRouteStatus(String name, String phase, TestContext context) {
        INTEGRATION_STATUS_LOG.info(String.format("Waiting for Camel integration '%s' to be in state '%s'", name, phase));

        for (int i = 0; i < maxAttempts; i++) {
            if (context.getVariables().containsKey(name + ":pid")) {
                Long pid = context.getVariable(name + ":pid", Long.class);
                if (findProcessAndVerifyStatus(pid, name, phase)) {
                    return pid;
                }

                if (context.getVariables().containsKey(name + ":process:" + pid)) {
                    // check if process is still alive
                    ProcessAndOutput pao = context.getVariable(name + ":process:" + pid, ProcessAndOutput.class);
                    if (!pao.getProcess().isAlive()) {
                        logger.info("Failed to verify Camel integration '%s' - exit code %s".formatted(name, pao.getProcess().exitValue()));
                        logger.info(pao.getOutput());

                        throw new CitrusRuntimeException(String.format("Failed to verify Camel integration '%s' - exit code %s", name, pao.getProcess().exitValue()));
                    }

                    // Verify that current processId is the same as the one saved in test context
                    Long appPid = pao.getProcessId();
                    if (!Objects.equals(pid, appPid)) {
                        // seems like there is another pid (descendant process) that should be verified
                        if (findProcessAndVerifyStatus(appPid, name, phase)) {
                            return appPid;
                        }
                    }
                }
            }

            logger.info(System.lineSeparator() + camelJBang().ps());
            logger.info(String.format("Waiting for Camel integration '%s' to be in state '%s'- retry in %s ms", name, phase, delayBetweenAttempts));
            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Camel integration state", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify Camel integration '%s' - " +
                        "is not in state '%s' after %d attempts", name, phase, maxAttempts)));

    }

    private boolean findProcessAndVerifyStatus(Long pid, String name, String phase) {
        Map<String, String> properties = camelJBang().get(pid);
        if ((phase.equals("Stopped") && properties.isEmpty()) || (!properties.isEmpty() && properties.get("STATUS").equals(phase))) {
            logger.info(String.format("Verified Camel integration '%s' state '%s' - All values OK!", name, phase));
            return true;
        } else if (properties.getOrDefault("STATUS", "").equals("Error")) {
            logger.info(String.format("Camel integration '%s' is in state 'Error'", name));
            if (stopOnErrorStatus) {
                throw new CitrusRuntimeException(String.format("Failed to verify Camel integration '%s' - is in state 'Error'", name));
            }
        }

        return false;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelVerifyIntegrationAction, Builder> {

        private String integrationName = "route";

        private String logMessage;

        private int maxAttempts = CamelSettings.getMaxAttempts();
        private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

        private String phase = "Running";
        private boolean printLogs = CamelSettings.isPrintLogs();

        private boolean stopOnErrorStatus = true;

        /**
         * Identify Camel JBang process for this route.
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

        public Builder isRunning() {
            this.phase = "Running";
            return this;
        }

        public Builder isStopped() {
            this.phase = "Stopped";
            return this;
        }

        public Builder isInPhase(String phase) {
            this.phase = phase;
            return this;
        }

        public Builder printLogs(boolean printLogs) {
            this.printLogs = printLogs;
            return this;
        }

        public Builder waitForLogMessage(String logMessage) {
            this.logMessage = logMessage;
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder delayBetweenAttempts(long delayBetweenAttempts) {
            this.delayBetweenAttempts = delayBetweenAttempts;
            return this;
        }

        public Builder stopOnErrorStatus(boolean stopOnErrorStatus) {
            this.stopOnErrorStatus = stopOnErrorStatus;
            return this;
        }

        @Override
        public CamelVerifyIntegrationAction build() {
            return new CamelVerifyIntegrationAction(this);
        }
    }
}
