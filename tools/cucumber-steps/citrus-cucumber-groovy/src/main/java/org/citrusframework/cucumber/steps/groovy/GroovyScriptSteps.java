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

package org.citrusframework.cucumber.steps.groovy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.citrusframework.groovy.dsl.actions.ActionsScript;
import org.citrusframework.groovy.dsl.configuration.ConfigurationScript;
import org.citrusframework.groovy.dsl.configuration.endpoints.EndpointConfigurationScript;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyScriptSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    @CitrusResource
    private TestCaseRunner runner;

    private Map<String, ActionsScript> scripts;

    @Before
    public void before(Scenario scenario) {
        this.scripts = new HashMap<>();
    }

    @Given("^(?:create|new) configuration$")
    public void createConfiguration(String config) {
        new ConfigurationScript(context.replaceDynamicContentInString(config), citrus).execute(context);
    }

    @Given("^load configuration ([^\"\\s]+)\\.groovy$")
    public void loadConfiguration(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        String script = FileUtils.readToString(scriptFile);
        createConfiguration(script);
    }

    @Given("^(?:create|new) endpoint ([^\"\\s]+)\\.groovy$")
    public void createEndpoint(String name, String configurationScript) {
        EndpointConfigurationScript endpointScript = new EndpointConfigurationScript(context.replaceDynamicContentInString(configurationScript), citrus) {
            @Override
            protected void onCreate(Endpoint endpoint) {
                endpoint.setName(name);
            }
        };
        endpointScript.execute(context);

    }

    @Given("^load endpoint ([^\"\\s]+)\\.groovy$")
    public void loadEndpoint(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        String script = FileUtils.readToString(scriptFile);
        final String fileName = FileUtils.getFileName(scriptFile.getLocation());
        final String baseName = Optional.of(fileName)
                .map(f -> f.lastIndexOf("."))
                .filter(index -> index >= 0)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
        createEndpoint(baseName, script);
    }

    @Given("^(?:create|new|bind) component ([^\"\\s]+)\\.groovy$")
    public void createComponent(String name, String configurationScript) {
        Object component = GroovyShellUtils.run(new ImportCustomizer(),
                context.replaceDynamicContentInString(configurationScript), citrus, context);

        if (component instanceof InitializingPhase) {
            ((InitializingPhase) component).initialize();
        }

        citrus.getCitrusContext().bind(name, component);
    }

    @Given("^load component ([^\"\\s]+)\\.groovy$")
    public void loadComponent(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        String script = FileUtils.readToString(scriptFile);
        final String fileName = FileUtils.getFileName(scriptFile.getLocation());
        final String baseName = Optional.ofNullable(fileName)
                .map(f -> f.lastIndexOf("."))
                .filter(index -> index >= 0)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
        createComponent(baseName, script);
    }

    @Given("^(?:create|new) actions ([^\"\\s]+)\\.groovy$")
    public void createActionScript(String scriptName, String code) {
        scripts.put(scriptName, new ActionsScript(code, citrus));
    }

    @Given("^load actions ([^\"\\s]+)\\.groovy$")
    public void loadActionScript(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        String script = FileUtils.readToString(scriptFile);
        final String fileName = FileUtils.getFileName(scriptFile.getLocation());
        final String baseName = Optional.ofNullable(fileName)
                .map(f -> f.lastIndexOf("."))
                .filter(index -> index >= 0)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
        createActionScript(baseName, script);
    }

    @Then("^(?:apply|verify) actions ([^\"\\s]+)\\.groovy$")
    public void runScript(String scriptName) {
        if (!scripts.containsKey(scriptName)) {
            try {
                loadActionScript(scriptName);
            } catch (IOException e) {
                throw new CitrusRuntimeException(String.format("Failed to load/get action script for path/name %s.groovy", scriptName), e);
            }
        }

        Optional.ofNullable(scripts.get(scriptName))
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find action script %s.groovy", scriptName)))
                .execute(runner, context);
    }

    @Then("^\\$\\((.+)\\)$")
    public void runAction(String script) {
        new ActionsScript(script, citrus).execute(runner, context);
    }

    @Then("^(?:apply|run) script: (.+)$")
    public void applyScript(String script) {
        if (ActionsScript.isActionScript(script)) {
            new ActionsScript(script, citrus).execute(runner, context);
        } else {
            GroovyShellUtils.run(new ImportCustomizer(),
                    context.replaceDynamicContentInString(script), citrus, context);
        }
    }

    @Then("^(?:apply|run) script$")
    public void applyScriptMultiline(String script) {
        applyScript(script);
    }

    @Given("^(?:apply|run) script ([^\"\\s]+)\\.groovy$")
    public void applyScriptFile(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        applyScript(context.replaceDynamicContentInString(FileUtils.readToString(scriptFile)));
    }
}
