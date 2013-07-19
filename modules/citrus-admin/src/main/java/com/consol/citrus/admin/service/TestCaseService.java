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

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.executor.TestExecutor;
import com.consol.citrus.admin.model.TestCaseDetail;
import com.consol.citrus.admin.model.TestCaseItem;
import com.consol.citrus.admin.model.TestResult;
import com.consol.citrus.admin.spring.model.SpringBeans;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.model.testcase.core.Testcase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

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

    @Autowired
    @Qualifier("jaxbMarshaller")
    private Unmarshaller unmarshaller;

    /**
     * Lists all available Citrus test cases from classpath.
     * @return
     */
    public List<TestCaseItem> getAllTests() {
        return testExecutor.getTests();
    }

    /**
     * Gets test case details such as status, description, author.
     * @return
     */
    public TestCaseDetail getTestDetails(String packageName, String testName) {
        // TODO also get testng groups from java part
        TestCaseDetail testCase = new TestCaseDetail();
        testCase.setName(testName);
        testCase.setPackageName(packageName);

        String xmlPart = testExecutor.getSourceCode(packageName, testName, "xml");

        try {
            Testcase test = ((SpringBeans) unmarshaller.unmarshal(new StringSource(xmlPart))).getTestcase();
            testCase.setDetail(test);
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("", e);
        }

        return testCase;
    }
    
    /**
     * Runs a test case and returns result outcome (success or failure).
     * @param testName
     * @return
     */
    public TestResult executeTest(String testName) {
        TestResult result = new TestResult();
        TestCaseItem testCase = new TestCaseItem();
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
     * Gets the test base directory according to the test executor used.
     * @return
     */
    public String getTestDirectory() {
        return testExecutor.getTestDirectory();
    }
}
