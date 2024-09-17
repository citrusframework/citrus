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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action watches pod logs for a given amount of time and prints logs to the log output of the test.
 */
public class WatchPodLogsAction extends AbstractKubernetesAction {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(WatchPodLogsAction.class);
    private static final Logger POD_LOG = LoggerFactory.getLogger("POD_LOGS");

    private final String podName;
    private final String labelExpression;
    private final String timeout;

    private final TimeUnit timeUnit;

    /**
     * Constructor using given builder.
     * @param builder
     */
    public WatchPodLogsAction(Builder builder) {
        super("watch-pod-logs", builder);
        this.podName = builder.podName;
        this.labelExpression = builder.labelExpression;
        this.timeout = builder.timeout;
        this.timeUnit = builder.timeUnit;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedPodName = context.replaceDynamicContentInString(podName);
        String resolvedLabelExpression = context.replaceDynamicContentInString(labelExpression);

        Pod pod;
        if (resolvedPodName != null && !resolvedPodName.isEmpty()) {
            pod = getPod(resolvedPodName, namespace(context));
        } else {
            pod = getPodFromLabel(resolvedLabelExpression, namespace(context));
        }

        String containerName = null;
        if (pod.getSpec() != null && pod.getSpec().getContainers() != null && pod.getSpec().getContainers().size() > 1) {
            containerName = pod.getSpec().getContainers().get(0).getName();
        }

        PodResource podRes = getKubernetesClient().pods()
                .inNamespace(namespace(context))
                .withName(pod.getMetadata().getName());

        LogWatch logs;
        if (containerName != null) {
            logs = podRes.inContainer(containerName).watchLog();
        } else {
            logs = podRes.watchLog();
        }

        long stoppingAt = System.currentTimeMillis() + getDurationMillis();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logs.getOutput()))) {
            String line;
            while (stoppingAt - System.currentTimeMillis() > 0 && (line = reader.readLine()) != null) {
                POD_LOG.info(line);
            }
        } catch (IOException e) {
            LOG.error("Failed to read pod logs", e);
        }
    }

    private long getDurationMillis() {
        if (timeout.indexOf(".") > 0) {
            switch (timeUnit) {
                case MILLISECONDS:
                    return Math.round(Double.parseDouble(timeout));
                case SECONDS:
                    return Math.round(Double.parseDouble(timeout) * 1000);
                case MINUTES:
                    return Math.round(Double.parseDouble(timeout) * 60 * 1000);
                default:
                    throw new CitrusRuntimeException("Unsupported time expression for watch pod log action - " +
                            "please use one of milliseconds, seconds, minutes");
            }
        }

        switch (timeUnit) {
            case MILLISECONDS:
                return Long.parseLong(timeout);
            case SECONDS:
                return Long.parseLong(timeout) * 1000;
            case MINUTES:
                return Long.parseLong(timeout) * 60 * 1000;
            default:
                throw new CitrusRuntimeException("Unsupported time expression for watch pod log action - " +
                        "please use one of milliseconds, seconds, minutes");
        }
    }

    /**
     * Retrieve pod given state.
     * @param name
     * @param namespace
     * @return
     */
    private Pod getPod(String name, String namespace) {
        return getKubernetesClient().pods()
                    .inNamespace(namespace)
                    .withName(name)
                    .get();
    }

    /**
     * Retrieve pod given state selected by label key and value expression.
     * @param labelExpression
     * @param namespace
     * @return
     */
    private Pod getPodFromLabel(String labelExpression, String namespace) {
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

        return pods.getItems().stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractKubernetesAction.Builder<WatchPodLogsAction, Builder> {

        private String podName;
        private String labelExpression;
        private String timeout = "60000";

        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public Builder podName(String podName) {
            this.podName = podName;
            return this;
        }

        public Builder label(String name, String value) {
            this.labelExpression = String.format("%s=%s", name, value);
            return this;
        }

        public Builder milliseconds(String time) {
            this.timeout = time;
            this.timeUnit = TimeUnit.MILLISECONDS;
            return this;
        }

        public Builder seconds(String time) {
            this.timeout = time;
            this.timeUnit = TimeUnit.SECONDS;
            return this;
        }

        public Builder minutes(String time) {
            this.timeout = time;
            this.timeUnit = TimeUnit.SECONDS;
            return this;
        }

        public Builder timeout(Duration duration) {
            this.timeout = String.valueOf(duration.toMillis());
            this.timeUnit = TimeUnit.MILLISECONDS;
            return this;
        }

        @Override
        public WatchPodLogsAction doBuild() {
            return new WatchPodLogsAction(this);
        }
    }
}
