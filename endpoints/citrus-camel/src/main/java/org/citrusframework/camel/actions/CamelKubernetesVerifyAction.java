package org.citrusframework.camel.actions;

import org.citrusframework.camel.CamelSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies Camel integration in kubernetes via Camel JBang. Waits for a log message to be present.
 * Raises errors when the Camel integration log message is not available.
 * Check operation is automatically retried for a given amount of attempts.
 */
public class CamelKubernetesVerifyAction extends AbstractCamelJBangAction {

    private static final Logger INTEGRATION_LOG = LoggerFactory.getLogger("INTEGRATION_LOGS");

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CamelKubernetesVerifyAction.class);

    /**
     * The integration name
     */
    private final String integrationName;
    /**
     * Label name and value used as a pod selector
     */
    private final String label;
    /**
     * Kubernetes Namespace
     */
    private final String namespace;

    private final String logMessage;
    private final int maxAttempts;
    private final long delayBetweenAttempts;

    private final boolean printLogs;

    /**
     * Camel Jbang command arguments
     */
    private final List<String> args;

    protected CamelKubernetesVerifyAction(CamelKubernetesVerifyAction.Builder builder) {
        super("verify-kubernetes-integration", builder);
        this.integrationName = builder.integrationName;
        this.label = builder.label;
        this.namespace = builder.namespace;
        this.logMessage = builder.logMessage;
        this.maxAttempts = builder.maxAttempts;
        this.delayBetweenAttempts = builder.delayBetweenAttempts;
        this.printLogs = builder.printLogs;
        this.args = builder.args;
    }


    @Override
    public void doExecute(TestContext context) {
        logger.info("Verify Camel integration in Kubernetes  ...");
        List<String> fullArgs = new ArrayList<>();
        fullArgs.add("logs");

        if (integrationName != null) {
            fullArgs.add("--name");
            fullArgs.add(integrationName);
        }
        if (label != null) {
            fullArgs.add("--label");
            fullArgs.add(label);
        }
        if (namespace != null) {
            fullArgs.add("--namespace");
            fullArgs.add(namespace);
        }

        if (args != null) {
            fullArgs.addAll(args);
        }

        verifyIntegrationLog(logMessage, fullArgs);
    }

    private void verifyIntegrationLog(String message, List<String> fullArgs) {
        if (printLogs) {
            INTEGRATION_LOG.info(String.format("Waiting for Camel integration in kubernetes to log message"));
        }

        String log;
        int offset = 0;

        ProcessAndOutput pao = camelJBang().camelApp().runAsync("kubernetes", fullArgs.toArray(String[]::new));
        for (int i = 0; i < maxAttempts; i++) {

            log = pao.getOutput();
            if (printLogs && (offset < log.length())) {
                INTEGRATION_LOG.info(log.substring(offset));
                offset = log.length();
            }

            if (log.contains(message)) {
                logger.info("Verified Camel integration in kubernetes logs - All values OK!");
                return;
            }

            if (!printLogs) {
                logger.warn(String.format("Waiting for Camel integration in kubernetes to log message - retry in %s ms", delayBetweenAttempts));
            }

            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Camel integration in kubernetes logs", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify Camel integration in kubernetes - " +
                        "has not printed message '%s' after %d attempts", message, maxAttempts)));
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelKubernetesVerifyAction, CamelKubernetesVerifyAction.Builder> {

        private String integrationName;
        private String label;
        private String namespace;

        private String logMessage;

        private int maxAttempts = CamelSettings.getMaxAttempts();
        private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

        private boolean printLogs = CamelSettings.isPrintLogs();

        private final List<String> args = new ArrayList<>();

        /**
         * Identify Camel JBang process for this route.
         *
         * @param name
         * @return
         */
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        /**
         * Sets the integration name.
         *
         * @param name
         * @return
         */
        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        /**
         * Sets the integration label.
         *
         * @param label
         * @return
         */
        public Builder label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Sets the namespace.
         *
         * @param namespace
         * @return
         */
        public Builder namespace(String namespace) {
            this.namespace = namespace;
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


        /**
         * Adds a command argument.
         *
         * @param arg
         * @return
         */
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        /**
         * Adds a command argument with name and value.
         *
         * @param name
         * @param value
         * @return
         */
        public Builder withArg(String name, String value) {
            this.args.add(name);
            this.args.add(value);
            return this;
        }

        /**
         * Adds command arguments.
         *
         * @param args
         * @return
         */
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        @Override
        public CamelKubernetesVerifyAction build() {
            return new CamelKubernetesVerifyAction(this);
        }
    }
}
