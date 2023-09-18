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

package org.citrusframework.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTestReporter implements TestReporter {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Should ignore errors when creating test report */
    private boolean ignoreErrors = TestReporterSettings.isIgnoreErrors();

    /** Report output directory */
    private String reportDirectory = TestReporterSettings.getReportDirectory();

    @Override
    public final void generateReport(TestResults testResults) {
        try {
            generate(testResults);
        } catch (Exception e) {
            if (ignoreErrors) {
                logger.error("Failed to create test report", e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Subclasses must implement this method and generate the test report for given test results.
     * @param testResults
     */
    protected abstract void generate(TestResults testResults);

    /**
     * Gets the reportDirectory.
     * @return
     */
    public String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * Sets the reportDirectory.
     * @param reportDirectory
     */
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    /**
     * Obtains the ignoreErrors.
     * @return
     */
    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    /**
     * Specifies the ignoreErrors.
     * @param ignoreErrors
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
}
