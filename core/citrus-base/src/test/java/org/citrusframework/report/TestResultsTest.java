/*
 * Copyright 2006-2013 the original author or authors.
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

import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TestResultsTest {

    @Test
    public void testSuccessResults() throws Exception {
        TestResults results = new TestResults();

        results.addResult(TestResult.success("OkTest", TestResultsTest.class.getName()));
        results.addResult(TestResult.success("OkTest2", TestResultsTest.class.getName()));

        Assert.assertEquals(results.getSuccess(), 2);
        Assert.assertEquals(results.getSuccessPercentage(), "100.0");
        Assert.assertEquals(results.getFailed(), 0);
        Assert.assertEquals(results.getFailedPercentage(), "0.0");
        Assert.assertEquals(results.getSkipped(), 0);
        Assert.assertEquals(results.getSkippedPercentage(), "0.0");
    }

    @Test
    public void testFailedResults() throws Exception {
        TestResults results = new TestResults();

        results.addResult(TestResult.success("OkTest", TestResultsTest.class.getName()));
        results.addResult(TestResult.failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        results.addResult(TestResult.success("OkTest2", TestResultsTest.class.getName()));

        Assert.assertEquals(results.getSuccess(), 2);
        Assert.assertEquals(results.getSuccessPercentage(), "66.7");
        Assert.assertEquals(results.getFailed(), 1);
        Assert.assertEquals(results.getFailedPercentage(), "33.3");
        Assert.assertEquals(results.getSkipped(), 0);
        Assert.assertEquals(results.getSkippedPercentage(), "0.0");
    }

    @Test
    public void testSkippedResults() throws Exception {
        TestResults results = new TestResults();

        results.addResult(TestResult.success("OkTest", TestResultsTest.class.getName()));
        results.addResult(TestResult.failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        results.addResult(TestResult.skipped("SkippedTest", TestResultsTest.class.getName()));

        Assert.assertEquals(results.getSuccess(), 1);
        Assert.assertEquals(results.getSuccessPercentage(), "50.0");
        Assert.assertEquals(results.getFailed(), 1);
        Assert.assertEquals(results.getFailedPercentage(), "50.0");
        Assert.assertEquals(results.getSkipped(), 1);
        Assert.assertEquals(results.getSkippedPercentage(), "33.3");
    }
}
