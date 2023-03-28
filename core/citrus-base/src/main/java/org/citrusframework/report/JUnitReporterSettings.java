package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public class JUnitReporterSettings {

    private JUnitReporterSettings() {
        // prevent instantiation
    }

    /** Reporter related settings */
    private static final String REPORT_TEMPLATE_PROPERTY = "citrus.junit.report.template";
    private static final String REPORT_TEMPLATE_ENV = "CITRUS_JUNIT_REPORT_TEMPLATE";
    private static final String REPORT_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/junit-report.xml";

    private static final String REPORT_SUCCESS_TEMPLATE_PROPERTY = "citrus.junit.report.success.template";
    private static final String REPORT_SUCCESS_TEMPLATE_ENV = "CITRUS_JUNIT_REPORT_SUCCESS_TEMPLATE";
    private static final String REPORT_SUCCESS_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/junit-test.xml";

    private static final String REPORT_FAILED_TEMPLATE_PROPERTY = "citrus.junit.report.failed.template";
    private static final String REPORT_FAILED_TEMPLATE_ENV = "CITRUS_JUNIT_REPORT_FAILED_TEMPLATE";
    private static final String REPORT_FAILED_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/junit-test-failed.xml";

    private static final String REPORT_DIRECTORY_PROPERTY = "citrus.junit.report.directory";
    private static final String REPORT_DIRECTORY_ENV = "CITRUS_JUNIT_REPORT_DIRECTORY";
    private static final String REPORT_DIRECTORY_DEFAULT = "junitreports";

    private static final String REPORT_FILE_PATTERN_PROPERTY = "citrus.junit.report.file.pattern";
    private static final String REPORT_FILE_PATTERN_ENV = "CITRUS_JUNIT_REPORT_FILE_PATTERN";
    private static final String REPORT_FILE_PATTERN_DEFAULT = "TEST-%s.xml";

    private static final String REPORT_SUITE_NAME_PROPERTY = "citrus.junit.report.suite.name";
    private static final String REPORT_SUITE_NAME_ENV = "CITRUS_JUNIT_REPORT_SUITE_NAME";
    private static final String REPORT_SUITE_NAME_DEFAULT = "TestSuite";

    private static final String REPORT_ENABLED_PROPERTY = "citrus.junit.report.enabled";
    private static final String REPORT_ENABLED_ENV = "CITRUS_JUNIT_REPORT_ENABLED";

    /**
     * Get default report template file.
     * @return the path to the template file.
     */
    public static String getReportTemplate() {
        return System.getProperty(REPORT_TEMPLATE_PROPERTY,  System.getenv(REPORT_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_TEMPLATE_ENV) : REPORT_TEMPLATE_DEFAULT);
    }

    /**
     * Get default success template file.
     * @return the path to the template file.
     */
    public static String getSuccessTemplate() {
        return System.getProperty(REPORT_SUCCESS_TEMPLATE_PROPERTY,  System.getenv(REPORT_SUCCESS_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_SUCCESS_TEMPLATE_ENV) : REPORT_SUCCESS_TEMPLATE_DEFAULT);
    }

    /**
     * Get default failed template file.
     * @return the path to the template file.
     */
    public static String getFailedTemplate() {
        return System.getProperty(REPORT_FAILED_TEMPLATE_PROPERTY,  System.getenv(REPORT_FAILED_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_FAILED_TEMPLATE_ENV) : REPORT_FAILED_TEMPLATE_DEFAULT);
    }

    /**
     * Get target directory where to create report.
     * @return
     */
    public static String getReportDirectory() {
        return System.getProperty(REPORT_DIRECTORY_PROPERTY,  System.getenv(REPORT_DIRECTORY_ENV) != null ?
                System.getenv(REPORT_DIRECTORY_ENV) : REPORT_DIRECTORY_DEFAULT);
    }

    /**
     * Get the file pattern to include in this report.
     * @return
     */
    public static String getReportFilePattern() {
        return System.getProperty(REPORT_FILE_PATTERN_PROPERTY,  System.getenv(REPORT_FILE_PATTERN_ENV) != null ?
                System.getenv(REPORT_FILE_PATTERN_ENV) : REPORT_FILE_PATTERN_DEFAULT);
    }

    /**
     * Get the tets suite name.
     * @return
     */
    public static String getSuiteName() {
        return System.getProperty(REPORT_SUITE_NAME_PROPERTY,  System.getenv(REPORT_SUITE_NAME_ENV) != null ?
                System.getenv(REPORT_SUITE_NAME_ENV) : REPORT_SUITE_NAME_DEFAULT);
    }

    /**
     * Get setting to determine if report is enabled.
     * @return
     */
    public static boolean isReportEnabled() {
        return Boolean.parseBoolean(System.getProperty(REPORT_ENABLED_PROPERTY,  System.getenv(REPORT_ENABLED_ENV) != null ?
                System.getenv(REPORT_ENABLED_ENV) : Boolean.TRUE.toString()));
    }
}
