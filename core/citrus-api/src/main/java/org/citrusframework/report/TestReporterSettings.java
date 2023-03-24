package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public final class TestReporterSettings {

    private TestReporterSettings() {
        // prevent instantiation
    }

    /** Report related settings */
    private static final String REPORT_AUTO_CLEAR_PROPERTY = "citrus.report.auto.clear";
    private static final String REPORT_AUTO_CLEAR_ENV = "CITRUS_REPORT_AUTO_CLEAR";

    private static final String REPORT_IGNORE_ERRORS_PROPERTY = "citrus.report.ignore.errors";
    private static final String REPORT_IGNORE_ERRORS_ENV = "CITRUS_REPORT_IGNORE_ERRORS";

    private static final String REPORT_DIRECTORY_PROPERTY = "citrus.report.directory";
    private static final String REPORT_DIRECTORY_ENV = "CITRUS_REPORT_DIRECTORY";

    /**
     * Get setting if report should automatically clear all test results after finishing the test suite. Default value
     * is true.
     * @return
     */
    public static boolean isAutoClear() {
        return Boolean.parseBoolean(System.getProperty(REPORT_AUTO_CLEAR_PROPERTY,  System.getenv(REPORT_AUTO_CLEAR_ENV) != null ?
                System.getenv(REPORT_AUTO_CLEAR_ENV) : Boolean.TRUE.toString()));
    }

    /**
     * Get setting if report should ignore errors during report generation. Default is true.
     * @return
     */
    public static boolean isIgnoreErrors() {
        return Boolean.parseBoolean(System.getProperty(REPORT_IGNORE_ERRORS_PROPERTY,  System.getenv(REPORT_IGNORE_ERRORS_ENV) != null ?
                System.getenv(REPORT_IGNORE_ERRORS_ENV) : Boolean.TRUE.toString()));
    }

    /**
     * Get target report directory where to create files.
     * @return
     */
    public static String getReportDirectory() {
        return System.getProperty(REPORT_DIRECTORY_PROPERTY,  System.getenv(REPORT_DIRECTORY_ENV) != null ?
                System.getenv(REPORT_DIRECTORY_ENV) : "target/citrus-reports");
    }
}
