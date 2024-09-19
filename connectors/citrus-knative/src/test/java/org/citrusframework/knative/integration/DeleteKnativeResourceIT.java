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

package org.citrusframework.knative.integration;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.internal.pkg.apis.duck.v1.DestinationBuilder;
import io.fabric8.knative.internal.pkg.apis.duck.v1.KReferenceBuilder;
import io.fabric8.knative.messaging.v1.Subscription;
import io.fabric8.knative.messaging.v1.SubscriptionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.annotations.CitrusTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class DeleteKnativeResourceIT extends AbstractKnativeIT {

    @Autowired
    private KubernetesClient k8sClient;

    @Autowired
    private KnativeClient knativeClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldDeleteResource() {
        given(context -> {
            Subscription subscription = new SubscriptionBuilder()
                    .withNewMetadata()
                    .withName("my-subscription")
                    .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                    .withChannel(new KReferenceBuilder()
                            .withApiVersion("messaging/v1")
                            .withKind("InMemoryChannel")
                            .withName("my-channel")
                            .build())
                    .withSubscriber(new DestinationBuilder()
                            .withNewRef()
                            .withApiVersion("v1")
                            .withKind("Service")
                            .withName("my-service")
                            .endRef()
                            .build())
                    .endSpec()
                    .build();

            knativeClient.subscriptions()
                .inNamespace(namespace)
                .resource(subscription)
                .create();
        });

        when(knative()
                .client(k8sClient)
                .client(knativeClient)
                .subscriptions()
                .delete("my-subscription")
                .inNamespace(namespace));

        then(context -> {
            Subscription subscription = knativeClient.subscriptions()
                    .inNamespace(namespace)
                    .withName("my-subscription")
                    .get();

            Assert.assertNull(subscription);
        });
    }

}
