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
package org.citrusframework.cucumber.integration.parallelspring;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.java.AfterAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.spring.CitrusSpringContext;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.test.context.ContextConfiguration;

/**
 * Spring-managed Java steps recording which threads and Citrus instances run the parallel scenarios.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class ParallelSpringSteps implements TestActionSupport {

    private static final Set<String> threads = ConcurrentHashMap.newKeySet();
    private static final Set<Citrus> instances = ConcurrentHashMap.newKeySet();

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @Given("^parallel Spring scenario (\\d+) runs$")
    public void scenarioRuns(String id) {
        threads.add(Thread.currentThread().getName());
        instances.add(citrus);

        runner.given(createVariable("scenario", id));
        runner.when(sleep().milliseconds(200));
    }

    @Then("^parallel Spring scenario variable is (\\d+)$")
    public void verifyScenario(String id) {
        runner.then(context -> {
            if (!id.equals(context.getVariable("scenario"))) {
                throw new AssertionError(String.format("Scenario variable leaked: expected '%s' but was '%s'",
                        id, context.getVariable("scenario")));
            }
        });
    }

    @AfterAll(order = 0)
    public static void verifyParallelRun() {
        if (threads.size() < 2) {
            throw new AssertionError("Expected scenarios to run on several threads but ran on " + threads);
        }

        if (instances.size() != 1) {
            throw new AssertionError("Expected all scenarios to share one Citrus instance but got " + instances.size());
        }

        if (!(instances.iterator().next().getCitrusContext() instanceof CitrusSpringContext)) {
            throw new AssertionError("Expected the Citrus instance to use the Spring application context");
        }
    }
}
