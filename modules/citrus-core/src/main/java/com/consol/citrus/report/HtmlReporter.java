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

package com.consol.citrus.report;

import java.io.*;
import java.text.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestResult.RESULT;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;

/**
 * Basic logging reporter generating a HTML report with detailed test results.
 * 
 * @author Philipp Komninos, Christoph Deppisch
 */
public class HtmlReporter extends AbstractTestListener implements TestReporter {
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(HtmlReporter.class);
    
    /** Collect test results for test report */
    private TestResults testResults = new TestResults();
    
    /** Map holding additional information of test cases */
    private Map<String, ResultDetail> details = new HashMap<String, ResultDetail>();
    
    /** Static resource for the HTML test report template */
    private static final Resource REPORT_TEMPLATE = new ClassPathResource("test-report.html", HtmlReporter.class);
    
    /** Test detail template */
    private static final Resource TEST_DETAIL_TEMPLATE = new ClassPathResource("test-detail.html", HtmlReporter.class);
    
    /** Output directory */
    private static final String OUTPUT_DIRECTORY = "test-output/citrus-reports";
    
    /** Resource files directory */
    private static final String RESOURCE_DIRECTORY = OUTPUT_DIRECTORY + "/resources";
    
    /** Resulting HTML test report file name */    
    private static final String REPORT_FILE_NAME = "citrus-test-results.html";
    
    /** Format for creation and update date of TestCases */
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    /** Common decimal format for percentage calculation in report */
    private DecimalFormat decFormat = new DecimalFormat("0.0");
    
