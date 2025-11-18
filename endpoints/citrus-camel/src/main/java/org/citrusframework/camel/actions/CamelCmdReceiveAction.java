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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.actions.camel.CamelJBangCmdReceiveActionBuilder;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.jbang.JBangSupport.OK_EXIT_CODE;

/**
 * Camel JBang receive command.
 */
public class CamelCmdReceiveAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelCmdReceiveAction.class);

    private static final Logger CAMEL_JBANG_LOG = LoggerFactory.getLogger("CAMEL_JBANG_CMD_RECEIVE_LOGS");

    /** Camel integration name */
    private final String integrationName;

    /** Endpoint to invoke */
    private final String endpoint;

    /** Endpoint URI to invoke */
    private final String endpointUri;

    /** Filter messages based on this expression */
    private final String grep;

    /** Return messages newer than a relative duration */
    private final String since;

    /** The number of messages from the end to show */
    private final String tail;

    /** Camel JBang command arguments */
    private final List<String> args;

    /** Polling configuration */
    private final int maxAttempts;
    private final long delayBetweenAttempts;

    private final boolean printLogs;
    private final boolean stopOnErrorStatus;

    /**
     * Default constructor.
     */
    public CamelCmdReceiveAction(CamelCmdReceiveAction.Builder builder) {
        super("cmd-receive", builder);
        this.integrationName = builder.integrationName;
        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.args = builder.args;
        this.grep = builder.grep;
        this.since = builder.since;
        this.tail = builder.tail;
        this.maxAttempts = builder.maxAttempts;
        this.delayBetweenAttempts = builder.delayBetweenAttempts;
        this.printLogs = builder.printLogs;
        this.stopOnErrorStatus = builder.stopOnErrorStatus;
    }

    @Override
    public void doExecute(TestContext context) {
        List<String> commandArgs = new ArrayList<>();

        if (StringUtils.hasText(integrationName)) {
            logger.info("Camel JBang cmd receiving message from integration '%s'".formatted(integrationName));
            commandArgs.add(context.replaceDynamicContentInString(integrationName));
        } else {
            logger.info("Camel JBang cmd receiving message from current Camel integration");
        }

        if (StringUtils.hasText(endpoint)) {
            commandArgs.add("--endpoint");
            commandArgs.add(endpoint);
        }

        if (StringUtils.hasText(endpointUri)) {
            commandArgs.add("--uri");
            commandArgs.add(endpointUri);
        }

        if (StringUtils.hasText(grep)) {
            commandArgs.add("--grep");
            commandArgs.add(grep);
        }

        if (StringUtils.hasText(since)) {
            commandArgs.add("--since");
            commandArgs.add(since);
        }

        if (StringUtils.hasText(tail)) {
            commandArgs.add("--tail");
            commandArgs.add(tail);
        }

        if (!args.contains("--logging-color") && args.stream().noneMatch(it -> it.startsWith("--logging-color"))) {
            // disable logging colors by default
            commandArgs.add("--logging-color=false");
        }

        if (!args.isEmpty()) {
            commandArgs.addAll(context.resolveDynamicValuesInList(args));
        }

        ProcessAndOutput pao = null;
        try {
            pao = camelJBang().receive(commandArgs.toArray(new String[0]));
            logger.info("Receive messages from Camel JBang receive command ...");

            String log;
            for (int i = 0; i < maxAttempts; i++) {
                if (!pao.getProcess().isAlive()) {
                    int exitValue = pao.getProcess().exitValue();
                    if (exitValue != OK_EXIT_CODE) {
                        logger.warn("Failed to receive message via Camel JBang command:%n\t camel cmd receive %s".formatted(String.join(" ", commandArgs)));
                        throw new CitrusRuntimeException("Error while receiving messages via Camel JBang: '%s' Exit code: %d"
                                .formatted(pao.getOutput(), exitValue));
                    }
                }

                log = pao.getOutput();

                if (printLogs) {
                    CAMEL_JBANG_LOG.info(log);
                }

                if (log.contains("Received Message:")) {
                    logger.info("Verified Camel received message - All values OK!");
                    return;
                }

                if (log.contains("STACK-TRACE") && stopOnErrorStatus) {
                    throw new CitrusRuntimeException("Error while receiving messages via Camel JBang - detected error state in Camel receive operation");
                }

                logger.warn(String.format("Waiting for Camel message '%s' - retry in %s ms", integrationName, delayBetweenAttempts));

                try {
                    Thread.sleep(delayBetweenAttempts);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for Camel message", e);
                }
            }

            throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                    new CitrusRuntimeException(String.format("Failed to verify Camel receive command '%s' - " +
                            "has not received message with matching '%s' after %d attempts", integrationName, grep, maxAttempts)));
        } finally {
            if (pao != null && pao.getProcess().isAlive()) {
                pao.getProcess().destroy();
            }
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelCmdReceiveAction, Builder>
            implements CamelJBangCmdReceiveActionBuilder<CamelCmdReceiveAction, Builder> {

        private String integrationName;
        private String endpoint;
        private String endpointUri;
        private String grep;
        private String since;
        private String tail = "0";
        private final List<String> args = new ArrayList<>();

        private int maxAttempts = CamelSettings.getMaxAttempts();
        private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

        private boolean printLogs = CamelSettings.isPrintLogs();
        private boolean stopOnErrorStatus = true;

        @Override
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public Builder endpointUri(String uri) {
            this.endpointUri = uri;
            return this;
        }

        @Override
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        @Override
        public Builder withArg(String name, String value) {
            this.args.add(name);
            this.args.add(value);
            return this;
        }

        @Override
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        @Override
        public Builder loggingColor(boolean enabled) {
            this.withArg("--logging-color=%s".formatted(enabled));
            return this;
        }

        @Override
        public Builder grep(String filter) {
            this.grep = filter;
            return this;
        }

        @Override
        public Builder since(String duration) {
            this.since = duration;
            return this;
        }

        @Override
        public Builder tail(String numberOfMessages) {
            this.tail = numberOfMessages;
            return this;
        }

        @Override
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        @Override
        public Builder delayBetweenAttempts(long delayBetweenAttempts) {
            this.delayBetweenAttempts = delayBetweenAttempts;
            return this;
        }

        @Override
        public Builder printLogs(boolean printLogs) {
            this.printLogs = printLogs;
            return this;
        }

        @Override
        public Builder stopOnErrorStatus(boolean stopOnErrorStatus) {
            this.stopOnErrorStatus = stopOnErrorStatus;
            return this;
        }

        @Override
        public CamelCmdReceiveAction doBuild() {
            return new CamelCmdReceiveAction(this);
        }
    }
}
