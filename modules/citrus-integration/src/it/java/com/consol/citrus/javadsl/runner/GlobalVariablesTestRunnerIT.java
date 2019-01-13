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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests the loading and usage of global variables in citrus
 */
@Test
public class GlobalVariablesTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void globalProperties() {
        echo("Project name is: ${project.name}");

        echo("Testing global variables from properties: ${globalWelcomingText}");
    }

    @CitrusTest
    @Parameters({"runner", "context"})
    public void randomLocalVariable_readMultipleTimes_valueShouldRemainConstantOnEachRead(
            @Optional @CitrusResource TestRunner runner, @Optional @CitrusResource TestContext context) {

        runner.variable("localRandomNumber1", "citrus:randomNumber(5)");
        runner.echo("localRandomNumber1: ${localRandomNumber1}");
        assertVariableValuesMatchWhenReadMultipleTimes(context, "localRandomNumber1");

        runner.createVariable("localRandomNumber2", "citrus:randomNumber(8)");
        runner.echo("localRandomNumber2: ${localRandomNumber2}");
        assertVariableValuesMatchWhenReadMultipleTimes(context, "localRandomNumber2");
    }

    @CitrusTest
    @Parameters({"runner", "context"})
    public void multipleGlobalVariablesWithCrossReferencesAndFunctions_readMultipleTimes_valueShouldRemainConstantOnEachRead(
            @Optional @CitrusResource TestRunner runner, @Optional @CitrusResource TestContext context) {
        runner.echo("globalSum1: ${globalSum1}");
        assertVariableValuesMatchWhenReadMultipleTimes(context, "globalSum1");

        runner.echo("globalSum2: ${globalSum2}");
        assertVariableValuesMatchWhenReadMultipleTimes(context, "globalSum2");

        assertVariableValuesMatch(context, "globalSum1", "globalSum2");
    }

    private void assertVariableValuesMatchWhenReadMultipleTimes(final TestContext testContext, final String name) {
        String val1 = testContext.resolveDynamicValue(testContext.getVariable(name));
        String val2 = testContext.resolveDynamicValue(testContext.getVariable(name));
        if (val1.equals(val2)) {
            log.debug(String.format("Values match for variable %s. Value: %s", name, val1));
        } else {
            throw new RuntimeException(String.format("Values don't match for variable %s. Value1: %s, Value2: %s", name, val1, val2));
        }
    }

    private void assertVariableValuesMatch(final TestContext testContext, final String name1, final String name2) {
        String val1 = testContext.resolveDynamicValue(testContext.getVariable(name1));
        String val2 = testContext.resolveDynamicValue(testContext.getVariable(name2));
        if (val1.equals(val2)) {
            log.debug(String.format("Values match for variables %s and %s. Value: %s", name1, name2, val1));
        } else {
            throw new RuntimeException(String.format("Values don't match for variables. %s: %s, %s: %s", name1, val1, name2, val2));
        }
    }
}