/*
 * Copyright 2006-2011 the original author or authors.
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.PropertyUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic logging reporter generating a HTML report with detailed test results.
 *
 * @author Philipp Komninos, Christoph Deppisch
 */
public class HtmlReporter extends AbstractOutputFileReporter implements TestListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(HtmlReporter.class);

    /** Map holding additional information of test cases */
    private Map<String, ResultDetail> details = new HashMap<>();

    /** Static resource for the HTML test report template */
    private String reportTemplate = HtmlReporterSettings.getReportTemplate();

    /** Test detail template */
    private String testDetailTemplate = HtmlReporterSettings.getReportDetailTemplate();

    /** Output directory */
    private final String outputDirectory = HtmlReporterSettings.getReportDirectory();

    /** Resulting HTML test report file name */
    private String reportFileName = HtmlReporterSettings.getReportFile();

    /** Format for creation and update date of TestCases */
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    /** Default logo image resource */
    private String logo = HtmlReporterSettings.getReportLogo();

    /** Enables/disables report generation */
    private boolean enabled = HtmlReporterSettings.isReportEnabled();

    @Override
    public String getReportContent(TestResults testResults) {
        final StringBuilder reportDetails = new StringBuilder();

        logger.debug("Generating HTML test report");

        try {
            final String testDetails = FileUtils.readToString(FileUtils.getFileResource(testDetailTemplate));
            final String emptyString = "";

            testResults.doWithResults(result -> {
                ResultDetail detail = Optional.ofNullable(details.get(result.getTestName())).orElseGet(ResultDetail::new);

                Properties detailProps = new Properties();
                detailProps.put("test.style.class", result.getResult().toLowerCase());
                detailProps.put("test.case.name", result.getTestName());
                detailProps.put("test.author", !StringUtils.hasText(detail.getMetaInfo().getAuthor()) ? emptyString : detail.getMetaInfo().getAuthor());
                detailProps.put("test.status", detail.getMetaInfo().getStatus().toString());
                detailProps.put("test.creation.date", detail.getMetaInfo().getCreationDate() == null ? emptyString : dateFormat.format(detail.getMetaInfo().getCreationDate()));
                detailProps.put("test.updater", !StringUtils.hasText(detail.getMetaInfo().getLastUpdatedBy()) ? emptyString : detail.getMetaInfo().getLastUpdatedBy());
                detailProps.put("test.update.date", detail.getMetaInfo().getLastUpdatedOn() == null ? emptyString : dateFormat.format(detail.getMetaInfo().getLastUpdatedOn()));
                detailProps.put("test.description", !StringUtils.hasText(detail.getDescription()) ? emptyString : detail.getDescription());
                detailProps.put("test.result", result.getResult());

                reportDetails.append(PropertyUtils.replacePropertiesInString(testDetails, detailProps));

                if (result.isFailed() && result.getCause() != null) {
                    reportDetails.append(getStackTraceHtml(result.getCause()));
                }
            });

            Properties reportProps = new Properties();
            reportProps.put("test.cnt", Integer.toString(testResults.getSize()));
            reportProps.put("skipped.test.cnt", Integer.toString(testResults.getSkipped()));
            reportProps.put("skipped.test.pct", testResults.getSkippedPercentage());
            reportProps.put("failed.test.cnt", Integer.toString(testResults.getFailed()));
            reportProps.put("failed.test.pct", testResults.getFailedPercentage());
            reportProps.put("success.test.cnt", Integer.toString(testResults.getSuccess()));
            reportProps.put("success.test.pct", testResults.getSuccessPercentage());
            reportProps.put("test.results", reportDetails.toString());
            reportProps.put("logo.data", getLogoImageData());
            return PropertyUtils.replacePropertiesInString(FileUtils.readToString(FileUtils.getFileResource(reportTemplate)), reportProps);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to generate HTML test report", e);
        }
    }

    /**
     * Reads citrus logo png image and converts to base64 encoded string for inline HTML image display.
     * @return
     * @throws IOException
     */
    private String getLogoImageData() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedInputStream reader = null;

        try {
            reader = new BufferedInputStream(FileUtils.getFileResource(logo).getInputStream());

            byte[] contents = new byte[1024];
            while( reader.read(contents) != -1) {
                os.write(contents);
            }
        } catch(IOException e) {
            logger.warn("Failed to add logo image data to HTML report", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException ex) {
                    logger.warn("Failed to close logo image resource for HTML report", ex);
                }
            }

            try {
                os.flush();
            } catch(IOException ex) {
                logger.warn("Failed to flush logo image stream for HTML report", ex);
            }
        }

        return Base64.encodeBase64String(os.toByteArray());
    }

    /**
     * Gets the code section from test case XML which is responsible for the
     * error.
     * @param cause the error cause.
     * @return
     */
    private String getCodeSnippetHtml(Throwable cause) {
        StringBuilder codeSnippet = new StringBuilder();
        BufferedReader reader = null;

        try {
            if (cause instanceof CitrusRuntimeException) {
                CitrusRuntimeException ex = (CitrusRuntimeException) cause;
                if (!ex.getFailureStack().isEmpty()) {
                    FailureStackElement stackElement = ex.getFailureStack().pop();
                    if (stackElement.getLineNumberStart() > 0) {
                        reader = new BufferedReader(Resources.fromClasspath(stackElement.getTestFilePath() + FileUtils.FILE_EXTENSION_XML).getReader());

                        codeSnippet.append("<div class=\"code-snippet\">");
                        codeSnippet.append("<h2 class=\"code-title\">" + stackElement.getTestFilePath() + ".xml</h2>");

                        String line;
                        String codeStyle;
                        int lineIndex = 1;
                        int snippetOffset = 5;
                        while ((line = reader.readLine()) != null) {
                            if (lineIndex >= stackElement.getLineNumberStart() - snippetOffset &&
                                    lineIndex < stackElement.getLineNumberStart() ||
                                    lineIndex > stackElement.getLineNumberEnd() &&
                                    lineIndex <= stackElement.getLineNumberEnd() + snippetOffset) {
                                codeStyle = "code";
                            } else if (lineIndex >= stackElement.getLineNumberStart() &&
                                    lineIndex <= stackElement.getLineNumberEnd()) {
                                codeStyle = "code-failed";
                            } else {
                                codeStyle = "";
                            }

                            if (StringUtils.hasText(codeStyle)) {
                                codeSnippet.append("<pre class=\"" + codeStyle +"\"><span class=\"line-number\">" + lineIndex + ":</span>" +
                                        line.replaceAll(">", "&gt;").replaceAll("<", "&lt;") + "</pre>");
                            }

                            lineIndex++;

                        }

                        codeSnippet.append("</div>");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to construct HTML code snippet", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Failed to close test file", e);
                }
            }
        }

        return codeSnippet.toString();
    }

    /**
     * Construct HTML code snippet for stack trace information.
     * @param cause the causing error.
     * @return
     */
    private String getStackTraceHtml(Throwable cause) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append(cause.getClass().getName())
                        .append(": ")
                        .append(cause.getMessage())
                        .append("\n ");
        for (int i = 0; i < cause.getStackTrace().length; i++) {
            stackTraceBuilder.append("\n\t at ");
            stackTraceBuilder.append(cause.getStackTrace()[i]);
        }

        return "<tr><td colspan=\"2\">" +
        		"<div class=\"error-detail\"><pre>" + stackTraceBuilder.toString() +
        		"</pre>" + getCodeSnippetHtml(cause) + "</div></td></tr>";
    }

    @Override
    public void onTestStart(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestFinish(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestSuccess(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        details.put(test.getName(), ResultDetail.build(test));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));
    }

    /**
     * Sets the logo.
     * @param logo the logo to set
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String getReportDirectory() {
        if (StringUtils.hasText(outputDirectory)) {
            return outputDirectory;
        }

        return super.getReportDirectory();
    }

    /**
     * Sets the reportFileName property.
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

    /**
     * Sets the dateFormat property.
     *
     * @param dateFormat
     */
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Sets the reportTemplate property.
     *
     * @param reportTemplate
     */
    public void setReportTemplate(String reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    /**
     * Sets the testDetailTemplate property.
     *
     * @param testDetailTemplate
     */
    public void setTestDetailTemplate(String testDetailTemplate) {
        this.testDetailTemplate = testDetailTemplate;
    }

    /**
     * Sets the enabled property.
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
     * Value object holding test specific data for HTML report generation.
     */
    private static class ResultDetail {
        /** The meta info of the underlying test */
        private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();

        /** Description of the test */
        private String description;

        /**
         * Builds a new result detail from test case.
         * @param test the test case.
         * @return the result detail.
         */
        public static ResultDetail build(TestCase test) {
            ResultDetail detail = new ResultDetail();
            detail.setDescription(test.getDescription());
            detail.setMetaInfo(test.getMetaInfo());

            return detail;
        }

        /**
         * Gets the test meta information.
         * @return the metaInfo
         */
        public TestCaseMetaInfo getMetaInfo() {
            return metaInfo;
        }

        /**
         * Sets the test meta information.
         * @param metaInfo the metaInfo to set
         */
        public void setMetaInfo(TestCaseMetaInfo metaInfo) {
            this.metaInfo = metaInfo;
        }

        /**
         * Gets the test description.
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the test description.
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }

}
