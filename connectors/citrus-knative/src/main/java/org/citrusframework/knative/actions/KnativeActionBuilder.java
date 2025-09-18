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

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.knative.KnativeBrokerActionBuilder;
import org.citrusframework.actions.knative.KnativeChannelActionBuilder;
import org.citrusframework.actions.knative.KnativeEventActionBuilder;
import org.citrusframework.actions.knative.KnativeSubscriptionActionBuilder;
import org.citrusframework.actions.knative.KnativeTriggerActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.knative.actions.eventing.CreateBrokerAction;
import org.citrusframework.knative.actions.eventing.CreateTriggerAction;
import org.citrusframework.knative.actions.eventing.DeleteBrokerAction;
import org.citrusframework.knative.actions.eventing.DeleteTriggerAction;
import org.citrusframework.knative.actions.eventing.ReceiveEventAction;
import org.citrusframework.knative.actions.eventing.SendEventAction;
import org.citrusframework.knative.actions.eventing.VerifyBrokerAction;
import org.citrusframework.knative.actions.messaging.CreateChannelAction;
import org.citrusframework.knative.actions.messaging.CreateSubscriptionAction;
import org.citrusframework.util.ObjectHelper;

public class KnativeActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<KnativeAction>,
        org.citrusframework.actions.knative.KnativeActionBuilder<KnativeAction, KnativeActionBuilder> {

    /** Kubernetes client */
    private KubernetesClient kubernetesClient;
    private KnativeClient knativeClient;

    private AbstractKnativeAction.Builder<? extends KnativeAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     */
    public static KnativeActionBuilder knative() {
        return new KnativeActionBuilder();
    }

    /**
     * Use a custom Kubernetes client.
     */
    public KnativeActionBuilder client(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        return this;
    }

    /**
     * Use a custom Knative client.
     */
    public KnativeActionBuilder client(KnativeClient knativeClient) {
        this.knativeClient = knativeClient;
        return this;
    }

    @Override
    public KnativeActionBuilder client(Object o) {
        if (o instanceof KnativeClient client) {
            this.knativeClient = client;
        } else if (o instanceof KubernetesClient client) {
            this.kubernetesClient = client;
        } else {
            throw new CitrusRuntimeException(("Unsupported client type, expected " +
                    "KnativeClient or KubernetesClient, but got: %s").formatted(o.getClass().getName()));
        }

        return this;
    }

    @Override
    public EventsActionBuilder event() {
        return new EventsActionBuilder();
    }

    @Override
    public ChannelActionBuilder channels() {
        return new ChannelActionBuilder();
    }

    @Override
    public SubscriptionActionBuilder subscriptions() {
        return new SubscriptionActionBuilder();
    }

    @Override
    public TriggerActionBuilder trigger() {
        return new TriggerActionBuilder();
    }

    @Override
    public BrokerActionBuilder brokers() {
        return new BrokerActionBuilder();
    }

    @Override
    public KnativeAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        if (kubernetesClient != null) {
            delegate.client(kubernetesClient);
        }

        if (knativeClient != null) {
            delegate.client(knativeClient);
        }
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    public class EventsActionBuilder implements KnativeEventActionBuilder {

        @Override
        public SendEventAction.Builder send() {
            SendEventAction.Builder builder = new SendEventAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public ReceiveEventAction.Builder receive() {
            ReceiveEventAction.Builder builder = new ReceiveEventAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class ChannelActionBuilder implements KnativeChannelActionBuilder {

        @Override
        public CreateChannelAction.Builder create(String channelName) {
            CreateChannelAction.Builder builder = new CreateChannelAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .channel(channelName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteKnativeResourceAction.Builder delete(String channelName) {
            DeleteKnativeResourceAction.Builder builder = new DeleteKnativeResourceAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .component("messaging")
                    .kind("channels")
                    .resource(channelName);
            delegate = builder;
            return builder;
        }
    }

    public class SubscriptionActionBuilder implements KnativeSubscriptionActionBuilder {

        @Override
        public CreateSubscriptionAction.Builder create(String subscriptionName) {
            CreateSubscriptionAction.Builder builder = new CreateSubscriptionAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .subscription(subscriptionName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteKnativeResourceAction.Builder delete(String subscriptionName) {
            DeleteKnativeResourceAction.Builder builder = new DeleteKnativeResourceAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .component("messaging")
                    .kind("subscriptions")
                    .resource(subscriptionName);
            delegate = builder;
            return builder;
        }
    }

    public class TriggerActionBuilder implements KnativeTriggerActionBuilder {

        @Override
        public CreateTriggerAction.Builder create(String triggerName) {
            CreateTriggerAction.Builder builder = new CreateTriggerAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .trigger(triggerName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteTriggerAction.Builder delete(String triggerName) {
            DeleteTriggerAction.Builder builder = new DeleteTriggerAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .trigger(triggerName);
            delegate = builder;
            return builder;
        }
    }

    public class BrokerActionBuilder implements KnativeBrokerActionBuilder {

        @Override
        public CreateBrokerAction.Builder create(String brokerName) {
            CreateBrokerAction.Builder builder = new CreateBrokerAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .broker(brokerName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteBrokerAction.Builder delete(String brokerName) {
            DeleteBrokerAction.Builder builder = new DeleteBrokerAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .broker(brokerName);
            delegate = builder;
            return builder;
        }

        @Override
        public VerifyBrokerAction.Builder verify(String brokerName) {
            VerifyBrokerAction.Builder builder = new VerifyBrokerAction.Builder()
                    .client(kubernetesClient)
                    .client(knativeClient)
                    .broker(brokerName);
            delegate = builder;
            return builder;
        }
    }
}
