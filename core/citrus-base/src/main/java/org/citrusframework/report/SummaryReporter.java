/*
 * Copyright 2006-2018 the original author or authors.
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

import java.io.IOException;
import java.util.Properties;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.PropertyUtils;

/**
 * Reporter creates a summary report as file.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SummaryReporter extends AbstractOutputFileReporter {

    /** Enables/disables report generation */
    private boolean enabled = SummaryReporterSettings.isReportEnabled();

    /** Resulting summary test report file name */
    private String reportFileName = SummaryReporterSettings.getReportFile();

    /** Static resource for the summary test report template */
    private String reportTemplate = SummaryReporterSettings.getReportTemplate();

    @Override
    protected String getReportContent(TestResults testResults) {
        try {
            Properties reportProps = new Properties();
            reportProps.put("test.cnt", Integer.toString(testResults.getSize()));
            reportProps.put("skipped.test.cnt", Integer.toString(testResults.getSkipped()));
            reportProps.put("skipped.test.pct", testResults.getSkippedPercentage());
            reportProps.put("failed.test.cnt", Integer.toString(testResults.getFailed()));
            reportProps.put("failed.test.pct", testResults.getFailedPercentage());
            reportProps.put("success.test.cnt", Integer.toString(testResults.getSuccess()));
            reportProps.put("success.test.pct", testResults.getSuccessPercentage());
            return PropertyUtils.replacePropertiesInString(FileUtils.readToString(FileUtils.getFileResource(reportTemplate)), reportProps);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to generate summary test report", e);
        }
    }

    /**
     * Sets the enabled property.
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the reportFileName.
     *
     * @param reportFileName
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    /**
     * Gets the reportFileName.
     *
     * @return
     */
    @Override
    public String getReportFileName() {
        return reportFileName;
    }
}
