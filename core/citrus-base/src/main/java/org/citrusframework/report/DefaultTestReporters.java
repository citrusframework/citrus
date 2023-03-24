package org.citrusframework.report;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christoph Deppisch
 */
public class DefaultTestReporters extends TestReporters {

    public static final List<TestReporter> DEFAULT_REPORTERS = Arrays.asList(
            new LoggingReporter(),
            new HtmlReporter(),
            new JUnitReporter()
    );

    public DefaultTestReporters() {
        DEFAULT_REPORTERS.forEach(this::addTestReporter);
    }
}