    /**
     * Default constructor.
     */
    public HtmlReporter() {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults()
     */
    public void generateTestResults() {
        String report = "";
        StringBuilder reportDetails = new StringBuilder();
        
        log.info("Generating HTML test report ...");
        
        try {
            String testDetails = FileUtils.readToString(TEST_DETAIL_TEMPLATE);
            String unknown = "N/A";
            
            for (TestResult result : testResults) {
                ResultDetail detail = details.get(result.getTestName());
                
                Properties detailProps = new Properties();
                detailProps.put("test.style.class", result.getResult().toString().toLowerCase());
                detailProps.put("test.case.name", result.getTestName());
                detailProps.put("test.author", !StringUtils.hasText(detail.getMetaInfo().getAuthor()) ? unknown : detail.getMetaInfo().getAuthor());
                detailProps.put("test.status", detail.getMetaInfo().getStatus().toString());
                detailProps.put("test.creation.date", detail.getMetaInfo().getCreationDate() == null ? unknown : dateFormat.format(detail.getMetaInfo().getCreationDate()));
                detailProps.put("test.updater", !StringUtils.hasText(detail.getMetaInfo().getLastUpdatedBy()) ? unknown : detail.getMetaInfo().getLastUpdatedBy());
                detailProps.put("test.update.date", detail.getMetaInfo().getLastUpdatedOn() == null ? unknown : dateFormat.format(detail.getMetaInfo().getLastUpdatedOn()));
                detailProps.put("test.description", !StringUtils.hasText(detail.getDescription()) ? unknown : detail.getDescription());
                detailProps.put("test.result", result.getResult().toString().toUpperCase());
                
                reportDetails.append(PropertyUtils.replacePropertiesInString(testDetails, detailProps));
                
                if (result.getResult().equals(RESULT.FAILURE) && result.getCause() != null) {
                    reportDetails.append(getStackTraceHtml(result.getCause()));
                }
            }

            Properties reportProps = new Properties();
            reportProps.put("test.cnt", Integer.toString(testResults.size()));
            reportProps.put("skipped.test.cnt", Integer.toString(testResults.getSkipped()));
            reportProps.put("skipped.test.pct", decFormat.format((double)testResults.getSkipped() / testResults.size()*100));
            reportProps.put("failed.test.cnt", Integer.toString(testResults.getFailed()));
            reportProps.put("failed.test.pct", decFormat.format((double)testResults.getFailed() / testResults.size()*100));
            reportProps.put("success.test.cnt", Integer.toString(testResults.getSuccess()));
            reportProps.put("success.test.pct", decFormat.format((double)testResults.getSuccess() / testResults.size()*100));
            reportProps.put("test.details", reportDetails.toString());
            report = PropertyUtils.replacePropertiesInString(FileUtils.readToString(REPORT_TEMPLATE), reportProps);
            
            copyResources();
            createReportFile(report);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to generate HTML test report", e);
        }
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
                        reader = new BufferedReader(new FileReader(
                                new ClassPathResource(stackElement.getTestFilePath() + ".xml").getFile()));
                        
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
            log.error("Failed to construct HTML code snippet", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Failed to close test file", e);
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
        stackTraceBuilder.append(cause.getClass().getName() + ": " + cause.getMessage() + "\n ");
        for (int i = 0; i < cause.getStackTrace().length; i++) {
            stackTraceBuilder.append("\n\t at " + cause.getStackTrace()[i]);
        }
        
        return "<tr><td colspan=\"2\">" +
        		"<div class=\"error-detail\"><pre>" + stackTraceBuilder.toString() + 
        		"</pre>" + getCodeSnippetHtml(cause) + "</div></td></tr>";
    }


    /**
     * Creates resources(images and CSS file) in target directory
     * @param resources A String array of classpath resources to be copied
     * @param targetPath The target directory where the files should be written to
     */
    private void copyResources() {
        Resource resource = new ClassPathResource("citrus_logo.png", HtmlReporter.class);
        
        InputStream  in = null;
        OutputStream out = null;
        File targetDirectory = new File(RESOURCE_DIRECTORY);
        if (!targetDirectory.exists()) {
            boolean success = targetDirectory.mkdirs();
            
            if (!success) {
                throw new CitrusRuntimeException("Unable to create folder structure for HTML report");
            }
        }
        
        try {
            in = resource.getInputStream();
            out = new FileOutputStream(RESOURCE_DIRECTORY + "/" + resource.getFilename());
            byte[] buffer = new byte[ 0xFFFF ];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } catch (IOException e) {
            log.error("Failed to copy the HTML test report resource files", e);
        } finally {
            if (in != null) {
                try { 
                    in.close(); 
                } catch (IOException e) { 
                    log.error("Failed to close input stream", e); 
                }
            }
            
            if (out != null) {
                try { 
                    out.close(); 
                } catch (IOException e) { 
                    log.error("Failed to close output stream", e); 
                }
            }
        }
    }
    
    
    /**
     * Creates the HTML report file
     * @param content The String content of the report file
     * @param targetPath The directory where the report file is created
     * @param reportFileName The name of the report file
     */
    private void createReportFile(String content) {
        Writer fileWriter = null;
        try {
            fileWriter = new FileWriter(OUTPUT_DIRECTORY + "/" + REPORT_FILE_NAME);
            fileWriter.append(content);
            fileWriter.flush();
        } catch (IOException e) {
            log.error("Failed to save HTML test report", e);
        } finally {
            if (fileWriter != null) {
                try { 
                    fileWriter.close(); 
                } catch (IOException e) { 
                    log.error("Error closing HTML report file", e); 
                } 
            }
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));
        
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS, test.getParameters()));        
    }
    
    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        details.put(test.getName(), ResultDetail.build(test));
        
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause, test.getParameters()));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, null, test.getParameters()));
        }
    }
    
    @Override
    public void onTestSkipped(TestCase test) {
        details.put(test.getName(), ResultDetail.build(test));
        
        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP, test.getParameters()));
    }
    
    /**
     * Value object holding test specific data for HTML report generation. 
     */
    private static class ResultDetail {
        /** The meta info of the underlying test */
        private TestCaseMetaInfo metaInfo;
        
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
