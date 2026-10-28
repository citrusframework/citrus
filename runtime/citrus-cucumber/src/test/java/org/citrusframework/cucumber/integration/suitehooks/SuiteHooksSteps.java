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
package org.citrusframework.cucumber.integration.suitehooks;

import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.AfterAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.report.TestSuiteListener;

/**
 * Glue class counting Citrus suite finish events without the Citrus reporter.
 */
public class SuiteHooksSteps implements TestActionSupport {

    private static final AtomicInteger suiteFinishes = new AtomicInteger();
    private static boolean listenerRegistered;

    /** The listener stays on the shared Citrus context, so it only counts during this run */
    private static volatile boolean counting;

    @CitrusResource
    private TestCaseRunner runner;

    @Given("^suite hooks scenario runs$")
    public void scenarioRuns() {
        if (!listenerRegistered) {
            listenerRegistered = true;
            CitrusInstanceManager.getOrDefault().getCitrusContext().getTestSuiteListeners()
                    .addTestSuiteListener(new CountingSuiteListener());
        }

        suiteFinishes.set(0);
        counting = true;

        runner.given(echo("Running without the Citrus reporter"));
    }

    @Then("^the Citrus suite has not finished yet$")
    public void suiteNotFinished() {
        if (suiteFinishes.get() != 0) {
            throw new AssertionError("Citrus suite finished before the last scenario");
        }
    }

    /**
     * Runs before the Citrus after-all hook: Cucumber runs after-all hooks in descending order and the default order
     * 10000 is higher than the Citrus suite hook order.
     */
    @AfterAll
    public static void verifySuiteNotFinishedForDefaultOrder() {
        if (suiteFinishes.get() != 0) {
            throw new AssertionError("Citrus suite finished before an after-all hook with the default order");
        }
    }

    /**
     * Runs after the Citrus after-all hook, which has the higher order and therefore runs first.
     */
    @AfterAll(order = 0)
    public static void verifySuiteFinished() {
        try {
            if (suiteFinishes.get() != 1) {
                throw new AssertionError(String.format("Expected the Citrus suite to finish once but was %d", suiteFinishes.get()));
            }
        } finally {
            counting = false;
        }
    }

    private static class CountingSuiteListener implements TestSuiteListener {
        @Override
        public void onStart() {
        }

        @Override
        public void onFinish() {
            if (counting) {
                suiteFinishes.incrementAndGet();
            }
        }

        @Override
        public void onStartFailure(Throwable cause) {
        }

        @Override
        public void onStartSuccess() {
        }

        @Override
        public void onFinishFailure(Throwable cause) {
        }

        @Override
        public void onFinishSuccess() {
        }
    }
}
