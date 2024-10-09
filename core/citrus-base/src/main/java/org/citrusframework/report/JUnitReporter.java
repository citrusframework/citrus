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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import jakarta.annotation.Nullable;
import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.PropertyUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

/**
 * @since 2.7.5
 */
public class JUnitReporter extends AbstractTestReporter {

    private static final Logger logger = LoggerFactory.getLogger(JUnitReporter.class);

    /** Output directory */
    private String outputDirectory = JUnitReporterSettings.getReportDirectory();

    /** Resulting test report file name */
    private String reportFileNamePattern = JUnitReporterSettings.getReportFilePattern();

    /** Test suite name to use in report */
    private String suiteName = JUnitReporterSettings.getSuiteName();

    /** Static resource for the summary test report template */
    private String reportTemplate = JUnitReporterSettings.getReportTemplate();

    /** Test result template */
    private String successTemplate = JUnitReporterSettings.getSuccessTemplate();

    /** Test result template */
    private String failedTemplate = JUnitReporterSettings.getFailedTemplate();

    /** Enables/disables report generation */
    private boolean enabled = JUnitReporterSettings.isReportEnabled();

    @Override
    public void generate(TestResults testResults) {
        if (isEnabled()) {
            ReportTemplates reportTemplates = new ReportTemplates();

            logger.debug("Generating JUnit test report");

            try {
                List<TestResult> results = testResults.asList();
                createReportFile(format(reportFileNamePattern, suiteName), createReportContent(suiteName, results, reportTemplates), new File(getReportDirectory()));

                Map<String, List<TestResult>> groupedResults = new HashMap<>();
                for(TestResult result : results) {
                    if (!groupedResults.containsKey(result.getClassName())) {
                        groupedResults.put(result.getClassName(), new ArrayList<>());
                    }

                    groupedResults.get(result.getClassName()).add(result);
                }

                File targetDirectory = new File(getReportDirectory() + (StringUtils.hasText(outputDirectory) ? File.separator + outputDirectory : ""));
                for (Map.Entry<String, List<TestResult>> resultEntry : groupedResults.entrySet()) {
                    createReportFile(format(reportFileNamePattern, resultEntry.getKey()), createReportContent(resultEntry.getKey(), resultEntry.getValue(), reportTemplates), targetDirectory);
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to generate JUnit test report", e);
            }
        }
    }

    /**
     * Create report file for test class.
     */
    private String createReportContent(String suiteName, List<TestResult> results, ReportTemplates templates) throws IOException {
        final StringBuilder reportDetails = new StringBuilder();
        Duration suiteDuration = Duration.ofSeconds(0);

        for (TestResult result: results) {
            Properties detailProps = new Properties();
            detailProps.put("test.class", result.getClassName());
            detailProps.put("test.name", escapeXml(result.getTestName()));
            detailProps.put("test.duration", toFormattedTimeString(result.getDuration()));

            if (nonNull(result.getDuration())) {
                suiteDuration = suiteDuration.plus(result.getDuration());
            }

            if (result.isFailed()) {
                detailProps.put("test.error.cause", Optional.ofNullable(result.getCause()).map(Object::getClass).map(Class::getName).orElseGet(() -> Objects.toString(result.getFailureType(), "")));

                if (nonNull(result.getErrorMessage())) {
                    String escapedErrorMessage = escapeXml(result.getErrorMessage());
                    detailProps.put("test.error.msg", escapedErrorMessage);
                }

                detailProps.put("test.error.stackTrace", Optional.ofNullable(result.getCause()).map(cause -> {
                    StringWriter writer = new StringWriter();
                    cause.printStackTrace(new PrintWriter(writer));
                    return writer.toString();
                }).orElseGet(() -> Objects.toString(result.getFailureType(), "")));
                reportDetails.append(System.lineSeparator())
                        .append("    ")
                        .append(PropertyUtils.replacePropertiesInString(templates.getFailedTemplate(), detailProps));
            } else {
                reportDetails.append(System.lineSeparator())
                        .append("    ")
                        .append(PropertyUtils.replacePropertiesInString(templates.getSuccessTemplate(), detailProps));
            }
        }

        Properties reportProps = new Properties();
        reportProps.put("test.suite", suiteName);
        reportProps.put("test.cnt", Integer.toString(results.size()));
        reportProps.put("test.skipped.cnt", Long.toString(results.stream().filter(TestResult::isSkipped).count()));
        reportProps.put("test.failed.cnt", Long.toString(results.stream().filter(TestResult::isFailed).count()));
        reportProps.put("test.success.cnt", Long.toString(results.stream().filter(TestResult::isSuccess).count()));
        reportProps.put("test.error.cnt", "0");
        reportProps.put("test.duration", toFormattedTimeString(suiteDuration));
        reportProps.put("tests", reportDetails.toString());
        return PropertyUtils.replacePropertiesInString(templates.getReportTemplate(), reportProps);
    }

    /**
     * Creates the JUnit report file
     * @param reportFileName The report file to write
     * @param content The String content of the report file
     */
    private void createReportFile(String reportFileName, String content, File targetDirectory) {
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create report output directory: " + getReportDirectory() + (StringUtils.hasText(outputDirectory) ? "/" + outputDirectory : ""));
            }
        }

