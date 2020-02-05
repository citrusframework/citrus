package com.consol.citrus.reporter;

import com.consol.citrus.report.HtmlReporter;
import com.consol.citrus.report.JUnitReporter;
import com.consol.citrus.report.LoggingReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class ReporterConfig {

    @Bean
    public LoggingReporter loggingReporter() {
        return new LoggingReporter();
    }

    @Bean
    public HtmlReporter htmlReporter() {
        return new HtmlReporter();
    }

    @Bean
    public JUnitReporter junitReporter() {
        return new JUnitReporter();
    }
}
