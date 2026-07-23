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

package org.citrusframework.report;

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
