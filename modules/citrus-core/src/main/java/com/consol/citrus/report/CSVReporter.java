package com.consol.citrus.report;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestResult;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Basic reporter generating a CSV report with detailed test results.
 * Created by sudeep.r on 21/11/2016.
 */
public class CSVReporter extends AbstractTestListener implements TestReporter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CSVReporter.class);

    /** Collect test results for test report */
    private TestResults testResults = new TestResults();

    /** Map holding additional information of test cases */
    private Map<String, ResultDetail> details = new HashMap<String, ResultDetail>();

    /** Format for creation and update date of TestCases */
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    /** Test data CSV template */
    @Value("${citrus.csv.report.detail.template:classpath:com/consol/citrus/report/test-detail.csv}")
    private Resource testDetailCSVTemplate;

    /** Static resource for the CSV test report template */
    @Value("${citrus.csv.report.template:classpath:com/consol/citrus/report/test-report.csv}")
    private Resource reportCSVTemplate;

    /** Output directory */
    @Value("${citrus.csv.report.directory:}")
    private String outputDirectory;

    /** Resulting CSV test report file name */
    @Value("${citrus.csv.report.file:citrus-test-results.csv}")
    private String reportFileName;

    /** Enables/disables report generation */
    @Value("${citrus.csv.report.enabled:true}")
    private String enabled;

    /**
     * Date for External ID and FileName
     */
    private Date date = new Date();
    private static final SimpleDateFormat SDF_DATA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat SDF_FILE = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * @see com.consol.citrus.report.TestReporter#clearTestResults()
     */
    public void clearTestResults() {
        testResults = new TestResults();
    }

    @Override
    public void generateTestResults() {
        if (StringUtils.hasText(enabled) &&
                !enabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return;
        }

        String report = "";
        final StringBuilder reportDetails = new StringBuilder();

        log.debug("Generating CSV test report");

        try {
            final String testDetails = FileUtils.readToString(testDetailCSVTemplate);
            final String unknown = "";

            testResults.doWithResults(new TestResults.ResultCallback() {
                @Override
                public void doWithResult(TestResult result) {
                    ResultDetail detail = details.get(result.getTestName());

                    Properties detailProps = new Properties();
                    detailProps.put("test.name", result.getTestName());
                    detailProps.put("test.description", !StringUtils.hasText(detail.getDescription()) ? unknown : "\"" + detail.getDescription().replaceAll("\"","'") + "\"");
                    detailProps.put("test.step", unknown);
                    detailProps.put("test.data", unknown);
                    detailProps.put("test.step", unknown);
                    detailProps.put("test.expectedresult", unknown);
                    detailProps.put("test.externalid", "" + UUID.randomUUID());
                    detailProps.put("test.timestamp", SDF_DATA.format(date));
                    detailProps.put("test.component", unknown);
                    detailProps.put("test.priority", unknown);
                    detailProps.put("test.asignee", !StringUtils.hasText(detail.getMetaInfo().getAuthor()) ? unknown : detail.getMetaInfo().getAuthor());
                    detailProps.put("test.label", "");
                    detailProps.put("test.requirementid", !StringUtils.hasText(detail.getRequirementID()) ? unknown : "\"" + detail.getRequirementID().replaceAll("\"","'") + "\"");
                    detailProps.put("test.scenario", !StringUtils.hasText(detail.getScenario()) ? unknown : "\"" + detail.getScenario().replaceAll("\"","'") + "\"");
                    detailProps.put("test.actualresult", result.getResult());

                    String failureReason = unknown;
                    if (result.isFailed() && result.getCause() != null) {
                        failureReason = "\"" + getStackTrace(result.getCause()).replaceAll("\"","'") + "\"";
                    }
                    detailProps.put("test.failurereport", failureReason + System.getProperty("line.separator"));

                    reportDetails.append(PropertyUtils.replacePropertiesInString(testDetails, detailProps));

                }
            });

            Properties reportProps = new Properties();
            reportProps.put("test.csvresult", reportDetails.toString());
            report = PropertyUtils.replacePropertiesInString(FileUtils.readToString(reportCSVTemplate), reportProps);

            createReportFile(report);

            log.info("Generated CSV test report");
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to generate CSV test report", e);
        }
    }


    /**
     * For stack trace information.
     * @param cause the causing error.
     * @return
     */
    private String getStackTrace(Throwable cause) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append(cause.getClass().getName() + ": " + cause.getMessage() + "\n ");
        for (int i = 0; i < cause.getStackTrace().length; i++) {
            stackTraceBuilder.append("\n\t at ");
            stackTraceBuilder.append(cause.getStackTrace()[i]);
        }

        return stackTraceBuilder.toString();
    }

    /**
     * Creates the HTML report file
     * @param content The String content of the report file
     */
    private void createReportFile(String content) {
        Writer fileWriter = null;

        String directory = StringUtils.hasText(outputDirectory) ? outputDirectory : "test-output" + File.separator + "citrus-reports";
        File targetDirectory = new File(directory);
        if (!targetDirectory.exists()) {
            boolean success = targetDirectory.mkdirs();

            if (!success) {
                throw new CitrusRuntimeException("Unable to create folder structure for CSV report");
            }
        }

        try {
            fileWriter = new FileWriter(directory + File.separator + reportFileName.replaceAll(".[^.]*$", "-") + SDF_FILE.format(date) + ".csv");
            fileWriter.append(content);
            fileWriter.flush();
        } catch (IOException e) {
            log.error("Failed to save CSV test report", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    log.error("Error closing CSV report file", e);
                }
            }
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));

        testResults.addResult(TestResult.success(test.getName(), test.getParameters()));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        details.put(test.getName(), ResultDetail.build(test));
        testResults.addResult(TestResult.failed(test.getName(), cause, test.getParameters()));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));

        testResults.addResult(TestResult.skipped(test.getName(), test.getParameters()));
    }

    /**
     * Sets the outputDirectory property.
     *
     * @param outputDirectory
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Sets the reportFileName property.
     *
     * @param reportFileName
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName.replaceAll(".[^.]*$", "-") + SDF_FILE.format(date) + ".csv";
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
    public void setReportTemplate(Resource reportTemplate) {
        this.reportCSVTemplate = reportTemplate;
    }

    /**
     * Sets the testDetailTemplate property.
     *
     * @param testDetailTemplate
     */
    public void setTestDetailTemplate(Resource testDetailTemplate) {
        this.testDetailCSVTemplate = testDetailTemplate;
    }

    /**
     * Sets the enabled property.
     *
     * @param enabled
     */
    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    /**
     * Value object holding test specific data for HTML report generation.
     */
    private static class ResultDetail {
        /** The meta info of the underlying test */
        private TestCaseMetaInfo metaInfo;

        /** Description of the test */
        private String description;

        /** requirementID of the test */
        private String requirementID;

        /** scenario of the test */
        private String scenario;

        /**
         * Builds a new result detail from test case.
         * @param test the test case.
         * @return the result detail.
         */
        public static ResultDetail build(TestCase test) {
            ResultDetail detail = new ResultDetail();
            detail.setDescription(test.getDescription());
            detail.setRequirementID(test.getRequirementID());
            detail.setScenario(test.getScenario());
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

        /**
         * Gets the test requirementID.
         * @return the requirementID
         */
        public String getRequirementID() {
            return requirementID;
        }

        /**
         * Sets the test requirementID.
         * @param requirementID the requirementID to set
         */
        public void setRequirementID(String requirementID) {
            this.requirementID = requirementID;
        }

        /**
         * Gets the test scenario.
         * @return the scenario
         */
        public String getScenario() {
            return scenario;
        }

        /**
         * Sets the test scenario.
         * @param scenario the scenario to set
         */
        public void setScenario(String scenario) {
            this.scenario = scenario;
        }
    }

}