        try (Writer fileWriter = new FileWriter(new File(targetDirectory, reportFileName))) {
            fileWriter.append(content);
            fileWriter.flush();
        } catch (IOException e) {
            logger.error("Failed to create test report", e);
        }
    }

    private class ReportTemplates {

        private String reportTemplateContent;
        private String successTemplateContent;
        private String failedTemplateContent;

        /**
         * Gets the reportTemplateContent.
         */
        public String getReportTemplate() throws IOException {
            if (reportTemplateContent == null) {
                reportTemplateContent = FileUtils.readToString(FileUtils.getFileResource(reportTemplate)).trim();
            }

            return reportTemplateContent;
        }

        /**
         * Gets the successTemplateContent.
         */
        public String getSuccessTemplate() throws IOException {
            if (successTemplateContent == null) {
                successTemplateContent = FileUtils.readToString(FileUtils.getFileResource(successTemplate)).trim();
            }

            return successTemplateContent;
        }

        /**
         * Gets the failedTemplateContent.
         */
        public String getFailedTemplate() throws IOException {
            if (failedTemplateContent == null) {
                failedTemplateContent = FileUtils.readToString(FileUtils.getFileResource(failedTemplate)).trim();
            }

            return failedTemplateContent;
        }
    }

    private String toFormattedTimeString(@Nullable Duration duration) {
        double seconds = isNull(duration) ? 0.0 : (duration.toNanos() / 1e9); // Convert to seconds with double precision
        return format(Locale.US, "%.3f", seconds); // Format with 3 decimal places
    }

    /**
     * Gets the outputDirectory.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the outputDirectory.
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Gets the reportFileNamePattern.
     */
    public String getReportFileNamePattern() {
        return reportFileNamePattern;
    }

    /**
     * Sets the reportFileNamePattern.
     */
    public void setReportFileNamePattern(String reportFileNamePattern) {
        this.reportFileNamePattern = reportFileNamePattern;
    }

    /**
     * Gets the reportTemplate.
     */
    public String getReportTemplate() {
        return reportTemplate;
    }

    /**
     * Sets the reportTemplate.
     */
    public void setReportTemplate(String reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    /**
     * Gets the suiteName.
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * Sets the suiteName.
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * Gets the successTemplate.
     */
    public String getSuccessTemplate() {
        return successTemplate;
    }

    /**
     * Sets the successTemplate.
     */
    public void setSuccessTemplate(String successTemplate) {
        this.successTemplate = successTemplate;
    }

    /**
     * Gets the failedTemplate.
     */
    public String getFailedTemplate() {
        return failedTemplate;
    }

    /**
     * Sets the failedTemplate.
     */
    public void setFailedTemplate(String failedTemplate) {
        this.failedTemplate = failedTemplate;
    }

    /**
     * Gets the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
