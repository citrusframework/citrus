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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.http.HttpServerSteps;
import org.citrusframework.cucumber.steps.util.ResourceUtils;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.kubernetes.KubernetesVariableNames;
import org.citrusframework.kubernetes.actions.CreateServiceAction;
import org.citrusframework.kubernetes.actions.VerifyPodAction;
import org.citrusframework.kubernetes.model.KubernetesResource;
import org.citrusframework.util.FileUtils;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    private HttpServerSteps httpServerSteps;

    private String servicePort = String.valueOf(KubernetesSettings.getServicePort());
    private String serviceName = KubernetesSettings.getServiceName();

    private KubernetesClient k8sClient;

    private boolean autoRemoveResources = KubernetesSettings.isAutoRemoveResources();
    private int maxAttempts = KubernetesSettings.getMaxAttempts();
    private long delayBetweenAttempts = KubernetesSettings.getDelayBetweenAttempts();

    private Duration watchLogsTimeout = Duration.ofMillis(KubernetesSettings.getWatchLogsTimeout());

    @Before
    public void before(Scenario scenario) {
        httpServerSteps = new HttpServerSteps();
        CitrusAnnotations.injectAll(httpServerSteps, citrus, context);
        CitrusAnnotations.injectTestRunner(httpServerSteps, runner);
        httpServerSteps.before(scenario);

        httpServerSteps.configureTimeout(KubernetesSettings.getServiceTimeout());
        httpServerSteps.setServerPort(Integer.parseInt(context.replaceDynamicContentInString(servicePort)));
        httpServerSteps.setServer(serviceName);

        if (k8sClient == null) {
            k8sClient = KubernetesSupport.getKubernetesClient(citrus);
        }
    }

    @Given("^Disable auto removal of Kubernetes resources$")
    public void disableAutoRemove() {
        autoRemoveResources = false;
    }

    @Given("^Kubernetes resource polling configuration$")
    public void configureResourcePolling(Map<String, Object> configuration) {
        maxAttempts = Integer.parseInt(configuration.getOrDefault("maxAttempts", maxAttempts).toString());
        delayBetweenAttempts = Long.parseLong(configuration.getOrDefault("delayBetweenAttempts", delayBetweenAttempts).toString());
    }

    @Given("^Kubernetes namespace ([^\\s]+)$")
    public void setNamespace(String namespace) {
        // update the test variable that points to the namespace
        runner.run(createVariable(KubernetesVariableNames.NAMESPACE.value(), namespace));
    }

    @Given("^Kubernetes timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        httpServerSteps.configureTimeout(timeout);
    }

    @Given("^Kubernetes service \"([^\"\\s]+)\"$")
    public void setServiceName(String name) {
        this.serviceName = name;
        httpServerSteps.setServer(name);
    }

    @Given("^Kubernetes service port ([^\\s]+)$")
    public void setServicePort(String port) {
        this.servicePort = port;
        httpServerSteps.setServerPort(Integer.parseInt(context.replaceDynamicContentInString(port)));
    }

    @Given("^create Kubernetes custom resource in ([^\\s]+)$")
    public void createCustomResource(String resourceType, String yaml) {
        KubernetesResource resource = KubernetesSupport.yaml().loadAs(yaml, KubernetesResource.class);

        runner.run(kubernetes().client(k8sClient)
                .customResources()
                .create()
                .type(resourceType)
                .kind(resource.getKind())
                .apiVersion(resource.getApiVersion())
                .content(yaml));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .customResources()
                            .delete(resource.getMetadata().getName())
                            .type(resourceType)
                            .kind(resource.getKind())
                            .apiVersion(resource.getApiVersion())));
        }
    }

    @Given("^load Kubernetes custom resource ([^\\s]+) in ([^\\s]+)$")
    public void createCustomResourceFromFile(String fileName, String resourceType) {
        try {
            createCustomResource(resourceType, FileUtils.readToString(ResourceUtils.resolve(fileName, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read custom resource from file", e);
        }
    }

    @Given("^delete Kubernetes custom resource in ([^\\s]+)$")
    public void deleteCustomResource(String resourceType, String yaml) {
        KubernetesResource resource = KubernetesSupport.yaml().loadAs(yaml, KubernetesResource.class);

        runner.run(kubernetes().client(k8sClient)
                        .customResources()
                        .delete(resource.getMetadata().getName())
                        .type(resourceType)
                        .kind(resource.getKind())
                        .apiVersion(resource.getApiVersion()));
    }

    @Given("^delete Kubernetes custom resource ([^\\s]+) in ([^\\s]+)$")
    public void deleteCustomResourceFromFile(String fileName, String resourceType) {
        try {
            deleteCustomResource(resourceType, FileUtils.readToString(ResourceUtils.resolve(fileName, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read custom resource from file", e);
        }
    }

    @Given("^create Kubernetes resource$")
    public void createResource(String content) {
        runner.run(kubernetes().client(k8sClient)
                .resources()
                .create()
                .content(content));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .resources()
                            .delete(content)));
        }
    }

    @Given("^load Kubernetes resource ([^\\s]+)$")
    public void createResourceFromFile(String fileName) {
        try {
            createResource(FileUtils.readToString(ResourceUtils.resolve(fileName, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read resource from file", e);
        }
    }

    @Given("^delete Kubernetes resource$")
    public void deleteResource(String yaml) {
        runner.run(kubernetes().client(k8sClient)
                .resources()
                .delete(yaml));
    }

    @Given("^delete Kubernetes resource ([^\\s]+)$")
    public void deleteResourceFromFile(String fileName) {
        try {
            deleteResource(FileUtils.readToString(ResourceUtils.resolve(fileName, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read resource from file", e);
        }
    }

    @Given("^create Kubernetes secret ([^\\s]+)$")
    public void createSecret(String name, Map<String, String> properties) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .create(name)
                .properties(properties));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .secrets()
                            .delete(name)));
        }
    }

    @Given("^expose Http server certificates as Kubernetes secret ([^\\s]+)$")
    public void createSecretFromHttpServerCertificates(String name) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .create(name)
                .fromFile("classpath:keystore/server.jks")
                .fromFile("classpath:keystore/server.crt")
                .fromFile("classpath:keystore/server.key"));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .secrets()
                            .delete(name)));
        }
    }

    @Given("^expose Http client certificates as Kubernetes secret ([^\\s]+)$")
    public void createSecretFromHttpClientCertificates(String name) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .create(name)
                .fromFile("classpath:keystore/client.jks")
                .fromFile("classpath:keystore/client.crt")
                .fromFile("classpath:keystore/client.key"));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .secrets()
                            .delete(name)));
        }
    }

    @Given("^expose Http certification authority as Kubernetes secret ([^\\s]+)$")
    public void createSecretFromHttpCertificationAuthority(String name) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .create(name)
                .fromFile("classpath:keystore/ca.crt")
                .fromFile("classpath:keystore/ca.key"));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .secrets()
                            .delete(name)));
        }
    }

    @Given("^load Kubernetes secret from file ([^\\s]+)$")
    public void createSecret(String filePath) {
        createSecret(FileUtils.getBaseName(FileUtils.getFileName(filePath)), filePath);
    }

    @Given("^create Kubernetes secret ([^\\s]+) from file ([^\\s]+)$")
    public void createSecret(String secretName, String filePath) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .create(secretName)
                .fromFile(filePath));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .secrets()
                            .delete(secretName)));
        }
    }

    @Given("^create Kubernetes service$")
    public void createService() {
        createService(serviceName, servicePort);
    }

    @Given("^create Kubernetes service ([^\\s]+)$")
    public void createService(String serviceName) {
        createService(serviceName, servicePort);
    }

    @Given("^create Kubernetes service ([^\\s]+) with target port ([^\\s]+)$")
    public void createService(String serviceName, String targetPort) {
        createService(serviceName, "80", targetPort);
    }

    @Given("^create Kubernetes service ([^\\s]+) with port mapping ([^\\s]+):([^\\s]+)$")
    public void createService(String serviceName, String port, String targetPort) {
        initializeService(serviceName, targetPort);

        runner.given(kubernetes().client(k8sClient)
                .services()
                .create(serviceName)
                .port(port)
                .targetPort(targetPort));

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .services()
                            .delete(serviceName)));
        }
    }

    @Given("^create Kubernetes service ([^\\s]+) with port mappings$")
    public void createServiceWithPortMappings(String serviceName, DataTable portMappings) {
        CreateServiceAction.Builder createServiceAction = kubernetes().client(k8sClient)
                .services()
                .create(serviceName);

        Map<String, String> mappings = portMappings.asMap(String.class, String.class);

        initializeService(serviceName, mappings.values().iterator().next());

        mappings.forEach(createServiceAction::portMapping);

        runner.run(createServiceAction);

        if (autoRemoveResources) {
            runner.then(doFinally()
                    .actions(kubernetes().client(k8sClient)
                            .services()
                            .delete(serviceName)));
        }
    }

    @Given("^delete Kubernetes service ([^\\s]+)$")
    public void deleteService(String serviceName) {
        runner.run(kubernetes()
                .client(k8sClient)
                .services()
                .delete(serviceName));
    }

    @Given("^delete Kubernetes secret ([^\\s]+)$")
    public void deleteSecret(String secretName) {
        runner.run(kubernetes().client(k8sClient)
                .secrets()
                .delete(secretName));
    }

    @Given("^wait for condition=([^\\s]+) on Kubernetes custom resource ([^\\s]+) in ([^\\s]+)$")
    public void resourceShouldMatchCondition(String condition, String name, String resourceType) {
        runner.run(kubernetes().client(k8sClient)
                .customResources()
                .verify(name)
                .type(resourceType)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .condition(condition));
    }

    @Given("^Kubernetes custom resource ([^\\s]+) in ([^\\s]+) is ready")
    @Then("^Kubernetes custom resource ([^\\s]+) in ([^\\s]+) should be ready")
    public void resourceShouldBeReady(String name, String resourceType) {
        resourceShouldMatchCondition("Ready", name, resourceType);
    }

    @Given("^Kubernetes custom resource ([^\\s]+) is ready")
    @Then("^Kubernetes custom resource ([^\\s]+) should be ready")
    public void resourceShouldBeReady(String name, DataTable table) {
        List<List<String>> cells = new ArrayList<>(table.cells());
        cells.add(Arrays.asList("name", name));
        resourceShouldMatchConditionWithConfiguration("Ready", DataTable.create(cells, table.getTableConverter()));
    }

    @Given("^wait for condition=([^\\s]+) on Kubernetes custom resource$")
    public void resourceShouldMatchConditionWithConfiguration(String condition, DataTable table) {
        Map<String, String> configuration = table.asMap(String.class, String.class);

        if (!configuration.containsKey("kind")) {
            throw new CitrusRuntimeException("Invalid custom resource type configuration - must use proper \"kind\" setting");
        }

        String kind = configuration.get("kind");
        String apiVersion = configuration.getOrDefault("apiVersion", "");
        String group = configuration.getOrDefault("group", apiVersion.length() > 0 ? apiVersion.substring(0, apiVersion.indexOf("/")) : "");
        String version = configuration.getOrDefault("version", apiVersion.length() > 0 ? apiVersion.substring(apiVersion.indexOf("/") + 1) : "");
        String resourceType = configuration.getOrDefault("type", String.format("%ss.%s/%s", kind.toLowerCase(Locale.ENGLISH), group, version));

        if (configuration.containsKey("name")) {
            String name = configuration.get("name");

            if (!name.contains("/")) {
                name = kind + "/" +name;
            }
            resourceShouldMatchCondition(condition, name, resourceType);
        } else if (configuration.containsKey("label")) {
            String labelExpression = configuration.get("label");

            String[] tokens = labelExpression.split("=");
            String labelKey = tokens[0];
            String labelValue = tokens.length > 1 ? tokens[1] : "";

            resourceLabeledShouldMatchCondition(condition, kind, resourceType, labelKey, labelValue);
        } else {
            throw new CitrusRuntimeException("Invalid custom resource type configuration - must identify resource via \"name\" or \"label\"");
        }
    }


    @Given("^Kubernetes custom resource is ready")
    @Then("^Kubernetes custom resource should be ready")
    public void resourceConfiguredShouldBeReady(DataTable table) {
        resourceShouldMatchConditionWithConfiguration("Ready", table);
    }

    @Given("^wait for condition=([^\\s]+) on Kubernetes custom resource ([^\\s]+) in ([^\\s]+) labeled with ([^\\s]+)=([^\\s]+)$")
    public void resourceLabeledShouldMatchCondition(String condition, String kind, String resourceType, String label, String value) {
        runner.run(kubernetes().client(k8sClient)
                .customResources()
                .verify(label, value)
                .kind(kind)
                .type(resourceType)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .condition(condition));
    }

    @Given("^Kubernetes custom resource ([^\\s]+) in ([^\\s]+) labeled with ([^\\s]+)=([^\\s]+) is ready")
    @Then("^Kubernetes custom resource ([^\\s]+) in ([^\\s]+) labeled with ([^\\s]+)=([^\\s]+) should be ready")
    public void resourceLabeledShouldBeReady(String kind, String resourceType, String label, String value) {
        resourceLabeledShouldMatchCondition("Ready", kind, resourceType, label, value);
    }

    @Given("^wait for Kubernetes pod ([a-z\\.0-9-]+)$")
    public void waitForRunningPod(String name) {
        podShouldBeInPhase(name, "running");
    }

    @Given("^Kubernetes pod ([a-z\\.0-9-]+) is (running|stopped)$")
    @Then("^Kubernetes pod ([a-z\\.0-9-]+) should be (running|stopped)$")
    public void podShouldBeInPhase(String name, String status) {
        VerifyPodAction.Builder action = kubernetes().client(k8sClient)
                .pods()
                .verify(name)
                .printLogs(KubernetesSettings.isPrintPodLogs())
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts);

        if (status.equals("running")) {
            action.isRunning();
        } else {
            action.isStopped();
        }

        runner.run(action);
    }

    @Given("^wait for Kubernetes pod labeled with ([^\\s]+)=([^\\s]+)$")
    public void waitForRunningPod(String label, String value) {
        podByLabelShouldBeInPhase(label, value, "running");
    }

    @Given("^Kubernetes pod labeled with ([^\\s]+)=([^\\s]+) is (running|stopped)$")
    @Then("^Kubernetes pod labeled with ([^\\s]+)=([^\\s]+) should be (running|stopped)$")
    public void podByLabelShouldBeInPhase(String label, String value, String status) {
        VerifyPodAction.Builder action = kubernetes().client(k8sClient)
                .pods()
                .verify(label, value)
                .printLogs(KubernetesSettings.isPrintPodLogs())
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts);

        if (status.equals("running")) {
            action.isRunning();
        } else {
            action.isStopped();
        }

        runner.run(action);
    }

    @Then("^Kubernetes pod ([a-z\\.0-9-]+) should print (.*)$")
    public void podShouldPrint(String name, String message) {
        runner.run(kubernetes().client(k8sClient)
                .pods()
                .verify(name)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .waitForLogMessage(message));
    }

    @Then("^Kubernetes pod ([a-z\\.0-9-]+) should not print (.*)$")
    public void podShouldNotPrint(String name, String message) {
        runner.run(assertException()
                .exception(ActionTimeoutException.class)
                .when(kubernetes().client(k8sClient)
                        .pods()
                        .verify(name)
                        .maxAttempts(maxAttempts)
                        .delayBetweenAttempts(delayBetweenAttempts)
                        .waitForLogMessage(message)));
    }

    @Then("^Kubernetes pod labeled with ([^\\s]+)=([^\\s]+) should print (.*)$")
    public void podByLabelShouldPrint(String label, String value, String message) {
        runner.run(kubernetes().client(k8sClient)
                .pods()
                .verify(label, value)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .waitForLogMessage(message));
    }

    @Then("^Kubernetes pod labeled with ([^\\s]+)=([^\\s]+) should not print (.*)$")
    public void podByLabelShouldNotPrint(String label, String value, String message) {
        runner.run(assertException()
                .exception(ActionTimeoutException.class)
                .when(kubernetes().client(k8sClient)
                        .pods()
                        .verify(label, value)
                        .maxAttempts(maxAttempts)
                        .delayBetweenAttempts(delayBetweenAttempts)
                        .waitForLogMessage(message)));
    }

    @Given("^watch logs timeout is ([\\d]+)(ms|s|min)$")
    public void watchPodLogs(Long time, String timeUnit) {
        switch (timeUnit) {
            case "ms":
                watchLogsTimeout = Duration.ofMillis(time);
                break;
            case "s":
                watchLogsTimeout = Duration.ofSeconds(time);
                break;
            case "min":
                watchLogsTimeout = Duration.ofMinutes(time);
                break;
        }
    }

    @Given("^watch logs for Kubernetes pod ([a-z\\.0-9-]+)$")
    public void watchPodLogs(String name) {
        runner.run(kubernetes().client(k8sClient)
                .pods()
                .watchLogs(name)
                .timeout(watchLogsTimeout));
    }

    @Given("^watch logs for Kubernetes pod labeled with ([^\\s]+)=([^\\s]+)$")
    public void watchPodLogs(String label, String value) {
        runner.run(kubernetes().client(k8sClient)
                .pods()
                .watchLogs(label, value)
                .timeout(watchLogsTimeout));
    }

    @Then("^create annotation ([^\\s]+)=([^\\s]+) on Kubernetes (pod|secret|service|deployment) ([a-z\\.0-9-]+)$")
    public void createAnnotationOnResource(String annotation, String value, String resourceType, String resourceName) {
        runner.run(kubernetes().client(k8sClient)
                        .resources()
                        .addAnnotation(resourceName, resourceType.toUpperCase(Locale.US))
                        .annotation(annotation, value));
    }

    @Then("^create annotations on Kubernetes (pod|secret|service|deployment) ([a-z\\.0-9-]+)$")
    public void createAnnotationOnResource(String resourceType, String resourceName, DataTable table) {
        Map<String, String> annotations = table.asMap(String.class, String.class);
        runner.run(kubernetes().client(k8sClient)
                .resources()
                .addAnnotation(resourceName, resourceType.toUpperCase(Locale.US))
                .annotations(annotations));
    }

    @Then("^create label ([^\\s]+)=([^\\s]+) on Kubernetes (pod|secret|service|deployment) ([a-z\\.0-9-]+)$")
    public void createLabelOnResource(String label, String value, String resourceType, String resourceName) {
        runner.run(kubernetes().client(k8sClient)
                        .resources()
                        .addLabel(resourceName, resourceType.toUpperCase(Locale.US))
                        .label(label, value));
    }

    @Then("^create labels on Kubernetes (pod|secret|service|deployment) ([a-z\\.0-9-]+)$")
    public void createLabelOnResource(String resourceType, String resourceName, DataTable table) {
        createLabelsOnResource(resourceType, resourceName, table.asMap(String.class, String.class));
    }

    public void createLabelsOnResource(String resourceType, String resourceName, Map<String, String> labels) {
        runner.run(kubernetes().client(k8sClient)
                .resources()
                .addLabel(resourceName, resourceType.toUpperCase(Locale.US))
                .labels(labels));
    }

    /**
     * Receive and verify given request.
     * @param request
     */
    public void receiveServiceRequest(HttpMessage request) {
        httpServerSteps.setRequestMessageType(request.getType());
        httpServerSteps.receiveServerRequest(request);
    }

    /**
     * Send given response status.
     * @param status
     */
    public void sendServiceResponse(HttpStatus status) {
        httpServerSteps.sendServerResponse(status);
    }

    private void initializeService(String serviceName, String targetPort) {
        setServiceName(serviceName);
        setServicePort(targetPort);
        httpServerSteps.startServer();
    }
}
