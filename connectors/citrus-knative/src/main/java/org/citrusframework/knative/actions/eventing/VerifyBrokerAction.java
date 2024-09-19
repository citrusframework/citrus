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

import io.fabric8.knative.eventing.v1.Broker;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

public class VerifyBrokerAction extends AbstractKnativeAction {

    private final String brokerName;

    public VerifyBrokerAction(Builder builder) {
        super("verify-broker", builder);

        this.brokerName = builder.brokerName;
    }

    @Override
    public void doExecute(TestContext context) {
        if (KubernetesSettings.isLocal(clusterType(context))) {
            verifyLocalBroker(context);
        } else {
            verifyBroker(context);
        }
    }

    private void verifyLocalBroker(TestContext context) {
        String resolvedBrokerName = context.replaceDynamicContentInString(brokerName);

        if (!context.getReferenceResolver().isResolvable(resolvedBrokerName, HttpServer.class)) {
            throw new ValidationException(String.format("Knative broker '%s' not found", brokerName));
        }

        HttpServer brokerServer = context.getReferenceResolver().resolve(resolvedBrokerName, HttpServer.class);
        if (!brokerServer.isRunning()) {
            throw new ValidationException(String.format("Knative broker '%s' is not ready", brokerName));
        }

        logger.info(String.format("Knative broker %s is ready", brokerName));
    }

    private void verifyBroker(TestContext context) {
        try {
            Broker broker = getKnativeClient().brokers()
                    .inNamespace(namespace(context))
                    .withName(brokerName)
                    .get();

            if (broker.getStatus() != null &&
                    broker.getStatus().getConditions() != null &&
                    broker.getStatus().getConditions().stream()
                            .anyMatch(condition -> condition.getType().equals("Ready") &&
                                    condition.getStatus().equalsIgnoreCase("True"))) {
                logger.info(String.format("Knative broker %s is ready", brokerName));
            } else {
                throw new ValidationException(String.format("Knative broker '%s' is not ready", brokerName));
            }
        } catch (KubernetesClientException e) {
            throw new ValidationException(String.format("Failed to validate Knative broker '%s' - " +
                    "not found in namespace '%s'", brokerName, namespace(context)), e);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<VerifyBrokerAction, Builder> {

        private String brokerName;

        public Builder broker(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        @Override
        public VerifyBrokerAction doBuild() {
            return new VerifyBrokerAction(this);
        }
    }
}
