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

package org.citrusframework.cucumber.report;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.citrusframework.report.AbstractTestSuiteListener;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reporter activates when logging over logging framework is disabled in order to print
 * a minimal test report using System output stream.
 */
public class MinimalTestReporter extends AbstractTestSuiteListener implements TestReporter {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(MinimalTestReporter.class);

    private final OutputStreamReporter delegate;

    public MinimalTestReporter() {
        this.delegate = new OutputStreamReporter(new OutputStreamWriter(System.out));
        this.delegate.setFormat("%s | %s%n");
    }

    @Override
    public void onStart() {
        if (!LoggerFactory.getLogger(LoggingReporter.class).isInfoEnabled()) {
            try {
                delegate.onStart();
                delegate.getLogWriter().flush();
            } catch (IOException e) {
                LOG.warn("Failed to initialize test report", e);
            }
        }
    }

    @Override
    public void generateReport(TestResults testResults) {
        if (!LoggerFactory.getLogger(LoggingReporter.class).isInfoEnabled()) {
            delegate.generateReport(testResults);
        }
    }

    public void destroy() throws Exception {
        try {
            delegate.getLogWriter().flush();
        } catch (IOException e) {
            // do nothing
        }

        delegate.getLogWriter().close();
    }
}
