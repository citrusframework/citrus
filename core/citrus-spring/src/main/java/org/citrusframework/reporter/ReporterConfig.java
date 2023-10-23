package org.citrusframework.reporter;

import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class ReporterConfig {

    public static final String CITRUS_LOGGING_REPORTER = "citrusLoggingReporter";
    public static final String CITRUS_JUNIT_REPORTER = "citrusJunitReporter";
    public static final String CITRUS_HTML_REPORTER = "citrusHtmlReporter";

    public static final String DEFAULT_LOGGING_REPORTER_ENABLED_PROPERTY = "citrus.default.logging.reporter.enabled";
    public static final String DEFAULT_JUNIT_REPORTER_ENABLED_PROPERTY = "citrus.default.junit.reporter.enabled";
    public static final String DEFAULT_HTML_REPORTER_ENABLED_PROPERTY = "citrus.default.html.reporter.enabled";

    @Bean(name = CITRUS_LOGGING_REPORTER)
    @Conditional(LoggingReporterEnablementCondition.class)
    public LoggingReporter loggingReporter() {
        return new LoggingReporter();
    }

    @Bean(name = CITRUS_HTML_REPORTER)
    @Conditional(HtmlReporterEnablementCondition.class)
    public HtmlReporter htmlReporter() {
        return new HtmlReporter();
    }

    @Bean(name = CITRUS_JUNIT_REPORTER)
    @Conditional(JunitReporterEnablementCondition.class)
    public JUnitReporter junitReporter() {
        return new JUnitReporter();
    }

    @Bean(name = "citrusTestReporters")
    public TestReportersFactory testReporters() {
        return new TestReportersFactory();
    }

    static class LoggingReporterEnablementCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "true".equals(context.getEnvironment().getProperty(DEFAULT_LOGGING_REPORTER_ENABLED_PROPERTY, "true"));
        }
    }

    static class JunitReporterEnablementCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "true".equals(context.getEnvironment().getProperty(DEFAULT_JUNIT_REPORTER_ENABLED_PROPERTY, "true"));
        }
    }

    static class HtmlReporterEnablementCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "true".equals(context.getEnvironment().getProperty(DEFAULT_HTML_REPORTER_ENABLED_PROPERTY, "true"));
        }
    }

}
