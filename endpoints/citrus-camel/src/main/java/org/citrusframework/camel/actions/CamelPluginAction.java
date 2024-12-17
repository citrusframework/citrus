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

package org.citrusframework.camel.actions;

import org.citrusframework.context.TestContext;


public class CamelPluginAction extends AbstractCamelJBangAction {

    private final AddCamelPluginAction addPluginAction;
    private final CamelKubernetesRunIntegrationAction kubernetesRunIntegrationAction;
    private final CamelKubernetesDeleteAction deleteKubernetesAction;
    private final CamelKubernetesVerifyAction verifyKubernetesIntegrationAction;


    public CamelPluginAction(CamelPluginAction.Builder builder) {
        super("plugin", builder);
        this.addPluginAction = builder.addPluginAction;
        this.kubernetesRunIntegrationAction = builder.kubernetesRunIntegrationAction;
        this.deleteKubernetesAction = builder.deleteKubernetesAction;
        this.verifyKubernetesIntegrationAction = builder.verifyKubernetesIntegrationAction;
    }

    @Override
    public void doExecute(TestContext context) {
        if (this.addPluginAction != null) {
            addPluginAction.doExecute(context);
        } else if (this.kubernetesRunIntegrationAction != null) {
            kubernetesRunIntegrationAction.doExecute(context);
        } else if (this.deleteKubernetesAction != null) {
            deleteKubernetesAction.doExecute(context);
        } else if (this.verifyKubernetesIntegrationAction != null) {
            verifyKubernetesIntegrationAction.doExecute(context);
        }
    }

    public AddCamelPluginAction getAdd() {
        return addPluginAction;
    }

    public CamelKubernetesRunIntegrationAction getKubernetesRunIntegration() {
        return kubernetesRunIntegrationAction;
    }

    public CamelKubernetesDeleteAction getDeleteKubernetesPluginAction() {
        return deleteKubernetesAction;
    }

    public CamelKubernetesVerifyAction getVerifyKubernetesIntegrationPluginAction() {
        return verifyKubernetesIntegrationAction;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelPluginAction, CamelPluginAction.Builder> {
        private AddCamelPluginAction addPluginAction;
        private CamelKubernetesRunIntegrationAction kubernetesRunIntegrationAction;
        private CamelKubernetesDeleteAction deleteKubernetesAction;
        private CamelKubernetesVerifyAction verifyKubernetesIntegrationAction;

        /**
         * Set add plugin action.
         *
         * @param addPluginAction
         * @return
         */
        public Builder addPluginAction(AddCamelPluginAction addPluginAction) {
            this.addPluginAction = addPluginAction;
            return this;
        }

        /**
         * Set run integration kubernetes plugin action.
         *
         * @param kubernetesRunIntegrationAction
         * @return
         */
        public Builder kubernetesRunIntegrationAction(CamelKubernetesRunIntegrationAction kubernetesRunIntegrationAction) {
            this.kubernetesRunIntegrationAction = kubernetesRunIntegrationAction;
            return this;
        }

        /**
         * Set delete kubernetes plugin action.
         *
         * @param deleteKubernetesAction
         * @return
         */
        public Builder deleteKubernetesAction(CamelKubernetesDeleteAction deleteKubernetesAction) {
            this.deleteKubernetesAction = deleteKubernetesAction;
            return this;
        }

        /**
         * Set verify kubernetes integration plugin action.
         *
         * @param verifyKubernetesIntegrationAction
         * @return
         */
        public Builder verifyKubernetesIntegrationAction(CamelKubernetesVerifyAction verifyKubernetesIntegrationAction) {
            this.verifyKubernetesIntegrationAction = verifyKubernetesIntegrationAction;
            return this;
        }

        @Override
        public CamelPluginAction build() {
            return new CamelPluginAction(this);
        }
    }
}
