/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.TestCase;
import com.consol.citrus.TestResult;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link TestReporter} implementation that generates the famous JUnit XML reports. JUnit can
 * use these XML reports to generate HTML reports.
 *  
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class JUnitReporter implements TestSuiteListener, TestListener, TestReporter {
    
    /** Collect all test results */
    private TestResults testResults = new TestResults();
    
    /** Result XML document */
    private Document doc;

    /** Result element for testsuite */
    private Element testSuiteElement;

    /** Target output file */
    private Resource outputFile = new FileSystemResource("target/test-output/test-results.xml");

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JUnitReporter.class);

    /** Track test execution time */
    private Map<String, Long> testExecutionTime = new HashMap<String, Long>();
    
    /** Track overall execution time */
    private Long overallExecutionTime = 0L;

    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.000");

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }
    
    /**
     * @see com.consol.citrus.report.TestReporter#clearTestResults()
     */
    public void clearTestResults() {
        testResults = new TestResults();
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults()
     */
    public void generateTestResults() {
        try {
            log.info("Generating JUnit results");

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationList domImplList = registry.getDOMImplementationList("LS");

            if (log.isDebugEnabled()) {
                for (int i = 0; i < domImplList.getLength(); i++) {
                    log.debug("Found DOMImplementationLS: " + domImplList.item(i));
                }
            }
            
            DOMImplementationLS domImpl;
            for (int i = 0; i < domImplList.getLength(); i++) {
                try {
                    domImpl = (DOMImplementationLS)domImplList.item(i);

                    if (log.isDebugEnabled()) {
                        log.debug("Using DOMSerializerImpl: " + domImpl.getClass().getName());
                    }

                    LSSerializer serializer = domImpl.createLSSerializer();

                    if (log.isDebugEnabled()) {
                        log.debug("Using LSSerializer: " + serializer.getClass().getName());
                    }

                    if (serializer.getDomConfig().canSetParameter("format-pretty-print", true)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Setting parameter format-pretty-print " + true);
                        }
                        serializer.getDomConfig().setParameter("format-pretty-print", true);
                    }

                    if (!outputFile.exists()) {
                        boolean success = outputFile.getFile().getParentFile().mkdirs();
                        
                        if (!success) {
                            throw new CitrusRuntimeException("Unable to create folder structure for JUnit report");
                        }
                        
                        outputFile.createRelative("");
                    }
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Serializing to file " + outputFile.getFile().toURI().toString());
                    }

                    serializer.writeToURI(doc, outputFile.getFile().toURI().toString());
                } catch(RuntimeException e) {
                    log.error("Error during report generation", e);
                    continue;
                }

                break;
            }

            log.info("JUnit results successfully");
            if (log.isDebugEnabled()) {
                log.debug("OutputFile is: " + outputFile.getFile().getPath());
            }
        } catch (IOException e) {
            log.error("Error during report generation", e);
        } catch (ClassCastException e) {
            log.error("Error during report generation", e);
        } catch (ClassNotFoundException e) {
            log.error("Error during report generation", e);
        } catch (InstantiationException e) {
            log.error("Error during report generation", e);
        } catch (IllegalAccessException e) {
            log.error("Error during report generation", e);
        } finally {
            try {
                outputFile.getInputStream().close();
            } catch (IOException ex) {
                log.error("Error while closing file", ex);
            }
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        Element testCaseElement = doc.createElement("testcase");

        testCaseElement.setAttribute("classname", test.getClass().getName());
        testCaseElement.setAttribute("name", test.getName());
        testCaseElement.setAttribute("time", getTestExecutionTime(test.getName()));

        Element errorElement = doc.createElement("error");
        if (cause != null) {
            errorElement.setAttribute("message", cause.getClass().getName() + " - " + cause.getMessage());
            errorElement.setAttribute("type", cause.getClass().getName());

            StringBuffer buf = new StringBuffer();
            buf.append(cause.getMessage());
            buf.append("\n");
            buf.append(cause.getClass().getName());
            for (int i = 0; i < cause.getStackTrace().length; i++) {
                buf.append("\n at ");
                buf.append(cause.getStackTrace()[i]);
            }
            errorElement.setTextContent(buf.toString());
        } else {
            errorElement.setAttribute("message", "No message available");
            errorElement.setAttribute("type", "no.available");
            errorElement.setTextContent("No exception available");
        }


        testCaseElement.appendChild(errorElement);

        testSuiteElement.appendChild(testCaseElement);
        testResults.addResult(TestResult.failed(test.getName(), cause, test.getParameters()));
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {
        removeTestExecutionTime(test.getName());
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        testResults.addResult(TestResult.skipped(test.getName(), test.getParameters()));
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {
        startTestExecution(test.getName());
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        Element testCaseElement = doc.createElement("testcase");

        testCaseElement.setAttribute("classname", test.getClass().getName());
        testCaseElement.setAttribute("name", test.getName());
        testCaseElement.setAttribute("time", getTestExecutionTime(test.getName()));

        testSuiteElement.appendChild(testCaseElement);
        
        testResults.addResult(TestResult.success(test.getName(), test.getParameters()));
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish()
     */
    public void onFinish() {
        testSuiteElement.setAttribute("errors", "" + testResults.getFailed());
        testSuiteElement.setAttribute("failures", "0");
        testSuiteElement.setAttribute("tests", "" + (testResults.getSuccess() + testResults.getFailed()));
        testSuiteElement.setAttribute("time", getExecutionTime());
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(java.lang.Throwable)
     */
    public void onFinishFailure(Throwable cause) {
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess()
     */
    public void onFinishSuccess() {
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart()
     */
    public void onStart() {
        startExecutionTime();

        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementation domImpl = (DOMImplementation) registry.getDOMImplementation("LS");

            doc = domImpl.createDocument("", "testsuite", null);

            testSuiteElement = doc.getDocumentElement();
            testSuiteElement.setAttribute("errors", "0");
            testSuiteElement.setAttribute("failures", "0");
            testSuiteElement.setAttribute("name", "citrus.AllTests");
            testSuiteElement.setAttribute("tests", "0");
            testSuiteElement.setAttribute("time", "0.0");
        } catch (RuntimeException e) {
            log.error("Error initialising reporter", e);
        } catch (Exception e) {
            log.error("Error initialising reporter", e);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(java.lang.Throwable)
     */
    public void onStartFailure(Throwable cause) {
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess()
     */
    public void onStartSuccess() {
    }
    
    /**
     * Track time for test suite execution.
     */
    private void startExecutionTime() {
        overallExecutionTime = System.currentTimeMillis();
    }

    /**
     * Get current execution time of test suite.
     * @return
     */
    private String getExecutionTime() {
        overallExecutionTime = System.currentTimeMillis() - overallExecutionTime;
        return decFormat.format(((double)(overallExecutionTime))/1000);
    }

    /**
     * Track test execution time.
     * @param testName
     */
    private void startTestExecution(String testName) {
        testExecutionTime.put(testName, System.currentTimeMillis());
    }

    /**
     * Get current test execution time.
     * @param testName
     * @return
     */
    private String getTestExecutionTime(String testName) {
        return decFormat.format(((double)(System.currentTimeMillis() - testExecutionTime.get(testName)))/1000);
    }

    /**
     * Remove test execution time for test name.
     * @param testName
     */
    private void removeTestExecutionTime(String testName) {
        testExecutionTime.remove(testName);
    }

    /**
     * Set the target output time.
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(Resource outputFile) {
        this.outputFile = outputFile;
    }
}
