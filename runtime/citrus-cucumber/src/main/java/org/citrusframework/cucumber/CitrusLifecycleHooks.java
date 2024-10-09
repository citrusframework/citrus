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

package org.citrusframework.cucumber;

import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.backend.Scenario;
import org.citrusframework.exceptions.CitrusRuntimeException;

import static java.lang.String.format;

/**
 * @since 2.6
 */
public class CitrusLifecycleHooks {

    @CitrusFramework
    protected Citrus citrus;

    @CitrusResource
    protected TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    public void before(Scenario scenario) {
        if (runner != null) {
            runner.name(scenario.getName());
            runner.description(scenario.getId());
            runner.start();
        }
    }

    public void after(Scenario scenario) {
        if (runner != null) {
            if (context != null && scenario.isFailed()) {
                var testCase = runner.getTestCase();
                var testResult = testCase.getTestResult();
                if (testResult == null || !testResult.isFailed()) {
                    testCase.fail(new CitrusRuntimeException(format("Scenario '%s' (%s) status %s", scenario.getName(), scenario.getId(), scenario.getStatus().name())));
                }
            }

            runner.stop();
        }
    }
}
