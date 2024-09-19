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

package org.citrusframework.knative.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.kubernetes.KubernetesSupport;

public class DeleteKnativeResourceAction extends AbstractKnativeAction {

    private final String component;
    private final String kind;
    private final String resourceName;

    public DeleteKnativeResourceAction(Builder builder) {
        super("delete-" + builder.kind, builder);

        this.component = builder.component;
        this.kind = builder.kind;
        this.resourceName = builder.resourceName;
    }

    @Override
    public void doExecute(TestContext context) {
        KubernetesSupport.deleteResource(getKubernetesClient(), namespace(context),
                KnativeSupport.knativeCRDContext(component, kind, KnativeSupport.knativeApiVersion()), resourceName);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<DeleteKnativeResourceAction, Builder> {

        private String component = "eventing";
        private String kind;
        private String resourceName;

        public Builder component(String component) {
            this.component = component;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder resource(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        @Override
        public DeleteKnativeResourceAction doBuild() {
            return new DeleteKnativeResourceAction(this);
        }
    }
}
