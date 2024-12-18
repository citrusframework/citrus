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
import io.fabric8.knative.eventing.v1.Broker;
import io.fabric8.knative.eventing.v1.BrokerBuilder;
import io.fabric8.knative.pkg.apis.ConditionBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class VerifyKnativeBrokerIT extends AbstractKnativeIT {

    @Autowired
    private KnativeClient knativeClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldVerifyBroker() {
        given(doFinally().actions(context -> knativeClient.brokers()
                .inNamespace(namespace)
                .withName("my-broker")
                .delete()));

        given(context -> {
            Broker broker = new BrokerBuilder()
                    .withNewMetadata()
                    .withName("my-broker")
                    .withNamespace(namespace)
                    .endMetadata()
                    .withNewStatus()
                    .withConditions(new ConditionBuilder()
                            .withType("Ready")
                            .withStatus("true")
                            .build())
                    .endStatus()
                    .build();

            knativeClient.brokers()
                .inNamespace(namespace)
                .resource(broker)
                .create();
        });

        then(knative()
                .client(knativeClient)
                .brokers()
                .verify("my-broker")
                .inNamespace(namespace));
    }

    @Test
    @CitrusTest
    public void shouldVerifyBrokerReadyState() {
        given(doFinally().actions(context -> knativeClient.brokers()
                .inNamespace(namespace)
                .withName("my-broker")
                .delete()));

        given(context -> {
            Broker broker = new BrokerBuilder()
                    .withNewMetadata()
                    .withName("my-broker")
                    .withNamespace(namespace)
                    .endMetadata()
                    .withNewStatus()
                    .withConditions(new ConditionBuilder()
                            .withType("Ready")
                            .withStatus("false")
                            .build())
                    .endStatus()
                    .build();

            knativeClient.brokers()
                .inNamespace(namespace)
                .resource(broker)
                .create();
        });

        then(assertException().exception(ValidationException.class)
                .when(knative()
                .client(knativeClient)
                .brokers()
                .verify("my-broker")
                .inNamespace(namespace)));
    }

}
