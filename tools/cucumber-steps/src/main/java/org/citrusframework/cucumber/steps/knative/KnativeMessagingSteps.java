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

package org.citrusframework.cucumber.steps.knative;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.kubernetes.KubernetesSupport;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class KnativeMessagingSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    private KubernetesClient k8sClient;
    private KnativeClient knativeClient;

    @Before
    public void before(Scenario scenario) {
        if (k8sClient == null) {
            k8sClient = KubernetesSupport.getKubernetesClient(citrus);
        }

        if (knativeClient == null) {
            knativeClient = KnativeSupport.getKnativeClient(citrus);
        }
    }

    @Given("^create Knative channel ([^\\s]+)$")
    public void createChannel(String channelName) {
        runner.given(knative().client(k8sClient).client(knativeClient)
                .channels()
                .create(channelName));

        if (KnativeSteps.autoRemoveResources) {
            runner.then(doFinally()
                    .actions(knative().client(k8sClient).client(knativeClient)
                            .channels()
                            .delete(channelName)));
        }
    }

    @Given("^delete Knative channel ([^\\s]+)$")
    public void deleteChannel(String channelName) {
        runner.given(knative().client(k8sClient).client(knativeClient)
                .channels()
                .delete(channelName));
    }

    @Given("^subscribe service ([^\\s]+) to Knative channel ([^\\s]+)$")
    public void createSubscription(String serviceName, String channelName) {
        runner.given(knative().client(k8sClient).client(knativeClient)
                .subscriptions()
                .create(serviceName + "-subscription")
                .channel(channelName)
                .service(serviceName));

        if (KnativeSteps.autoRemoveResources) {
            runner.then(doFinally()
                    .actions(knative().client(k8sClient).client(knativeClient)
                            .subscriptions()
                            .delete(serviceName + "-subscription")));
        }
    }
}
