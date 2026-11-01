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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractTestReporterTest {

    private final AbstractTestReporter reporter = new AbstractTestReporter() {
        @Override
        protected void generate(TestResults testResults) {
        }
    };

    @Test
    void shouldResolveReportDirectoryFromSettingsWhenNotSetExplicitly() {
        String previous = System.getProperty("citrus.report.directory");

        try {
            System.setProperty("citrus.report.directory", "target/first-reports");
            assertEquals("target/first-reports", reporter.getReportDirectory());

            System.setProperty("citrus.report.directory", "target/second-reports");
            assertEquals("target/second-reports", reporter.getReportDirectory());
        } finally {
            restoreProperty(previous);
        }
    }

    @Test
    void shouldPreferExplicitlySetReportDirectory() {
        String previous = System.getProperty("citrus.report.directory");

        try {
            reporter.setReportDirectory("target/explicit-reports");
            System.setProperty("citrus.report.directory", "target/other-reports");
            assertEquals("target/explicit-reports", reporter.getReportDirectory());
        } finally {
            restoreProperty(previous);
        }
    }

    private static void restoreProperty(String previousValue) {
        if (previousValue == null) {
            System.clearProperty("citrus.report.directory");
        } else {
            System.setProperty("citrus.report.directory", previousValue);
        }
    }
}
