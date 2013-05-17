/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.admin.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.consol.citrus.admin.executor.TestExecutor;
import com.consol.citrus.admin.model.TestCaseType;
import com.consol.citrus.admin.model.TestResult;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * Test case related activities get bundled in this service implementation. Service lists all test cases,
 * executes tests and provides test case information.
 * 
 * @author Christoph Deppisch
 */
@Component
public class TestCaseService {
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCaseService.class);
    
    /** Test executor depends on type of project classpath or filesystem */
    @Autowired
    private TestExecutor testExecutor;

    /**
     * Lists all available Citrus test cases from classpath.
     * @return
     */
    public List<TestCaseType> getAllTests() {
        List<TestCaseType> tests = testExecutor.getTests();
        
        for (TestCaseType test : tests) {
            addTestCaseInfo(test);
        }
        
        return tests;
    }
    
    /**
     * Runs a test case and returns result outcome (success or failure).
     * @param testName
     * @return
     */
    public TestResult executeTest(String testName) {
        TestResult result = new TestResult();
        TestCaseType testCase = new TestCaseType();
        testCase.setName(testName);
        result.setTestCase(testCase);
        
        try {
            testExecutor.execute(testName);
            
            result.setSuccess(true);
        } catch (Exception e) {
            log.warn("Failed to execute Citrus test case '" + testName + "'", e);

            result.setSuccess(false);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(os));
            result.setStackTrace("Caused by: " + os.toString());
            
            if (e instanceof CitrusRuntimeException) {
                result.setFailureStack(((CitrusRuntimeException)e).getFailureStackAsString());
            }
        }
        
        return result;
    }
    
    /**
     * Gets the source code for the given test.
     * @param packageName
     * @param name
     * @param type
     * @return
     */
    public String getTestSources(String packageName, String name, String type) {
        return testExecutor.getSourceCode(packageName, name, type);
    }
    
    /**
     * Finds test case related information and adds it to the test case type.
     * @param testCase
     */
    private void addTestCaseInfo(TestCaseType testCase) {
        // TODO also get testng groups from java part
        String xmlPart = testExecutor.getSourceCode(testCase.getPackageName(), testCase.getName(), "xml");
        
        SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
        nsContext.bindNamespaceUri("spring", "http://www.springframework.org/schema/beans"); //TODO: remove hard coded namespace uri
        nsContext.bindNamespaceUri("citrus", "http://www.citrusframework.org/schema/testcase"); //TODO: remove hard coded namespace uri
        
        Document testCaseDocument = XMLUtils.parseMessagePayload(xmlPart);
        Node metaInfoNode = XPathUtils.evaluateAsNode(testCaseDocument, "/spring:beans/citrus:testcase/citrus:meta-info", nsContext);
        
        testCase.setAuthor(XPathUtils.evaluateAsString(metaInfoNode, "citrus:author", nsContext));
        testCase.setCreationDate(XPathUtils.evaluateAsString(metaInfoNode, "citrus:creationdate", nsContext));
        testCase.setLastUpdatedBy(XPathUtils.evaluateAsString(metaInfoNode, "citrus:last-updated-by", nsContext));
        testCase.setLastUpdated(XPathUtils.evaluateAsString(metaInfoNode, "citrus:last-updated-on", nsContext));
        testCase.setStatus(XPathUtils.evaluateAsString(metaInfoNode, "citrus:status", nsContext));
        
        try {
            testCase.setDescription(XPathUtils.evaluateAsString(testCaseDocument, "/spring:beans/citrus:testcase/citrus:description", nsContext));
        } catch (CitrusRuntimeException e) {
            testCase.setDescription("");
        }
    }
    
}
