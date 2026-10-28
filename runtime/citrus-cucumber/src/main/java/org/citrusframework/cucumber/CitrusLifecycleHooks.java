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
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceProcessor;
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

    /**
     * Order of the Citrus suite hooks, one below the Cucumber default order 10000. Cucumber runs before-all hooks in
     * ascending and after-all hooks in descending order, so user hooks with the default order run after the Citrus suite
     * has started and before it finishes.
     */
    public static final int SUITE_HOOK_ORDER = 9999;

    /** Instance processor starting the suite on the next Citrus instance created */
    private static final AtomicReference<CitrusInstanceProcessor> pendingSuiteStart = new AtomicReference<>();

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
        cancelPendingSuiteStart();

        Optional<Citrus> instance = CitrusInstanceManager.get();
        if (instance.isPresent()) {
            instance.get().beforeSuite(CitrusReporter.SUITE_NAME);
            return;
        }

        CitrusInstanceProcessor suiteStart = new CitrusInstanceProcessor() {
            @Override
            public void process(Citrus created) {
                if (pendingSuiteStart.compareAndSet(this, null)) {
                    CitrusInstanceManager.removeInstanceProcessor(this);
                    created.beforeSuite(CitrusReporter.SUITE_NAME);
                }
            }
        };

        pendingSuiteStart.set(suiteStart);
        CitrusInstanceManager.addInstanceProcessor(suiteStart);
    }

    /**
     * Finishes the Citrus suite. Runs as Cucumber after-all hook once per Cucumber run.
     * Does nothing when no Citrus instance has been created during the run.
     */
    public static void afterSuite() {
        cancelPendingSuiteStart();
        CitrusInstanceManager.get().ifPresent(instance -> instance.afterSuite(CitrusReporter.SUITE_NAME));
    }

    private static void cancelPendingSuiteStart() {
        CitrusInstanceProcessor pending = pendingSuiteStart.getAndSet(null);
        if (pending != null) {
            CitrusInstanceManager.removeInstanceProcessor(pending);
        }
    }
}
