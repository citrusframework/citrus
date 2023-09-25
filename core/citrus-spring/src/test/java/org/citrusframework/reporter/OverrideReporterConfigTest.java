package org.citrusframework.reporter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.reporter.ReporterConfig.CITRUS_HTML_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_JUNIT_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_LOGGING_REPORTER;

@ContextConfiguration
public class OverrideReporterConfigTest extends UnitTestSupport {

    @Test
    public void testOverridesLoggingReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_LOGGING_REPORTER) instanceof OverrideLoggingReporter);
    }

    @Test
    public void testOverridesJunitReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_JUNIT_REPORTER) instanceof OverrideJUnitReporter);
    }

    @Test
    public void testOverridesHtmlReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_HTML_REPORTER) instanceof OverrideHtmlReporter);
    }

    @Configuration
    public static class OverrideReporterConfiguration {

        @Bean
        public LoggingReporter citrusLoggingReporter() {
            return new OverrideLoggingReporter();
        }

        @Bean
        public JUnitReporter citrusJunitReporter() {
            return new OverrideJUnitReporter();
        }

        @Bean
        public HtmlReporter citrusHtmlReporter() {
            return new OverrideHtmlReporter();
        }

    }

    private static class OverrideLoggingReporter extends LoggingReporter {
    }

    private static class OverrideJUnitReporter extends JUnitReporter {
    }

    private static class OverrideHtmlReporter extends HtmlReporter {
    }
}
