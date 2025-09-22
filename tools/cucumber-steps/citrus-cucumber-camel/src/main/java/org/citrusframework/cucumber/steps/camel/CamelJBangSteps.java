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

package org.citrusframework.cucumber.steps.camel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.util.ResourceUtils;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.spi.Resource;

import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;

public class CamelJBangSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    private Map<String, String> properties;
    private Resource propertiesFile;
    private Map<String, String> envVars;
    private Resource envVarsFile;

    private List<String> resourceFiles;

    private boolean autoRemoveResources = CamelSettings.isAutoRemoveResources();
    private int maxAttempts = CamelSettings.getMaxAttempts();
    private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();
    private boolean stopOnErrorStatus = CamelSettings.isStopOnErrorStatus();

    @Before
    public void before(Scenario scenario) {
        properties = new LinkedHashMap<>();
        propertiesFile = null;
        envVars = new LinkedHashMap<>();
        envVarsFile = null;
        resourceFiles = new ArrayList<>();
    }

    @After
    public void after(Scenario scenario) {
    }

    @Given("^Disable auto removal of Camel JBang resources$")
    public void disableAutoRemove() {
        autoRemoveResources = false;
    }

    @Given("^Enable auto removal of Camel JBang resources$")
    public void enableAutoRemove() {
        autoRemoveResources = true;
    }

    @Given("^Disable Camel stop on error status$")
    public void disableStopOnErrorStatus() {
        stopOnErrorStatus = false;
    }

    @Given("^Enable Camel stop on error status$")
    public void enableStopOnErrorStatus() {
        stopOnErrorStatus = true;
    }

    @Given("^Camel resource polling configuration$")
    public void configureResourcePolling(Map<String, Object> configuration) {
        maxAttempts = Integer.parseInt(configuration.getOrDefault("maxAttempts", maxAttempts).toString());
        delayBetweenAttempts = Long.parseLong(configuration.getOrDefault("delayBetweenAttempts", delayBetweenAttempts).toString());
    }

    @Given("^Camel integration resource ([^\\s]+)")
    public void addIntegrationResource(String filePath) {
        resourceFiles.add(filePath);
    }

    @Given("^run Camel integration ([^\\s]+)\\.(groovy|xml|java)")
    public void loadIntegration(String fileName, String language) {
        CamelRunIntegrationAction.Builder runIntegration = camel().jbang()
                .run()
                .integrationName(fileName)
                .integration(ResourceUtils.resolve("%s.%s".formatted(fileName, language), context))
                .withSystemProperties(properties)
                .withSystemProperties(propertiesFile)
                .withEnvs(envVars)
                .withEnvs(envVarsFile);

        resourceFiles.forEach(runIntegration::addResource);

        runner.run(runIntegration);

        if (autoRemoveResources) {
            runner.run(doFinally()
                .actions(camel().jbang()
                        .stop()
                        .integration(fileName)));
        }

        resourceFiles.clear();
        propertiesFile = null;
        envVars.clear();
        envVarsFile = null;
        properties.clear();
        propertiesFile = null;
    }

    @Given("^wait for Camel integration ([^\\s]+)(?:.groovy|.xml|.java)$")
    @Given("^Camel integration ([^\\s]+)\\.(?:groovy|xml|java) is running$")
    @Then("^Camel integration ([^\\s]+)\\.(?:groovy|xml|java) should be running$")
    public void verifyIntegration(String fileName) {
        runner.run(camel().jbang()
                .verify()
                .integration(fileName)
                .isRunning());
    }

    @Given("^stop Camel integration ([^\\s]+)(?:.groovy|.xml|.java)$")
    public void stopIntegration(String routeId) {
        runner.run(camel().jbang()
                        .stop()
                        .integration(routeId));
    }

    @Given("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) is stopped")
    @Then("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) should be stopped")
    public void integrationShouldBeStopped(String name) {
        runner.run(camel()
                .jbang()
                .verify()
                .integration(name)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .isStopped());
    }

    @Then("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) should print (.*)$")
    public void integrationShouldPrint(String name, String message) {
        integrationShouldPrintMultiline(name, message);
    }

    @Then("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) should print$")
    public void integrationShouldPrintMultiline(String name, String message) {
        runner.run(camel()
                .jbang()
                .verify()
                .integration(name)
                .printLogs(CamelSettings.isPrintPodLogs())
                .stopOnErrorStatus(stopOnErrorStatus)
                .maxAttempts(maxAttempts)
                .delayBetweenAttempts(delayBetweenAttempts)
                .waitForLogMessage(message));
    }

    @Then("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) should not print (.*)$")
    public void integrationShouldNotPrint(String name, String message) {
        integrationShouldNotPrintMultiline(name, message);
    }

    @Then("^Camel integration ([^\\s]+)(?:.groovy|.xml|.java) should not print$")
    public void integrationShouldNotPrintMultiline(String name, String message) {
        runner.run(assertException()
                .exception(ActionTimeoutException.class)
                .when(camel()
                        .jbang()
                        .verify()
                        .integration(name)
                        .printLogs(CamelSettings.isPrintPodLogs())
                        .stopOnErrorStatus(stopOnErrorStatus)
                        .maxAttempts(maxAttempts)
                        .delayBetweenAttempts(delayBetweenAttempts)
                        .waitForLogMessage(message)));
    }

    @Given("^Camel integration property ([^\\s]+)=\"([^\"]*)\"$")
    @Given("^Camel integration property ([^\\s]+) (?:is|=) \"([^\"]*)\"$")
    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    @Given("^Camel integration properties$")
    public void addProperties(DataTable propertyTable) {
        properties.putAll(propertyTable.asMap(String.class, String.class));
    }

    @Given("^Camel integration property file ([^\\s]+)$")
    public void addPropertyFile(String filePath) {
        propertiesFile = ResourceUtils.resolve(filePath, context);
    }

    @Given("^Camel integration environment variable ([^\\s]+)=\"([^\"]*)\"$")
    @Given("^Camel integration environment variable ([^\\s]+) (?:is|=) \"([^\"]*)\"$")
    public void addEnVar(String name, String value) {
        envVars.put(name, value);
    }

    @Given("^Camel integration environment variables$")
    public void addEnvVars(DataTable propertyTable) {
        envVars.putAll(propertyTable.asMap(String.class, String.class));
    }

    @Given("^Camel integration environment variable file ([^\\s]+)$")
    public void addEnvVarsFile(String filePath) {
        envVarsFile = ResourceUtils.resolve(filePath, context);
    }
}
