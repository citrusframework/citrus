/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.citrus.cucumber;

import org.citrusframework.citrus.Citrus;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseRunner;
import org.citrusframework.citrus.TestResult;
import org.citrusframework.citrus.annotations.CitrusFramework;
import org.citrusframework.citrus.annotations.CitrusResource;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.cucumber.backend.Scenario;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import io.cucumber.java.After;
import io.cucumber.java.Before;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusLifecycleHooks {

    @CitrusFramework
    protected Citrus citrus;

    @CitrusResource
    protected TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @Before
    public void before(Scenario scenario) {
        if (runner != null) {
            runner.name(scenario.getName());
            runner.description(scenario.getId());
            runner.start();
        }
    }

    @After
    public void after(Scenario scenario) {
        if (runner != null) {
            if (context != null && scenario.isFailed()) {
                TestCase testCase = runner.getTestCase();
                TestResult testResult = testCase.getTestResult();
                if (testResult == null || !testResult.isFailed()) {
                    runner.getTestCase().setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(),
                            new CitrusRuntimeException(String.format("Scenario '%s' (%s) status %s", scenario.getName(), scenario.getId(), scenario.getStatus().name()))));
                }
            }

            runner.stop();
        }
    }
}
