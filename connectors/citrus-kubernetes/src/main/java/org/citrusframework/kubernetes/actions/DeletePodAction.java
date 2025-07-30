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

import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import org.citrusframework.actions.kubernetes.KubernetesPodDeleteActionBuilder;
import org.citrusframework.context.TestContext;

public class DeletePodAction extends AbstractKubernetesAction {

    private final String podName;
    private final String labelExpression;

    public DeletePodAction(Builder builder) {
        super("delete-pod", builder);

        this.podName = builder.podName;
        this.labelExpression = builder.labelExpression;
    }

    @Override
    public void doExecute(TestContext context) {
        if (podName != null) {
            getKubernetesClient().pods()
                    .inNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(podName))
                    .delete();
        } else if (labelExpression != null) {
            String[] tokens = context.replaceDynamicContentInString(labelExpression).split("=");
            String labelKey = tokens[0];
            String labelValue = tokens.length > 1 ? tokens[1] : "";

            getKubernetesClient().pods()
                    .inNamespace(namespace(context))
                    .withLabel(labelKey, labelValue)
                    .delete();
        } else {
            // delete all pods in current namespace
            List<Pod> pods = getKubernetesClient().pods()
                    .list()
                    .getItems();

            getKubernetesClient().resourceList(pods)
                    .inNamespace(namespace(context))
                    .delete();
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<DeletePodAction, Builder>
            implements KubernetesPodDeleteActionBuilder<DeletePodAction, Builder> {

        private String podName;
        private String labelExpression;

        @Override
        public Builder podName(String podName) {
            this.podName = podName;
            return this;
        }

        @Override
        public Builder label(String name, String value) {
            this.labelExpression = String.format("%s=%s", name, value);
            return this;
        }

        @Override
        public DeletePodAction doBuild() {
            return new DeletePodAction(this);
        }
    }
}
