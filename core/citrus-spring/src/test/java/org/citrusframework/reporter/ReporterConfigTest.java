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

import static org.citrusframework.reporter.ReporterConfig.CITRUS_HTML_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_JUNIT_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_LOGGING_REPORTER;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReporterConfigTest extends UnitTestSupport {

    @Test
    public void testDefaultLoggingReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_LOGGING_REPORTER) instanceof  LoggingReporter);
    }

    @Test
    public void testDefaultJunitReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_JUNIT_REPORTER) instanceof  JUnitReporter);
    }

    @Test
    public void testDefaultHtmlReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_HTML_REPORTER) instanceof  HtmlReporter);
    }
}
