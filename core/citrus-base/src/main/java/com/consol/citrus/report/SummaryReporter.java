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

package com.consol.citrus.report;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Reporter creates a summary report as file.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SummaryReporter extends AbstractOutputFileReporter {

    /** Enables/disables report generation */
    @Value("${citrus.summary.report.enabled:true}")
    private String enabled = Boolean.TRUE.toString();

    /** Resulting summary test report file name */
    @Value("${citrus.summary.report.file:citrus-summary.xml}")
    private String reportFileName = "citrus-summary.xml";

    /** Static resource for the summary test report template */
    @Value("${citrus.summary.report.template:classpath:com/consol/citrus/report/summary-report.xml}")
    private String reportTemplate = "classpath:com/consol/citrus/report/summary-report.xml";

    @Override
    protected String getReportContent() {
        try {
            Properties reportProps = new Properties();
            reportProps.put("test.cnt", Integer.toString(getTestResults().getSize()));
            reportProps.put("skipped.test.cnt", Integer.toString(getTestResults().getSkipped()));
            reportProps.put("skipped.test.pct", getTestResults().getSkippedPercentage());
            reportProps.put("failed.test.cnt", Integer.toString(getTestResults().getFailed()));
            reportProps.put("failed.test.pct", getTestResults().getFailedPercentage());
            reportProps.put("success.test.cnt", Integer.toString(getTestResults().getSuccess()));
            reportProps.put("success.test.pct", getTestResults().getSuccessPercentage());
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
        this.enabled = String.valueOf(enabled);
    }

    @Override
    protected boolean isEnabled() {
        return StringUtils.hasText(enabled) && enabled.equalsIgnoreCase(Boolean.TRUE.toString());
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
