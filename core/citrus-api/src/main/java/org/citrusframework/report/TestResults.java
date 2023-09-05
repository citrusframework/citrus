/*
 * Copyright 2006-2010 the original author or authors.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.citrusframework.TestResult;

/**
 * Multiple {@link org.citrusframework.TestResult} instances combined to a {@link TestResults}.
 *
 * @author Christoph Deppisch
 */
public class TestResults {

    private static final long serialVersionUID = 1L;

    /** Common decimal format for percentage calculation in report **/
    private static final DecimalFormat decFormat = new DecimalFormat("0.0");
    private static final String ZERO_PERCENTAGE = "0.0";

    /** Collected test results */
    private final Set<TestResult> results = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Provides access to results as list generated from synchronized result list.
     * @return
     */
    public List<TestResult> asList() {
        List<TestResult> results = new ArrayList<>();
        doWithResults(results::add);
        return results;
    }

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * Adds a test result to the result list.
     * @param result
     * @return
     */
    public boolean addResult(TestResult result) {
        return results.add(result);
    }

    /**
     * Provides synchronized access to all test results in iteration.
     * @param callback
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
     * @return
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
     * @return
     */
    public String getSuccessPercentage() {
        return results.size() > 0 ? decFormat.format((double)getSuccess() / (getFailed() + getSuccess())*100) : ZERO_PERCENTAGE;
    }

    /**
     * Get number of tests failed.
     * @return
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
     * @return
     */
    public String getFailedPercentage() {
        return results.size() > 0 ? decFormat.format((double)getFailed() / (getFailed() + getSuccess())*100) : ZERO_PERCENTAGE;
    }

    /**
     * Get number of skipped tests.
     * @return
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
     * @return
     */
    public String getSkippedPercentage() {
        return results.size() > 0 ? decFormat.format((double)getSkipped() / (results.size())*100) : ZERO_PERCENTAGE;
    }

    /**
     * Callback interface for synchronized access to test results in iteration.
     */
    public interface ResultCallback {
        /**
         * Do something with the result.
         * @param result
         */
        void doWithResult(TestResult result);
    }

    /**
     * Gets the total amount of test results.
     * @return
     */
    public int getSize() {
        return results.size();
    }
}
