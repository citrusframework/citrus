package org.citrusframework.report;

import java.util.Map;

/**
 *
 * @author Christoph Deppisch
 */
public class DefaultTestReporters extends TestReporters {

    public static final Map<String, TestReporter> DEFAULT_REPORTERS = Map.of("citrusLoggingReporter", new LoggingReporter(),
        "citrusHtmlReporter",new HtmlReporter(),
        "citrusJunitReporter",new JUnitReporter());

    public DefaultTestReporters() {
        DEFAULT_REPORTERS.values().forEach(this::addTestReporter);
    }
}
