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

package com.consol.citrus.remote.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReportConfiguration implements Serializable {

    /**
     * The report directory.
     */
    @Parameter(property = "citrus.report.directory", defaultValue = "citrus-reports", required = true)
    private String directory = "citrus-reports";

    /**
     * The summary file name.
     */
    @Parameter(property = "citrus.report.summary.file", defaultValue = "citrus-summary.xml", required = true)
    private String summaryFile = "citrus-summary.xml";

    /**
     * Enable/disable HTML report generation.
     */
    @Parameter(property = "citrus.report.html", defaultValue = "true")
    private boolean htmlReport = true;

    /**
     * Get reporting files from server and save them in report output directory.
     */
    @Parameter(property = "citrus.report.save.files", defaultValue = "true")
    private boolean saveReportFiles = true;

    /**
     * Gets the summaryFile.
     *
     * @return
     */
    public String getSummaryFile() {
        return summaryFile;
    }

    /**
     * Sets the summaryFile.
     *
     * @param summaryFile
     */
    public void setSummaryFile(String summaryFile) {
        this.summaryFile = summaryFile;
    }

    /**
     * Gets the htmlReport.
     *
     * @return
     */
    public boolean isHtmlReport() {
        return htmlReport;
    }

    /**
     * Sets the htmlReport.
     *
     * @param htmlReport
     */
    public void setHtmlReport(boolean htmlReport) {
        this.htmlReport = htmlReport;
    }

    /**
     * Sets the directory.
     *
     * @param directory
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Gets the directory.
     *
     * @return
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Gets the saveReportFiles.
     *
     * @return
     */
    public boolean isSaveReportFiles() {
        return saveReportFiles;
    }

    /**
     * Sets the saveReportFiles.
     *
     * @param saveReportFiles
     */
    public void setSaveReportFiles(boolean saveReportFiles) {
        this.saveReportFiles = saveReportFiles;
    }
}
