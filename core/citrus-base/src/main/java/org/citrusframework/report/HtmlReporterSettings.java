package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public class HtmlReporterSettings {

    private HtmlReporterSettings() {
        // prevent instantiation
    }

    /** Reporter related settings */
    private static final String REPORT_TEMPLATE_PROPERTY = "citrus.html.report.template";
    private static final String REPORT_TEMPLATE_ENV = "CITRUS_HTML_REPORT_TEMPLATE";
    private static final String REPORT_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/test-report.html";

    private static final String REPORT_DETAIL_TEMPLATE_PROPERTY = "citrus.html.report.detail.template";
    private static final String REPORT_DETAIL_TEMPLATE_ENV = "CITRUS_HTML_REPORT_DETAIL_TEMPLATE";
    private static final String REPORT_DETAIL_TEMPLATE_DEFAULT = "classpath:org/citrusframework/report/test-detail.html";

    private static final String REPORT_DIRECTORY_PROPERTY = "citrus.html.report.directory";
    private static final String REPORT_DIRECTORY_ENV = "CITRUS_HTML_REPORT_DIRECTORY";

    private static final String REPORT_FILE_PROPERTY = "citrus.html.report.file";
    private static final String REPORT_FILE_ENV = "CITRUS_HTML_REPORT_FILE";
    private static final String REPORT_FILE_DEFAULT = "citrus-test-results.html";

    private static final String REPORT_LOGO_PROPERTY = "citrus.html.report.logo";
    private static final String REPORT_LOGO_ENV = "CITRUS_HTML_REPORT_LOGO";
    private static final String REPORT_LOGO_DEFAULT = "classpath:org/citrusframework/report/citrus_logo.png";

    private static final String REPORT_ENABLED_PROPERTY = "citrus.html.report.enabled";
    private static final String REPORT_ENABLED_ENV = "CITRUS_HTML_REPORT_ENABLED";

    /**
     * Get default report template HTML file.
     * @return the path to the template file.
     */
    public static String getReportTemplate() {
        return System.getProperty(REPORT_TEMPLATE_PROPERTY,  System.getenv(REPORT_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_TEMPLATE_ENV) : REPORT_TEMPLATE_DEFAULT);
    }

    /**
     * Get default report detail template HTML file.
     * @return the path to the template file.
     */
    public static String getReportDetailTemplate() {
        return System.getProperty(REPORT_DETAIL_TEMPLATE_PROPERTY,  System.getenv(REPORT_DETAIL_TEMPLATE_ENV) != null ?
                System.getenv(REPORT_DETAIL_TEMPLATE_ENV) : REPORT_DETAIL_TEMPLATE_DEFAULT);
    }

    /**
     * Get target directory where to create report.
     * @return
     */
    public static String getReportDirectory() {
        return System.getProperty(REPORT_DIRECTORY_PROPERTY,  System.getenv(REPORT_DIRECTORY_ENV) != null ?
                System.getenv(REPORT_DIRECTORY_ENV) : "");
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
     * Get the logo source to add to the report.
     * @return
     */
    public static String getReportLogo() {
        return System.getProperty(REPORT_LOGO_PROPERTY,  System.getenv(REPORT_LOGO_ENV) != null ?
                System.getenv(REPORT_LOGO_ENV) : REPORT_LOGO_DEFAULT);
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
