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

package org.citrusframework.knative.actions.eventing;

import org.citrusframework.context.TestContext;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

public class DeleteTriggerAction extends AbstractKnativeAction {

    private final String triggerName;

    public DeleteTriggerAction(Builder builder) {
        super("delete-trigger", builder);

        this.triggerName = builder.triggerName;
    }

    @Override
    public void doExecute(TestContext context) {
        getKnativeClient().triggers()
                .inNamespace(namespace(context))
                .withName(context.replaceDynamicContentInString(triggerName))
                .delete();
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return KubernetesSettings.isLocal(clusterType(context));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<DeleteTriggerAction, Builder> {

        private String triggerName;

        public Builder trigger(String triggerName) {
            this.triggerName = triggerName;
            return this;
        }

        @Override
        public DeleteTriggerAction doBuild() {
            return new DeleteTriggerAction(this);
        }
    }
}
