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

package org.citrusframework.reporter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_HTML_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_JUNIT_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_LOGGING_REPORTER;

public class ReporterConfigIT extends UnitTestSupport {

    @Test
    public void testDefaultLoggingReporter() {
        assertThat(applicationContext.getBean(CITRUS_LOGGING_REPORTER))
                .isInstanceOf(LoggingReporter.class);
    }

    @Test
    public void testDefaultJunitReporter() {
        assertThat(applicationContext.getBean(CITRUS_JUNIT_REPORTER))
                .isInstanceOf(JUnitReporter.class);
    }

    @Test
    public void testDefaultHtmlReporter() {
        assertThat(applicationContext.getBean(CITRUS_HTML_REPORTER))
                .isInstanceOf(HtmlReporter.class);
    }
}
