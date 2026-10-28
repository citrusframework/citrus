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
package org.citrusframework.cucumber.integration.springxml;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.test.context.ContextConfiguration;

/**
 * Spring-managed Java steps used together with XML step templates.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class SpringJavaSteps implements TestActionSupport {

    @CitrusResource
    private TestCaseRunner runner;

    @Given("^Spring XML user is (.*)$")
    public void user(String name) {
        runner.given(createVariable("user", name));
    }

    @Then("^Spring XML greeting is (.*)$")
    public void verifyGreeting(String expected) {
        runner.then(context -> {
            if (!expected.equals(context.getVariable("greeting"))) {
                throw new AssertionError(String.format("Expected greeting '%s' but was '%s'",
                        expected, context.getVariable("greeting")));
            }
        });
    }
}
