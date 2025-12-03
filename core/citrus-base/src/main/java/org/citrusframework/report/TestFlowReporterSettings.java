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

import java.util.Arrays;

public class TestFlowReporterSettings {

    private TestFlowReporterSettings() {
        // prevent instantiation
    }

    /** Reporter related settings */
    private static final String REPORT_OUTPUT_PROPERTY = "citrus.test.flow.report.output";
    private static final String REPORT_OUTPUT_ENV = "CITRUS_TEST_FLOW_REPORT_OUTPUT";
    private static final String REPORT_OUTPUT_DEFAULT = "json";

    private static final String REPORT_ENABLED_PROPERTY = "citrus.test.flow.report.enabled";
    private static final String REPORT_ENABLED_ENV = "CITRUS_TEST_FLOW_REPORT_ENABLED";
    private static final String REPORT_ENABLED_DEFAULT = Boolean.TRUE.toString();

    /**
     * Get the report output data format.
     */
    public static String getReportOutput() {
        return System.getProperty(REPORT_OUTPUT_PROPERTY,  System.getenv(REPORT_OUTPUT_ENV) != null ?
                System.getenv(REPORT_OUTPUT_ENV) : REPORT_OUTPUT_DEFAULT);
    }

    /**
     * Is report output data format equal to json.
     */
    public static boolean isJsonReport() {
        return isReportEnabled("json");
    }

    /**
     * Is report output data format equal to yaml.
     */
    public static boolean isYamlReport() {
        return isReportEnabled("yaml");
    }

    /**
     * Get setting to determine if report of given type (json or yaml) is enabled.
     */
    public static boolean isReportEnabled(String type) {
        if (type.equalsIgnoreCase(getReportOutput())) {
            return true;
        }

        if (getReportOutput().contains(",")) {
            return Arrays.stream(getReportOutput().split(",")).anyMatch(s -> s.trim().equals(type));
        }

        return false;
    }

    /**
     * Get setting to determine if report is enabled.
     */
    public static boolean isReportEnabled() {
        return Boolean.parseBoolean(System.getProperty(REPORT_ENABLED_PROPERTY,  System.getenv(REPORT_ENABLED_ENV) != null ?
                System.getenv(REPORT_ENABLED_ENV) : REPORT_ENABLED_DEFAULT));
    }
}
