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

package org.citrusframework.knative.actions.messaging;

import io.fabric8.knative.internal.pkg.apis.duck.v1.KReferenceBuilder;
import io.fabric8.knative.messaging.v1.Subscription;
import io.fabric8.knative.messaging.v1.SubscriptionBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class CreateSubscriptionAction extends AbstractKnativeAction {

    private final String subscriptionName;
    private final String channelName;
    private final String serviceName;

    public CreateSubscriptionAction(Builder builder) {
        super("create-subscription", builder);

        this.subscriptionName = builder.subscriptionName;
        this.channelName = builder.channelName;
        this.serviceName = builder.serviceName;
    }

    @Override
    public void doExecute(TestContext context) {
        Subscription subscription = new SubscriptionBuilder()
                .withApiVersion(String.format("%s/%s", KnativeSupport.knativeMessagingGroup(), KnativeSupport.knativeApiVersion()))
                .withNewMetadata()
                    .withNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(subscriptionName))
                    .withLabels(KnativeSettings.getDefaultLabels())
                .endMetadata()
                .withNewSpec()
                    .withChannel(new KReferenceBuilder()
                            .withApiVersion(String.format("%s/%s", KnativeSupport.knativeMessagingGroup(), KnativeSupport.knativeApiVersion()))
                            .withKind("InMemoryChannel")
                            .withName(context.replaceDynamicContentInString(channelName))
                            .build())
                    .withNewSubscriber()
                        .withNewRef()
                            .withApiVersion("v1")
                            .withKind("Service")
                            .withName(context.replaceDynamicContentInString(serviceName))
                        .endRef()
                    .endSubscriber()
                .endSpec()
                .build();

        getKnativeClient().subscriptions()
                .inNamespace(namespace(context))
                .resource(subscription)
                .createOr(Updatable::update);

        if (isAutoRemoveResources()) {
            context.doFinally(knative().client(getKubernetesClient()).client(getKnativeClient())
                    .subscriptions()
                    .delete(subscriptionName)
                    .inNamespace(getNamespace()));
        }
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return KubernetesSettings.isLocal(clusterType(context));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<CreateSubscriptionAction, Builder> {

        private String subscriptionName;
        private String channelName;
        private String serviceName;

        public Builder subscription(String subscriptionName) {
            this.subscriptionName = subscriptionName;
            return this;
        }

        public Builder channel(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public CreateSubscriptionAction doBuild() {
            if (subscriptionName == null) {
                subscriptionName = serviceName + "subscription";
            }

            return new CreateSubscriptionAction(this);
        }
    }
}
