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

package org.citrusframework.cucumber.steps.core;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.LoadPropertiesAction.Builder.load;
import static org.citrusframework.actions.SleepAction.Builder.sleep;

public class StandardSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @Given("^Citrus does Cloud-Native BDD testing$")
    public void itDoesBDD() {
        print("Citrus does Cloud-Native BDD testing");
    }

    @Then("^Citrus rocks!$")
    public void citrusRocks() {
        print("Citrus rocks!");
    }

    @Given("^variable ([^\\s]+) (?:is|=) \"([^\"]*)\"$")
    public void variable(String name, String value) {
        runner.variable(name, value);
    }

    @Given("^variable ([^\\s]+)=\"([^\"]*)\"$")
    public void variableDeclaration(String name, String value) {
        runner.variable(name, value);
    }

    @Given("^variable ([^\\s]+) (?:is|=)$")
    public void variableMultiline(String name, String value) {
        runner.variable(name, value);
    }

    @Given("^load variable ([^\\s]+)\\.([a-z0-9-]+)$")
    public void loadVariable(String name, String fileExtension) {
        loadVariableFromFile(name, name + "." + fileExtension);
    }

    @Given("^load variable ([^\\s]+) from ([^\\s]+)$")
    public void loadVariableFromFile(String name, String file) {
        try {
            variable(name, FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Given("^load variables ([^\\s]+)$")
    public void loadVariables(String file) {
        runner.run(load(file));
    }

    @Given("^variables$")
    public void variables(DataTable dataTable) {
        Map<String, String> variables = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            runner.variable(entry.getKey(), entry.getValue());
        }
    }

    @Then("^(?:log|print) '(.+)'$")
    public void print(String message) {
        runner.run(echo(message));
    }

    @Then("^(?:log|print)$")
    public void printMultiline(String message) {
        runner.run(echo(message));
    }

    @Then("^sleep$")
    public void doSleep() {
        runner.then(sleep());
    }

    @Then("^sleep (\\d+) ms$")
    public void doSleep(long milliseconds) {
        runner.then(sleep().milliseconds(milliseconds));
    }

    @Then("^sleep( \\d+h)?( \\d+min)?( \\d+sec)?( \\d+ms)?$")
    public void doSleep(String hours, String min, String sec, String milliseconds) {
        StringBuilder time = new StringBuilder("PT");

        if (hours != null) {
            time.append(String.format("%sH", hours.substring(0, hours.indexOf("h")).trim()));
        }

        if (min != null) {
            time.append(String.format("%sM", min.substring(0, min.indexOf("m")).trim()));
        }

        if (sec != null) {
            time.append(String.format("%sS", sec.substring(0, sec.indexOf("s")).trim()));
        }

        long ms = 0;
        if (milliseconds != null) {
            ms = Long.parseLong(milliseconds.substring(0, milliseconds.indexOf("m")).trim());
        }

        runner.then(sleep().milliseconds(Duration.parse(time.toString()).toMillis() + ms));
    }
}
