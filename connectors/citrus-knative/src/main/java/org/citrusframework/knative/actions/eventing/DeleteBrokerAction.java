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
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

public class DeleteBrokerAction extends AbstractKnativeAction {

    private final String brokerName;

    public DeleteBrokerAction(Builder builder) {
        super("delete-broker", builder);

        this.brokerName = builder.brokerName;
    }

    @Override
    public void doExecute(TestContext context) {
        if (KubernetesSettings.isLocal(clusterType(context))) {
            deleteLocalBroker(context);
        } else {
            deleteBroker(context);
        }
    }

    /**
     * Removes Http server acting as local Knative broker.
     * @param context
     */
    private void deleteLocalBroker(TestContext context) {
        String resolvedBrokerName = context.replaceDynamicContentInString(brokerName);
        if (context.getReferenceResolver().isResolvable(resolvedBrokerName, HttpServer.class)) {
            HttpServer brokerServer = context.getReferenceResolver().resolve(resolvedBrokerName, HttpServer.class);
            brokerServer.stop();
        }
    }

    /**
     * Removes Knative broker from current namespace.
     * @param context
     */
    private void deleteBroker(TestContext context) {
        getKnativeClient().brokers()
                .inNamespace(namespace(context))
                .withName(context.replaceDynamicContentInString(brokerName))
                .delete();
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<DeleteBrokerAction, Builder> {

        private String brokerName;

        public Builder broker(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        @Override
        public DeleteBrokerAction doBuild() {
            return new DeleteBrokerAction(this);
        }
    }
}
