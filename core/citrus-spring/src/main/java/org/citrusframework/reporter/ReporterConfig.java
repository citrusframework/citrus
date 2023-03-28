package org.citrusframework.reporter;

import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class ReporterConfig {

    @Bean(name = "citrusLoggingReporter")
    public LoggingReporter loggingReporter() {
        return new LoggingReporter();
    }

    @Bean(name = "citrusHtmlReporter")
    public HtmlReporter htmlReporter() {
        return new HtmlReporter();
    }

    @Bean(name = "citrusJunitReporter")
    public JUnitReporter junitReporter() {
        return new JUnitReporter();
    }

    @Bean(name = "citrusTestReporters")
    public TestReportersFactory testReporters() {
        return new TestReportersFactory();
    }
}
