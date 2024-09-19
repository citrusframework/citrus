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
import org.citrusframework.annotations.CitrusTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class CreateKnativeBrokerIT extends AbstractKnativeIT {

    @Autowired
    private KnativeClient knativeClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateBroker() {
        given(doFinally().actions(context -> knativeClient.brokers()
                .inNamespace(namespace)
                .withName("my-broker")
                .delete()));

        when(knative()
                .client(knativeClient)
                .brokers()
                .create("my-broker")
                .inNamespace(namespace));

        then(context -> {
            Broker broker = knativeClient.brokers()
                    .inNamespace(namespace)
                    .withName("my-broker")
                    .get();

            Assert.assertNotNull(broker);
        });
    }

}
