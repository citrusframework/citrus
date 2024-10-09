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
import io.fabric8.knative.eventing.v1.BrokerBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.KnativeVariableNames;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class CreateBrokerAction extends AbstractKnativeAction {

    private final String brokerName;

    public CreateBrokerAction(Builder builder) {
        super("create-broker", builder);

        this.brokerName = builder.brokerName;
    }

    @Override
    public void doExecute(TestContext context) {
        if (KubernetesSettings.isLocal(clusterType(context))) {
            createLocalBroker(context);
        } else {
            createBroker(context);
        }
    }

    /**
     * Creates Http server as a local Knative broker.
     * @param context
     */
    private void createLocalBroker(TestContext context) {
        String resolvedBrokerName = context.replaceDynamicContentInString(brokerName);

        logger.info(String.format("Creating local Knative broker: %s", resolvedBrokerName));

        HttpServer brokerServer;
        if (!context.getReferenceResolver().isResolvable(resolvedBrokerName, HttpServer.class)) {
             brokerServer = new HttpServerBuilder()
                    .autoStart(true)
                    .port(KnativeSettings.getServicePort())
                    .referenceResolver(context.getReferenceResolver())
                    .build();

            brokerServer.initialize();
            context.getReferenceResolver()
                    .bind(resolvedBrokerName, brokerServer);
        } else {
            brokerServer = context.getReferenceResolver().resolve(resolvedBrokerName, HttpServer.class);
        }

        context.setVariable(KnativeVariableNames.BROKER_PORT.value(), brokerServer.getPort());

        logger.info(String.format("Successfully created Knative broker: %s", resolvedBrokerName));
    }

    /**
     * Creates Knative broker on current namespace.
     * @param context
     */
    private void createBroker(TestContext context) {
        String resolvedBrokerName = context.replaceDynamicContentInString(brokerName);
        String brokerNamespace = namespace(context);

        logger.info(String.format("Creating Knative broker '%s' in namespace %s", resolvedBrokerName, brokerNamespace));

        Broker broker = new BrokerBuilder()
                .withApiVersion(String.format("%s/%s", KnativeSupport.knativeEventingGroup(), KnativeSupport.knativeApiVersion()))
                .withNewMetadata()
                .withNamespace(brokerNamespace)
                .withName(resolvedBrokerName)
                .withLabels(KnativeSettings.getDefaultLabels())
                .endMetadata()
                .build();

        getKnativeClient().brokers()
                .inNamespace(brokerNamespace)
                .resource(broker)
                .createOr(Updatable::update);

        if (isAutoRemoveResources()) {
            context.doFinally(knative().client(getKubernetesClient()).client(getKnativeClient())
                    .brokers()
                    .delete(resolvedBrokerName)
                    .inNamespace(getNamespace()));
        }

        logger.info(String.format("Successfully created Knative broker '%s' in namespace %s", resolvedBrokerName, brokerNamespace));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<CreateBrokerAction, Builder> {

        private String brokerName;

        public Builder broker(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        @Override
        public CreateBrokerAction doBuild() {
            return new CreateBrokerAction(this);
        }
    }
}
