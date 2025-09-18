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

import java.util.Map;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.CucumberSettings;
import org.citrusframework.cucumber.steps.kubernetes.KubernetesSteps;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeVariableNames;
import org.citrusframework.knative.ce.CloudEventMessage;
import org.citrusframework.knative.ce.CloudEventSupport;
import org.springframework.http.HttpStatus;

public class ReceiveEventSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @CitrusFramework
    private Citrus citrus;

    private String eventData;

    private KubernetesSteps kubernetesSteps;

    @Before
    public void before(Scenario scenario) {
        kubernetesSteps = new KubernetesSteps();
        CitrusAnnotations.injectAll(kubernetesSteps, citrus, context);
        CitrusAnnotations.injectTestRunner(kubernetesSteps, runner);
        kubernetesSteps.before(scenario);
        kubernetesSteps.configureTimeout(KnativeSettings.getEventConsumerTimeout());
        kubernetesSteps.setServiceName(KnativeSettings.getServiceName());
        kubernetesSteps.setServicePort(String.valueOf(KnativeSettings.getServicePort()));
    }

    @Given("^Knative service \"([^\"\\s]+)\"$")
    public void setServiceName(String name) {
        kubernetesSteps.setServiceName(name);
    }

    @Given("^Knative service port ([^\\s]+)$")
    public void setServicePort(String port) {
        kubernetesSteps.setServicePort(port);
    }

    @Given("^Knative event consumer timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        kubernetesSteps.configureTimeout(timeout);
    }

    @Given("^(?:expect|verify) Knative event data$")
    public void setEventDataMultiline(String data) {
        setEventData(data);
    }

    @Given("^(?:expect|verify) Knative event data: (.+)$")
    public void setEventData(String data) {
        this.eventData = data;
    }

    @Then("^(?:receive|verify) Knative event$")
    public void receiveEvent(DataTable attributes) {
        receiveEvent(CloudEventSupport.createEventMessage(eventData, attributes.asMap(String.class, Object.class)));
    }

    @Then("^(?:receive|verify) Knative event as json$")
    public void receiveEventJson(String json) {
        Map<String, Object> attributes = CloudEventSupport.attributesFromJson(json)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        receiveEvent(CloudEventSupport.createEventMessage(eventData, attributes));
    }

    @Given("^create Knative event consumer service ([^\\s]+)$")
    public void createService(String serviceName) {
        if (CucumberSettings.isLocal() && context.getVariables().containsKey(KnativeVariableNames.BROKER_NAME.value()) &&
                context.getReferenceResolver().isResolvable(context.getVariable(KnativeVariableNames.BROKER_NAME.value()))) {
            HttpServer brokerServer = context.getReferenceResolver().resolve(context.getVariable(KnativeVariableNames.BROKER_NAME.value()), HttpServer.class);
            context.getReferenceResolver().bind(serviceName, brokerServer);
            setServiceName(serviceName);
            setServicePort(String.valueOf(brokerServer.getPort()));
        } else {
            kubernetesSteps.createService(serviceName);
        }
    }

    @Given("^create Knative event consumer service ([^\\s]+) with target port ([^\\s]+)$")
    public void createService(String serviceName, String targetPort) {
        if (CucumberSettings.isLocal() && context.getVariables().containsKey(KnativeVariableNames.BROKER_NAME.value()) &&
                context.getReferenceResolver().isResolvable(context.getVariable(KnativeVariableNames.BROKER_NAME.value()))) {
            HttpServer brokerServer = context.getReferenceResolver().resolve(context.getVariable(KnativeVariableNames.BROKER_NAME.value()), HttpServer.class);
            context.getReferenceResolver().bind(serviceName, brokerServer);
            setServiceName(serviceName);
            setServicePort(String.valueOf(brokerServer.getPort()));
        } else {
            kubernetesSteps.createService(serviceName, targetPort);
        }
    }

    /**
     * Receives cloud event as Http request.
     */
    private void receiveEvent(CloudEventMessage request) {
        kubernetesSteps.receiveServiceRequest(request);
        kubernetesSteps.sendServiceResponse(HttpStatus.ACCEPTED);
    }
}
