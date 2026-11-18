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

package org.citrusframework.junit.jupiter.regression;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Regression test for a failing invocation of a parameterized Citrus test stopping the following invocations
 * (JUnit reports "Chain of InvocationInterceptors never called invocation" for them).
 * Each sample has three invocations where only the first one fails on purpose, so the expected outcome is
 * one failure and two successes.
 */
class ParameterizedInvocationFailureTest {

    @Test
    void shouldRunInvocationsAfterFailedValueSourceInvocation() {
        assertOnlyFirstRowFails(ValueSourceRowsSample.class);
    }

    @Test
    void shouldRunInvocationsAfterFailedCsvSourceInvocation() {
        assertOnlyFirstRowFails(CsvSourceRowsSample.class);
    }

    @Test
    void shouldRunInvocationsAfterFailedTestSourceInvocation() {
        assertOnlyFirstRowFails(JavaSourceRowsSample.class);
    }

    private static void assertOnlyFirstRowFails(Class<?> sampleClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(sampleClass))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.execute(request, listener);

        TestExecutionSummary summary = listener.getSummary();
        Assertions.assertEquals(3, summary.getTestsFoundCount(), sampleClass.getSimpleName() + " invocations found");
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), sampleClass.getSimpleName() + " succeeded: " + summary.getFailures());
        Assertions.assertEquals(1, summary.getTestsFailedCount(), sampleClass.getSimpleName() + " failed: " + summary.getFailures());
        Assertions.assertTrue(summary.getFailures().get(0).getException().getMessage().contains("deliberate failure in the first row"),
                () -> "unexpected failure: " + summary.getFailures().get(0).getException());
    }
}
