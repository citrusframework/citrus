/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.report;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.report.TestResult.RESULT;


public class JUnitReporter implements TestSuiteListener, TestListener, TestReporter {
    
    private TestResults testResults = new TestResults();
    
    private Document doc;

    private Element testSuiteElement;

    private Resource outputFile = new FileSystemResource("target/test-output/test-results.xml");

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JUnitReporter.class);

    private Map<String, Long> testExecutionTime = new HashMap<String, Long>();
    private Map<String, Long> testSuiteExecutionTime = new HashMap<String, Long>();

    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.000");

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    public void generateTestResults(TestSuite[] suites) {
        try {
            log.info("Generating JUnit results");

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationList domImplList = registry.getDOMImplementationList("LS");

            if(log.isDebugEnabled()) {
                for (int i = 0; i < domImplList.getLength(); i++) {
                    log.debug("Found DOMImplementationLS: " + domImplList.item(i));
                }
            }
            
            DOMImplementationLS domImpl;
            for (int i = 0; i < domImplList.getLength(); i++) {
                try {
                    domImpl = (DOMImplementationLS)domImplList.item(i);

                    if(log.isDebugEnabled()) {
                        log.debug("Using DOMSerializerImpl: " + domImpl.getClass().getName());
                    }

                    LSSerializer serializer = domImpl.createLSSerializer();

                    if(log.isDebugEnabled()) {
                        log.debug("Using LSSerializer: " + serializer.getClass().getName());
                    }

                    if (serializer.getDomConfig().canSetParameter("format-pretty-print", true)) {
                        if(log.isDebugEnabled()) {
                            log.debug("Setting parameter format-pretty-print " + true);
                        }
                        serializer.getDomConfig().setParameter("format-pretty-print", true);
                    }

                    if(outputFile.exists() == false) {
                        outputFile.getFile().getParentFile().mkdirs();
                        outputFile.createRelative("");
                    }
                    
                    if(log.isDebugEnabled()) {
                        log.debug("Serializing to file " + outputFile.getFile().toURI().toString());
                    }

                    serializer.writeToURI(doc, outputFile.getFile().toURI().toString());
                } catch(Exception e) {
                    log.error("Error during report generation", e);
                    continue;
                }

                break;
            }

            log.info("JUnit results successfully");
            if(log.isDebugEnabled()) {
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
            buf.append(cause.getMessage() + "\n");
            buf.append(cause.getClass().getName());
            for (int i = 0; i < cause.getStackTrace().length; i++) {
                buf.append("\n at " + cause.getStackTrace()[i]);
            }
            errorElement.setTextContent(buf.toString());
        } else {
            errorElement.setAttribute("message", "No message available");
            errorElement.setAttribute("type", "no.available");
            errorElement.setTextContent("No exception available");
        }


        testCaseElement.appendChild(errorElement);

        testSuiteElement.appendChild(testCaseElement);
        
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE));
        }
    }

    public void onTestFinish(TestCase test) {
        removeTestExecutionTime(test.getName());
    }

    public void onTestSkipped(TestCase test) {
        //		Element testCaseElement = doc.createElement("testcase");
        //
        //		testCaseElement.setAttribute("classname", test.getClass().getName());
        //		testCaseElement.setAttribute("name", test.getName());
        //		testCaseElement.setAttribute("time", test.getExecutionTime());
        //
        //		testSuiteElement.appendChild(testCaseElement);
        
        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP));
    }

    public void onTestStart(TestCase test) {
        startTestExecution(test.getName());
    }

    public void onTestSuccess(TestCase test) {
        Element testCaseElement = doc.createElement("testcase");

        testCaseElement.setAttribute("classname", test.getClass().getName());
        testCaseElement.setAttribute("name", test.getName());
        testCaseElement.setAttribute("time", getTestExecutionTime(test.getName()));

        testSuiteElement.appendChild(testCaseElement);
        
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS));
    }

    public void onFinish(TestSuite testsuite) {
        testSuiteElement.setAttribute("errors", "" + testResults.getFailed());
        testSuiteElement.setAttribute("failures", "0");
        testSuiteElement.setAttribute("tests", "" + (testResults.getSuccess() + testResults.getFailed()));
        testSuiteElement.setAttribute("time", getTestSuiteExecutionTime(testsuite.getName()));
    }

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
    }

    public void onFinishSuccess(TestSuite testsuite) {
    }

    public void onStart(TestSuite testsuite) {
        startTestSuiteExecutionTime(testsuite.getName());

        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementation domImpl = (DOMImplementation) registry.getDOMImplementation("LS");

            doc = domImpl.createDocument("", "testsuite", null);

            testSuiteElement = doc.getDocumentElement();
            testSuiteElement.setAttribute("errors", "0");
            testSuiteElement.setAttribute("failures", "0");
            testSuiteElement.setAttribute("name", testsuite.getName() + ".AllTests");
            testSuiteElement.setAttribute("tests", "0");
            testSuiteElement.setAttribute("time", "0.0");
        } catch (Exception e) {
            log.error("Error initialising reporter", e);
        }
    }

    public void onStartFailure(TestSuite testsuite, Throwable cause) {
    }

    public void onStartSuccess(TestSuite testsuite) {
    }

    private void startTestSuiteExecutionTime(String testSuiteName) {
        testSuiteExecutionTime.put(testSuiteName, System.currentTimeMillis());
    }

    private String getTestSuiteExecutionTime(String testSuiteName) {
        return decFormat.format(((double)(System.currentTimeMillis() - testSuiteExecutionTime.get(testSuiteName)))/1000);
    }

    private void startTestExecution(String testName) {
        testExecutionTime.put(testName, System.currentTimeMillis());
    }

    private String getTestExecutionTime(String testName) {
        return decFormat.format(((double)(System.currentTimeMillis() - testExecutionTime.get(testName)))/1000);
    }

    private void removeTestExecutionTime(String testName) {
        testExecutionTime.remove(testName);
    }

    /**
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(Resource outputFile) {
        this.outputFile = outputFile;
    }
}
