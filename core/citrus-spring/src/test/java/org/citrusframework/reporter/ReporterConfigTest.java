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
