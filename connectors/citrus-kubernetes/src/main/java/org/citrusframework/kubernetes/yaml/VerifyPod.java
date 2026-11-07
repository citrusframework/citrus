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

package org.citrusframework.kubernetes.yaml;

import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.VerifyPodAction;
import org.citrusframework.api.yaml.SchemaProperty;

public class VerifyPod extends AbstractKubernetesAction.Builder<VerifyPodAction, VerifyPod> {

    private final VerifyPodAction.Builder delegate = new VerifyPodAction.Builder();

    @SchemaProperty(description = "Name of the pod to verify.")
    public void setName(String name) {
        this.delegate.podName(name);
    }

    @SchemaProperty(description = "Labels used to select the pod to verify.")
    public void setLabels(List<Label> labels) {
        labels.forEach(label -> this.delegate.label(label.getName(), label.getValue()));
    }

    @SchemaProperty(description = "Label expression in name=value form used to select the pod.")
    public void setLabel(String labelExpression) {
        String[] tokens = labelExpression.split("=", 2);
        this.delegate.label(tokens[0].trim(), tokens[1].trim());
    }

    @SchemaProperty(description = "Expected pod phase, such as Running or Stopped.")
    public void setPhase(String phase) {
        this.delegate.phase(phase);
    }

    @SchemaProperty(description = "Log message to wait for after the pod reaches the expected phase.")
    public void setLogMessage(String message) {
        this.delegate.waitForLogMessage(message);
    }

    @SchemaProperty(description = "Prints pod logs while waiting for the expected log message.")
    public void setPrintLogs(boolean printLogs) {
        this.delegate.printLogs(printLogs);
    }

    @SchemaProperty(description = "Maximum number of status or log verification attempts.")
    public void setMaxAttempts(int maxAttempts) {
        this.delegate.maxAttempts(maxAttempts);
    }

    @SchemaProperty(description = "Delay in milliseconds between verification attempts.")
    public void setDelayBetweenAttempts(long delayBetweenAttempts) {
        this.delegate.delayBetweenAttempts(delayBetweenAttempts);
    }

    @Override
    public VerifyPod description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public VerifyPod actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public VerifyPod client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public VerifyPod inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public VerifyPod autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public VerifyPodAction doBuild() {
        return delegate.build();
    }

    public static class Label {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(String value) {
            this.value = value;
        }

    }
}
