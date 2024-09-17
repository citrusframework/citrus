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

import org.citrusframework.context.TestContext;

public class DeleteSecretAction extends AbstractKubernetesAction {

    private final String secretName;

    public DeleteSecretAction(Builder builder) {
        super("delete-secret", builder);

        this.secretName = builder.secretName;
    }

    @Override
    public void doExecute(TestContext context) {
        getKubernetesClient().secrets().inNamespace(namespace(context))
                .withName(context.replaceDynamicContentInString(secretName))
                .delete();
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<DeleteSecretAction, Builder> {

        private String secretName;

        public Builder secret(String secretName) {
            this.secretName = secretName;
            return this;
        }

        @Override
        public DeleteSecretAction doBuild() {
            return new DeleteSecretAction(this);
        }
    }
}
