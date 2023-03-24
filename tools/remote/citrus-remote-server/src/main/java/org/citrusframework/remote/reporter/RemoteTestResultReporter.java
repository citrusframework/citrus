/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.remote.reporter;

import java.io.StringWriter;

import org.citrusframework.report.AbstractTestReporter;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.TestResults;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RemoteTestResultReporter extends AbstractTestReporter {

    /** Test report */
    private String testReport;

    /** Latest test results */
    private TestResults latestResults = new TestResults();

    @Override
    public void generate(TestResults testResults) {
        this.latestResults = testResults;
        StringWriter results = new StringWriter();
        OutputStreamReporter reporter = new OutputStreamReporter(results);
        reporter.generate(testResults);
        this.testReport = results.toString();
    }

    /**
     * Gets the latest.
     * @return
     */
    public String getTestReport() {
        return testReport;
    }

    /**
     * Obtains the latestResults.
     * @return
     */
    public TestResults getLatestResults() {
        return latestResults;
    }
}
