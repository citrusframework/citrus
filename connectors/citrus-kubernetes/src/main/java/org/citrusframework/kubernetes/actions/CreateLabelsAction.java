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

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import org.citrusframework.actions.kubernetes.KubernetesCreateLabelsActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesResourceType;

public class CreateLabelsAction extends AbstractKubernetesAction implements KubernetesAction {

    private final String resourceName;
    private final KubernetesResourceType resourceType;
    private final Map<String, String> labels;

    public CreateLabelsAction(Builder builder) {
        super("create-label", builder);

        this.resourceName = builder.resourceName;
        this.resourceType = builder.resourceType;
        this.labels = builder.labels;
    }

    @Override
    public void doExecute(TestContext context) {
        Map<String, String> resolvedLabels = context.resolveDynamicValuesInMap(labels);

        switch (resourceType) {
            case DEPLOYMENT:
                getKubernetesClient().apps().deployments()
                        .inNamespace(namespace(context))
                        .withName(resourceName)
                        .edit(d -> new DeploymentBuilder(d)
                                    .editMetadata()
                                        .addToLabels(resolvedLabels)
                                    .endMetadata()
                                .build());
                break;
            case POD:
                getKubernetesClient().pods()
                        .inNamespace(namespace(context))
                        .withName(resourceName)
                        .edit(p -> new PodBuilder(p)
                                    .editMetadata()
                                        .addToLabels(resolvedLabels)
                                    .endMetadata()
                                .build());
                break;
            case SERVICE:
                getKubernetesClient().services()
                        .inNamespace(namespace(context))
                        .withName(resourceName)
                        .edit(s -> new ServiceBuilder(s)
                                    .editMetadata()
                                        .addToLabels(resolvedLabels)
                                    .endMetadata()
                                .build());
                break;
            case SECRET:
                getKubernetesClient().secrets()
                        .inNamespace(namespace(context))
                        .withName(resourceName)
                        .edit(s -> new SecretBuilder(s)
                                    .editMetadata()
                                        .addToLabels(resolvedLabels)
                                    .endMetadata()
                                .build());
                break;
            case CONFIGMAP:
                getKubernetesClient().configMaps()
                        .inNamespace(namespace(context))
                        .withName(resourceName)
                        .edit(cm -> new ConfigMapBuilder(cm)
                                    .editMetadata()
                                        .addToLabels(resolvedLabels)
                                    .endMetadata()
                                .build());
                break;
            default:
                throw new CitrusRuntimeException(String.format("Unable to add label to resource type '%s'", resourceType.name()));
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<CreateLabelsAction, Builder>
            implements KubernetesCreateLabelsActionBuilder<CreateLabelsAction, Builder> {

        private String resourceName;
        private KubernetesResourceType resourceType = KubernetesResourceType.POD;
        private final Map<String, String> labels = new HashMap<>();

        @Override
        public Builder resource(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        @Override
        public Builder deployment(String name) {
            this.resourceName = name;
            return type(KubernetesResourceType.DEPLOYMENT);
        }

        @Override
        public Builder pod(String name) {
            this.resourceName = name;
            return type(KubernetesResourceType.POD);
        }

        @Override
        public Builder secret(String name) {
            this.resourceName = name;
            return type(KubernetesResourceType.SECRET);
        }

        @Override
        public Builder configMap(String name) {
            this.resourceName = name;
            return type(KubernetesResourceType.CONFIGMAP);
        }

        @Override
        public Builder service(String name) {
            this.resourceName = name;
            return type(KubernetesResourceType.SERVICE);
        }

        @Override
        public Builder type(KubernetesResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        @Override
        public Builder type(String resourceType) {
            return type(KubernetesResourceType.valueOf(resourceType));
        }

        @Override
        public Builder labels(Map<String, String> labels) {
            this.labels.putAll(labels);
            return this;
        }

        @Override
        public Builder label(String label, String value) {
            this.labels.put(label, value);
            return this;
        }

        @Override
        public CreateLabelsAction doBuild() {
            return new CreateLabelsAction(this);
        }
    }
}
