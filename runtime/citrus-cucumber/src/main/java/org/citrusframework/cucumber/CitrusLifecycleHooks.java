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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
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

    /** Suite start waiting for the next Citrus instance to be created */
    private static final AtomicBoolean suiteStartPending = new AtomicBoolean();

    /** Instance processor starting a pending suite is registered only once */
    private static final AtomicBoolean suiteStartProcessorRegistered = new AtomicBoolean();

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

    /**
     * Starts the Citrus suite. Runs as Cucumber before-all hook once per Cucumber run.
     * Never creates a Citrus instance itself so object factories such as the Spring object factory
     * stay in charge of instance creation. When no instance exists yet the suite starts on the instance created next.
     */
    public static void beforeSuite() {
        Optional<Citrus> instance = CitrusInstanceManager.get();
        if (instance.isPresent()) {
            suiteStartPending.set(false);
            instance.get().beforeSuite(CitrusReporter.SUITE_NAME);
            return;
        }

        suiteStartPending.set(true);
        if (suiteStartProcessorRegistered.compareAndSet(false, true)) {
            CitrusInstanceManager.addInstanceProcessor(created -> {
                if (suiteStartPending.compareAndSet(true, false)) {
                    created.beforeSuite(CitrusReporter.SUITE_NAME);
                }
            });
        }
    }

    /**
     * Finishes the Citrus suite. Runs as Cucumber after-all hook once per Cucumber run.
     * Does nothing when no Citrus instance has been created during the run.
     */
    public static void afterSuite() {
        suiteStartPending.set(false);
        CitrusInstanceManager.get().ifPresent(instance -> instance.afterSuite(CitrusReporter.SUITE_NAME));
    }
}
