package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public class SummaryReporterSettings {

    private SummaryReporterSettings() {
        // prevent instantiation
    }

    /** Reporter related settings */
    private static final String REPORT_TEMPLATE_PROPERTY = "citrus.summary.report.template";
    private static final String REPORT_TEMPLATE_ENV = "CITRUS_SUMMARY_REPORT_TEMPLATE";
    private static final String REPORT_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/summary-report.xml";

    private static final String REPORT_FILE_PROPERTY = "citrus.summary.report.file";
    private static final String REPORT_FILE_ENV = "CITRUS_SUMMARY_REPORT_FILE";
    private static final String REPORT_FILE_DEFAULT = "citrus-summary.xml";

    private static final String REPORT_ENABLED_PROPERTY = "citrus.summary.report.enabled";
    private static final String REPORT_ENABLED_ENV = "CITRUS_SUMMARY_REPORT_ENABLED";

    /**
     * Get default report template summary file.
     * @return the path to the template file.
     */
    public static String getReportTemplate() {
        return System.getProperty(REPORT_TEMPLATE_PROPERTY,  System.getenv(REPORT_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_TEMPLATE_ENV) : REPORT_TEMPLATE_DEFAULT);
    }

    /**
     * Get the target output file name of the report.
     * @return
     */
    public static String getReportFile() {
        return System.getProperty(REPORT_FILE_PROPERTY,  System.getenv(REPORT_FILE_ENV) != null ?
                System.getenv(REPORT_FILE_ENV) : REPORT_FILE_DEFAULT);
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
