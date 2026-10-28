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
package org.citrusframework.cucumber.integration.docstringtable;

import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;

/**
 * Glue class with a step that receives a DocString and a DataTable.
 */
public class DocStringDataTableSteps implements TestActionSupport {

    @CitrusResource
    private TestCaseRunner runner;

    @Given("^message$")
    public void message(String body, DataTable headers) {
        runner.given(createVariable("body", body));
        headers.asMap().forEach((name, value) -> runner.given(createVariable(name, value)));
    }

    @Then("^message body is (.*)$")
    public void verifyBody(String expected) {
        verify(Map.of("body", expected));
    }

    @Then("^message header (\\w+) is (.*)$")
    public void verifyHeader(String name, String expected) {
        verify(Map.of(name, expected));
    }

    private void verify(Map<String, String> expected) {
        runner.then(context -> expected.forEach((name, value) -> {
            if (!value.equals(context.getVariable(name))) {
                throw new AssertionError(String.format("Expected variable '%s' to be '%s' but was '%s'",
                        name, value, context.getVariable(name)));
            }
        }));
    }
}
