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
    private static final String REPORT_DIRECTORY_DEFAULT = "target/citrus-reports";

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
                System.getenv(REPORT_DIRECTORY_ENV) : REPORT_DIRECTORY_DEFAULT);
    }

    public static void setReportDirectory(String dir) {
        System.setProperty(REPORT_DIRECTORY_PROPERTY, dir);
    }
}
