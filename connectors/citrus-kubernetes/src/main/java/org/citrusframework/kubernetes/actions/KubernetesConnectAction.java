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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesVariableNames;

/**
 * Connects to a Kubernetes cluster.
 */
public class KubernetesConnectAction extends AbstractKubernetesAction implements KubernetesAction {

    public KubernetesConnectAction(Builder builder) {
        super("kubernetes-connect", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        if (isDisabled(context)) {
            return;
        }

        context.setVariable(KubernetesVariableNames.CONNECTED.value(), true);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<KubernetesConnectAction, Builder> {

        private String containerImage;
        private final Map<String, String> annotations = new HashMap<>();
        private final Map<String, String> labels = new HashMap<>();

        public Builder image(String containerImage) {
            this.containerImage = containerImage;
            return this;
        }

        public Builder annotations(Map<String, String>annotations) {
            this.annotations.putAll(annotations);
            return this;
        }

        public Builder annotation(String annotation, String value) {
            this.annotations.put(annotation, value);
            return this;
        }

        public Builder labels(Map<String, String>labels) {
            this.labels.putAll(labels);
            return this;
        }

        public Builder label(String label, String value) {
            this.labels.put(label, value);
            return this;
        }

        @Override
        public KubernetesConnectAction doBuild() {
            return new KubernetesConnectAction(this);
        }
    }
}
