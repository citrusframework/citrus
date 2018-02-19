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

package com.consol.citrus.remote.reporter;

import com.consol.citrus.report.AbstractTestReporter;
import com.consol.citrus.report.OutputStreamReporter;

import java.io.StringWriter;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RemoteTestResultReporter extends AbstractTestReporter {

    /** Test report */
    private String testReport;

    @Override
    public void generateTestResults() {
        StringWriter results = new StringWriter();
        OutputStreamReporter reporter = new OutputStreamReporter(results);
        getTestResults().doWithResults(result -> reporter.getTestResults().addResult(result));
        reporter.generateTestResults();
        this.testReport = results.toString();
    }

    /**
     * Gets the latest.
     * @return
     */
    public String getTestReport() {
        return testReport;
    }
}
