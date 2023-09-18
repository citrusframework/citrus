/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.integration.variables;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * Tests the loading and usage of global variables in citrus
 */
@Test
public class GlobalVariablesJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void globalProperties() {
        run(echo("Project name is: ${project.name}"));

        run(echo("Testing global variables from properties: ${globalWelcomingText}"));
    }

    @CitrusTest
    public void shouldRemainConstantOnEachRead(@Optional @CitrusResource TestCaseRunner runner,
            @Optional @CitrusResource TestContext context) {

        runner.variable("localRandomNumber1", "citrus:randomNumber(5)");
        runner.run(echo("localRandomNumber1: ${localRandomNumber1}"));
        assertVariableValuesMatchWhenReadMultipleTimes(context, "localRandomNumber1");

        runner.run(createVariable("localRandomNumber2", "citrus:randomNumber(8)"));
        runner.run(echo("localRandomNumber2: ${localRandomNumber2}"));
        assertVariableValuesMatchWhenReadMultipleTimes(context, "localRandomNumber2");
    }

    @CitrusTest
    public void shouldRemainConstantOnEachReadCrossReference(@Optional @CitrusResource TestCaseRunner runner,
            @Optional @CitrusResource TestContext context) {
        runner.run(echo("globalSum1: ${globalSum1}"));
        assertVariableValuesMatchWhenReadMultipleTimes(context, "globalSum1");

        runner.run(echo("globalSum2: ${globalSum2}"));
        assertVariableValuesMatchWhenReadMultipleTimes(context, "globalSum2");

        assertVariableValuesMatch(context, "globalSum1", "globalSum2");
    }

    private void assertVariableValuesMatchWhenReadMultipleTimes(final TestContext testContext, final String name) {
        String val1 = testContext.resolveDynamicValue(testContext.getVariable(name));
        String val2 = testContext.resolveDynamicValue(testContext.getVariable(name));
        if (val1.equals(val2)) {
            logger.debug(String.format("Values match for variable %s. Value: %s", name, val1));
        } else {
            throw new RuntimeException(String.format("Values don't match for variable %s. Value1: %s, Value2: %s", name, val1, val2));
        }
    }

    private void assertVariableValuesMatch(final TestContext testContext, final String name1, final String name2) {
        String val1 = testContext.resolveDynamicValue(testContext.getVariable(name1));
        String val2 = testContext.resolveDynamicValue(testContext.getVariable(name2));
        if (val1.equals(val2)) {
            logger.debug(String.format("Values match for variables %s and %s. Value: %s", name1, name2, val1));
        } else {
            throw new RuntimeException(String.format("Values don't match for variables. %s: %s, %s: %s", name1, val1, name2, val2));
        }
    }
}
