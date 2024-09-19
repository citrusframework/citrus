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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;

/**
 * Connects to a Kubernetes cluster.
 * The action starts an agent Pod that is responsible for connecting to services on the cluster.
 */
public class ClusterConnectAction extends AbstractKubernetesAction implements KubernetesAction {

    private final String containerImage;
    private final Map<String, String> annotations;
    private final Map<String, String> labels;

    public ClusterConnectAction(Builder builder) {
        super("kubernetes-connect", builder);

        this.containerImage = builder.containerImage;
        this.labels = builder.labels;
        this.annotations = builder.annotations;
    }

    @Override
    public void doExecute(TestContext context) {
        Map<String, String> resolvedAnnotations = context.resolveDynamicValuesInMap(annotations);
        Map<String, String> resolvedLabels = context.resolveDynamicValuesInMap(labels);

        if (!labels.containsKey(KubernetesSettings.getTestIdLabel())) {
            labels.put(KubernetesSettings.getTestIdLabel(), context.getVariable(CitrusSettings.TEST_NAME_VARIABLE));
        }

        String testName = KubernetesSupport.sanitize("citrus-test-" + context.getVariable(CitrusSettings.TEST_NAME_VARIABLE));
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(testName)
                    .addToAnnotations(resolvedAnnotations)
                    .addToLabels(resolvedLabels)
                .endMetadata()
                .withNewSpec()
                    .withNewTemplate()
                        .withNewSpec()
                            .addToContainers(new ContainerBuilder()
                                    .withImage(containerImage)
                                    .build())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();

        getKubernetesClient().apps().deployments()
                .inNamespace(namespace(context))
                .resource(deployment)
                .createOr(Updatable::update);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<ClusterConnectAction, Builder> {

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
        public ClusterConnectAction doBuild() {
            return new ClusterConnectAction(this);
        }
    }
}
