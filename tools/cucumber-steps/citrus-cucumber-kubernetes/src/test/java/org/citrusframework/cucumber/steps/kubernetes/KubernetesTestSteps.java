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

package org.citrusframework.cucumber.steps.kubernetes;

import java.util.Collections;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.assertj.core.api.Assertions;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.kubernetes.actions.KubernetesAction;
import org.citrusframework.kubernetes.actions.KubernetesActionBuilder;

public class KubernetesTestSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    @Then("^verify broker ([^\\s]+) exists$")
    public void verifyBroker(String brokerName) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertThat(getKubernetesClient()
                        .genericKubernetesResources(KubernetesSupport.crdContext("brokers", "eventing.knative.dev", "Broker", "v1"))
                        .inNamespace(namespace(context))
                        .withName(brokerName)
                        .get()).isNotNull();
            }
        });
    }

    @Then("^verify pod ([^\\s]+) exists$")
    public void verifyPod(String podName) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertThat(getKubernetesClient()
                        .pods()
                        .inNamespace(namespace(context))
                        .withName(podName)
                        .get()).isNotNull();
            }
        });
    }

    @Then("^verify secret ([^\\s]+) exists$")
    public void verifySecret(String name) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertThat(getKubernetesClient()
                        .secrets()
                        .inNamespace(namespace(context))
                        .withName(name)
                        .get()).isNotNull();
            }
        });
    }

    @Then("^verify Kubernetes service ([^\\s]+) exists$")
    public void verifyService(String serviceName) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertThat(getKubernetesClient()
                        .services()
                        .inNamespace(namespace(context))
                        .withName(serviceName)
                        .get()).isNotNull();
            }
        });
    }

    @Given("^Kubernetes pod ([a-z0-9-]+) in phase (Running|Stopped)$")
    public void createPod(String podName, String status) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Pod pod = new PodBuilder()
                        .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace(context))
                        .endMetadata()
                        .withNewStatus()
                        .withPhase(status)
                        .endStatus()
                        .build();

                getKubernetesClient().pods().inNamespace(namespace(context)).resource(pod).create();
            }
        });
    }

    @Given("^Kubernetes pod ([a-z0-9-]+)$")
    public void createPod(String podName) {
        createPod(podName, "citrusframework.org/pod", podName);
    }

    @Given("^Kubernetes pod ([a-z0-9-]+) with label ([^\\s]+)=([^\\s]+)$")
    public void createPod(String podName, String label, String value) {
        runner.run(new KubernetesTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Pod pod = new PodBuilder()
                        .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace(context))
                        .withLabels(Collections.singletonMap(label, value))
                        .endMetadata()
                        .withNewStatus()
                        .withPhase("Running")
                        .endStatus()
                        .build();

                getKubernetesClient().pods().inNamespace(namespace(context)).resource(pod).create();
            }
        });
    }

    private abstract class KubernetesTestAction extends AbstractTestAction implements KubernetesAction {
        @Override
        public KubernetesClient getKubernetesClient() {
            return KubernetesSupport.getKubernetesClient(citrus);
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public boolean isAutoRemoveResources() {
            return KubernetesSettings.isAutoRemoveResources();
        }

        @Override
        public KubernetesActionBuilder kubernetes() {
            return new KubernetesActionBuilder()
                    .client(KubernetesSupport.getKubernetesClient(citrus));
        }
    }
}
