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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.api.actions.camel.CamelIntegrationStopActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops given Camel integration with Camel CLI tooling.
 */
public class CamelStopIntegrationAction extends AbstractCamelCliAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelStopIntegrationAction.class);

    private static final long DEFAULT_STOP_TIMEOUT = 60000L;

    private final String integrationName;
    private final long stopTimeout;

    public CamelStopIntegrationAction(Builder builder) {
        super("stop", builder);

        this.integrationName = builder.integrationName;
        this.stopTimeout = builder.stopTimeout;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        if (name.equals("*")) {
            logger.info("Stopping all Camel integrations ...");

            camelCli().stop();

            logger.info("Stopped all Camel integrations");
        } else {
            logger.info("Stopping Camel integration '%s' ...".formatted(name));

            Long pid;
            String pidKey = "%s:%s".formatted(name, CamelRunTestVariables.PID.suffix());
            String processKeyPrefix = "%s:%s".formatted(name, CamelRunTestVariables.PROCESS.suffix());
            if (context.getVariables().containsKey(pidKey)) {
                pid = context.getVariable(pidKey, Long.class);
            } else {
                pid = findIntegrationByName(name);
                if (pid == null) {
                    logger.info("Camel integration '{}' not found - may have already exited", name);
                    return;
                }
            }

            camelCli().stop(pid);

            String processKey = "%s:%d".formatted(processKeyPrefix, pid);
            if (context.getVariables().containsKey(processKey)) {
                ProcessAndOutput pao = context.getVariable(processKey, ProcessAndOutput.class);
                if (pao.getProcess().isAlive()) {
                    List<Long> descendants = pao.getDescendants();
                    for (Long descendantPid : descendants) {
                        camelCli().stop(descendantPid);
                        logger.info("Stopped Camel integration '%s' (%s - %s)".formatted(name, pid, descendantPid));
                    }
                }
            }

            waitForProcessExit(pid, name);

            logger.info("Stopped Camel integration '%s' (%s)".formatted(name, pid));
        }
    }

    private void waitForProcessExit(Long pid, String name) {
        ProcessHandle.of(pid).ifPresent(handle -> {
            try {
                handle.onExit().get(stopTimeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new CitrusRuntimeException(
                        "Camel integration '%s' (pid %d) did not stop within %d ms".formatted(name, pid, stopTimeout));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CitrusRuntimeException(
                        "Interrupted while waiting for Camel integration '%s' (pid %d) to stop".formatted(name, pid), e);
            } catch (ExecutionException e) {
                // process exited, which is what we wanted
            }
        });
    }

    private Long findIntegrationByName(String name) {
        return camelCli().getAll().stream()
                .filter(props -> name.equals(props.get("name")) && !props.getOrDefault("pid", "").isBlank())
                .map(props -> Long.valueOf(props.get("pid"))).findFirst()
                .orElse(null);
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public long getStopTimeout() {
        return stopTimeout;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelCliAction.Builder<CamelStopIntegrationAction, Builder>
            implements CamelIntegrationStopActionBuilder<CamelStopIntegrationAction, Builder> {

        private String integrationName = "*";
        private long stopTimeout = DEFAULT_STOP_TIMEOUT;

        @Override
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        public Builder stopTimeout(long timeout) {
            this.stopTimeout = timeout;
            return this;
        }

        @Override
        public CamelStopIntegrationAction doBuild() {
            return new CamelStopIntegrationAction(this);
        }
    }
}
