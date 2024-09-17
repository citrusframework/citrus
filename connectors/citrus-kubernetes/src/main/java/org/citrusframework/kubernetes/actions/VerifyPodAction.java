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

package org.citrusframework.kubernetes.actions;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test action verifies pod phase in running/stopped state and optionally waits for a log message to be present. Raises errors
 * when either the pod is not in expected state or the log message is not available. Both operations are automatically retried
 * for a given amount of attempts.
 */
public class VerifyPodAction extends AbstractKubernetesAction {

    private static final Logger POD_STATUS_LOG = LoggerFactory.getLogger("POD_STATUS");
    private static final Logger POD_LOG = LoggerFactory.getLogger("POD_LOGS");

    private final String podName;
    private final String labelExpression;
    private final String logMessage;
    private final int maxAttempts;
    private final long delayBetweenAttempts;

    private final String phase;
    private final boolean printLogs;

    /**
     * Constructor using given builder.
     * @param builder
     */
    public VerifyPodAction(Builder builder) {
        super("verify-pod-status", builder);
        this.podName = builder.podName;
        this.labelExpression = builder.labelExpression;
        this.phase = builder.phase;
        this.logMessage = builder.logMessage;
        this.maxAttempts = builder.maxAttempts;
        this.delayBetweenAttempts = builder.delayBetweenAttempts;
        this.printLogs = builder.printLogs;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedPodName = context.replaceDynamicContentInString(podName);
        String resolvedLabelExpression = context.replaceDynamicContentInString(labelExpression);
        Pod pod = verifyPod(resolvedPodName, resolvedLabelExpression,
                context.replaceDynamicContentInString(phase), namespace(context));

        if (logMessage != null) {
            verifyPodLogs(pod, getNameOrLabel(resolvedPodName, resolvedLabelExpression), namespace(context), context.replaceDynamicContentInString(logMessage));
        }
    }

    /**
     * Wait for pod to log given message.
     * @param pod
     * @param nameOrLabel
     * @param namespace
     * @param message
     */
    private void verifyPodLogs(Pod pod, String nameOrLabel, String namespace, String message) {
        if (printLogs) {
            POD_LOG.info(String.format("Waiting for pod '%s' to log message", nameOrLabel));
        }

        String log;
        int offset = 0;

        for (int i = 0; i < maxAttempts; i++) {
            log = getPodLogs(pod, namespace);

            if (printLogs && (offset < log.length())) {
                POD_LOG.info(log.substring(offset));
                offset = log.length();
            }

            if (log.contains(message)) {
                logger.info("Verified pod logs - All values OK!");
                return;
            }

            if (!printLogs) {
                logger.info(String.format("Waiting for pod '%s' to log message - retry in %s ms", nameOrLabel, delayBetweenAttempts));
            }

            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for pod logs", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify pod '%s' - " +
                        "has not printed message '%s' after %d attempts", nameOrLabel, logMessage, maxAttempts)));
    }

    /**
     * Retrieve log messages from given pod.
     * @param pod
     * @param namespace
     * @return
     */
    private String getPodLogs(Pod pod, String namespace) {
        PodResource podRes = getKubernetesClient().pods()
                .inNamespace(namespace)
                .withName(pod.getMetadata().getName());

        String containerName = null;
        if (pod.getSpec() != null && pod.getSpec().getContainers() != null && pod.getSpec().getContainers().size() > 1) {
            containerName = pod.getSpec().getContainers().get(0).getName();
        }

        String logs;
        if (containerName != null) {
            logs = podRes.inContainer(containerName).getLog();
        } else {
            logs = podRes.getLog();
        }
        return logs;
    }

    /**
     * Wait for given pod to be in given state.
     * @param name
     * @param labelExpression         1
     * @param phase
     * @param namespace
     * @return
     */
    private Pod verifyPod(String name, String labelExpression, String phase, String namespace) {
        if (StringUtils.hasText(name)) {
            POD_STATUS_LOG.info(String.format("Waiting for pod '%s' to be in state '%s'", name, phase));
        } else {
            POD_STATUS_LOG.info(String.format("Waiting for pod with label '%s' to be in state '%s'", labelExpression, phase));
        }

        for (int i = 0; i < maxAttempts; i++) {
            Pod pod;
            if (StringUtils.hasText(name)) {
                pod = getPod(name, phase, namespace);
            } else {
                pod = getPodFromLabel(labelExpression, phase, namespace);
            }

            if (pod != null) {
                logger.info(String.format("Verified pod '%s' state '%s'!", getNameOrLabel(name, labelExpression), phase));
                return pod;
            }

            logger.info(String.format("Waiting for pod '%s' in state '%s' - retry in %s ms",
                    getNameOrLabel(name, labelExpression), phase, delayBetweenAttempts));
            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for pod state", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify pod '%s' - " +
                        "is not in state '%s' after %d attempts", getNameOrLabel(name, labelExpression), phase, maxAttempts)));
    }

    /**
     * Retrieve pod given state.
     * @param name
     * @param phase
     * @param namespace
     * @return
     */
    private Pod getPod(String name, String phase, String namespace) {
        Pod pod = getKubernetesClient().pods()
                .inNamespace(namespace)
                .withName(name)
                .get();

        boolean verified = KubernetesSupport.verifyPodStatus(pod, phase);

        if (!verified) {
            POD_STATUS_LOG.info(String.format("Pod '%s' not yet in state '%s'. Will keep checking ...", name, phase));
        }

        return verified ? pod : null;
    }

    /**
     * Retrieve pod given state selected by label key and value expression.
     * @param labelExpression
     * @param phase
     * @param namespace
     * @return
     */
    private Pod getPodFromLabel(String labelExpression, String phase, String namespace) {
        if (labelExpression == null || labelExpression.isEmpty()) {
            return null;
        }

        String[] tokens = labelExpression.split("=");
        String labelKey = tokens[0];
        String labelValue = tokens.length > 1 ? tokens[1] : "";

        PodList pods = getKubernetesClient().pods()
                .inNamespace(namespace)
                .withLabel(labelKey, labelValue)
                .list();

        if (pods.getItems().isEmpty()) {
            POD_STATUS_LOG.info(String.format("Integration with label '%s' not yet available. Will keep checking ...", labelExpression));
        }

        return pods.getItems().stream()
                .filter(pod -> {
                    boolean verified = KubernetesSupport.verifyPodStatus(pod, phase);

                    if (!verified) {
                        POD_STATUS_LOG.info(String.format("Pod with label '%s' not yet in state '%s'. Will keep checking ...", labelExpression, phase));
                    }

                    return verified;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * If name is set return as pod name. Else return given label expression.
     * @param name
     * @param labelExpression
     * @return
     */
    private String getNameOrLabel(String name, String labelExpression) {
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return labelExpression;
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractKubernetesAction.Builder<VerifyPodAction, Builder> {

        private String podName;
        private String labelExpression;
        private String logMessage;

        private int maxAttempts = KubernetesSettings.getMaxAttempts();
        private long delayBetweenAttempts = KubernetesSettings.getDelayBetweenAttempts();

        private String phase = "Running";
        private boolean printLogs = true;

        public Builder phase(String phase) {
            this.phase = phase;
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

        public Builder printLogs(boolean printLogs) {
            this.printLogs = printLogs;
            return this;
        }

        public Builder podName(String podName) {
            this.podName = podName;
            return this;
        }

        public Builder label(String name, String value) {
            this.labelExpression = String.format("%s=%s", name, value);
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

        @Override
        public VerifyPodAction doBuild() {
            return new VerifyPodAction(this);
        }
    }
}
