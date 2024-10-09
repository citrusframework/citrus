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

import java.util.HashMap;
import java.util.Map;

import io.fabric8.knative.eventing.v1.Trigger;
import io.fabric8.knative.eventing.v1.TriggerBuilder;
import io.fabric8.knative.eventing.v1.TriggerSpecBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class CreateTriggerAction extends AbstractKnativeAction {

    private final String triggerName;
    private final String serviceName;
    private final String brokerName;
    private final String channelName;

    private final Map<String, String> filterOnAttributes;

    public CreateTriggerAction(Builder builder) {
        super("create-trigger", builder);

        this.triggerName = builder.triggerName;
        this.serviceName = builder.serviceName;
        this.brokerName = builder.brokerName;
        this.channelName = builder.channelName;
        this.filterOnAttributes = builder.filterOnAttributes;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedTriggerName = context.replaceDynamicContentInString(triggerName);
        TriggerSpecBuilder triggerSpec = new TriggerSpecBuilder()
                .withBroker(brokerName(brokerName, context));

        addServiceSubscriber(triggerSpec, context);
        addChannelSubscriber(triggerSpec, context);
        addFilterOnAttributes(triggerSpec, context);

        Trigger trigger = new TriggerBuilder()
                .withApiVersion(String.format("%s/%s", KnativeSupport.knativeEventingGroup(), KnativeSupport.knativeApiVersion()))
                .withNewMetadata()
                    .withNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(resolvedTriggerName))
                    .withLabels(KnativeSettings.getDefaultLabels())
                .endMetadata()
                .withSpec(triggerSpec.build())
                .build();

        getKnativeClient().triggers()
                .inNamespace(namespace(context))
                .resource(trigger)
                .createOr(Updatable::update);

        if (isAutoRemoveResources()) {
            context.doFinally(knative().client(getKubernetesClient()).client(getKnativeClient())
                    .trigger()
                    .delete(resolvedTriggerName)
                    .inNamespace(getNamespace()));
        }
    }

    private void addFilterOnAttributes(TriggerSpecBuilder triggerSpec, TestContext context) {
        if (!filterOnAttributes.isEmpty()) {
            triggerSpec.withNewFilter()
                    .withAttributes(context.resolveDynamicValuesInMap(filterOnAttributes))
                    .endFilter();
        }
    }

    private void addChannelSubscriber(TriggerSpecBuilder triggerSpec, TestContext context) {
        if (channelName != null) {
            triggerSpec.withNewSubscriber()
                    .withNewRef()
                        .withApiVersion(String.format("%s/%s", KnativeSupport.knativeMessagingGroup(), KnativeSupport.knativeApiVersion()))
                        .withKind("InMemoryChannel")
                        .withName(context.replaceDynamicContentInString(channelName))
                    .endRef()
                    .endSubscriber();
        }
    }

    private void addServiceSubscriber(TriggerSpecBuilder triggerSpec, TestContext context) {
        if (serviceName != null) {
            triggerSpec.withNewSubscriber()
                    .withNewRef()
                        .withApiVersion("v1")
                        .withKind("Service")
                        .withName(context.replaceDynamicContentInString(serviceName))
                    .endRef()
                    .endSubscriber();
        }
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return KubernetesSettings.isLocal(clusterType(context));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<CreateTriggerAction, Builder> {

        private String triggerName;
        private String serviceName;
        private String brokerName;
        private String channelName;

        private final Map<String, String> filterOnAttributes = new HashMap<>();

        public Builder trigger(String triggerName) {
            this.triggerName = triggerName;
            return this;
        }

        public Builder broker(String brokerName) {
            this.brokerName = brokerName;
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

        public Builder filter(Map<String, String> filter) {
            this.filterOnAttributes.putAll(filter);
            return this;
        }

        public Builder filter(String attributeName, String value) {
            this.filterOnAttributes.put(attributeName, value);
            return this;
        }

        @Override
        public CreateTriggerAction doBuild() {
            return new CreateTriggerAction(this);
        }
    }
}
