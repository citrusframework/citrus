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

import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

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
