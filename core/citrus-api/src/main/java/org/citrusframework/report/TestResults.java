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

package org.citrusframework.report;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.citrusframework.TestResult;

import static java.util.Collections.synchronizedSet;

/**
 * Multiple {@link org.citrusframework.TestResult} instances combined to a {@link TestResults}.
 *
 */
public class TestResults {

    /**
     * Common decimal format for percentage calculation in report
     **/
    //private static final DecimalFormat decFormat = ;
    private static final String ZERO_PERCENTAGE = "0.0";

    /**
     * Collected test results
     */
    private final Set<TestResult> results = synchronizedSet(new LinkedHashSet<>());

    /**
     * Provides access to results as list generated from synchronized result list.
     */
    public List<TestResult> asList() {
        List<TestResult> results = new ArrayList<>();
        doWithResults(results::add);
        return results;
    }

    /**
     * Adds a test result to the result list.
     */
    public boolean addResult(TestResult result) {
        return results.add(result);
    }

    /**
     * Provides synchronized access to all test results in iteration.
     */
    public void doWithResults(ResultCallback callback) {
        synchronized (results) {
            for (TestResult result : results) {
                callback.doWithResult(result);
            }
        }
    }

    /**
     * Get number of tests in success.
     */
    public int getSuccess() {
        int count = 0;

        synchronized (results) {
            for (TestResult testResult : results) {
                if (testResult.isSuccess()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Calculates percentage of success tests.
     *
     * @deprecated will return {@code double} value in the future!
     */
    @Deprecated
    public String getSuccessPercentage() {
        return getSuccessPercentageFormatted();
    }

    public String getSuccessPercentageFormatted() {
        return results.isEmpty() || getSuccess() == 0 ? ZERO_PERCENTAGE : getNewDecimalFormat().format((double) getSuccess() / (results.size()) * 100);
    }

    /**
     * Get number of tests failed.
     */
    public int getFailed() {
        int count = 0;

        synchronized (results) {
            for (TestResult testResult : results) {
                if (testResult.isFailed()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Calculates percentage of failed tests.
     *
     * @deprecated will return {@code double} value in the future!
     */
    @Deprecated
    public String getFailedPercentage() {
        return getFailedPercentageFormatted();
    }

    public String getFailedPercentageFormatted() {
        return results.isEmpty() || getFailed() == 0 ? ZERO_PERCENTAGE : getNewDecimalFormat().format((double) getFailed() / (results.size()) * 100);
    }

    /**
     * Get number of skipped tests.
     */
    public int getSkipped() {
        int count = 0;

        synchronized (results) {
            for (TestResult testResult : results) {
                if (testResult.isSkipped()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Calculates percentage of skipped tests.
     *
     * @deprecated will return {@code double} value in the future!
     */
    @Deprecated
    public String getSkippedPercentage() {
        return getSkippedPercentageFormatted();
    }

    public String getSkippedPercentageFormatted() {
        return results.isEmpty() ? ZERO_PERCENTAGE : getNewDecimalFormat().format((double) getSkipped() / (results.size()) * 100);
    }

    /**
     * Callback interface for synchronized access to test results in iteration.
     */
    public interface ResultCallback {

        /**
         * Do something with the result.
         */
        void doWithResult(TestResult result);
    }

    /**
     * Gets the total amount of test results.
     */
    public int getSize() {
        return results.size();
    }

    /**
     * Gets the total duration of all tests.
     */
    public Duration getTotalDuration() {
        return Duration.ofMillis(results.stream()
                .filter(r -> Objects.nonNull(r.getDuration()))
                .mapToLong(r -> r.getDuration().toMillis())
                .sum());
    }

    private DecimalFormat getNewDecimalFormat() {
        var symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        var decimalFormat = new DecimalFormat("0.0");
        decimalFormat.setDecimalFormatSymbols(symbol);
        return decimalFormat;
    }
}
