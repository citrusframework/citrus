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

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.cli.GnuParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusCliOptions;
import com.consol.citrus.admin.model.TestCaseType;
import com.consol.citrus.admin.model.TestResult;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * @author Christoph Deppisch
 */
@Component
public class TestCaseService {
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCaseService.class);
    
    @Autowired
    private AppContextHolder appContextHolder;
    
    /** Project home property name */
    private static final String PROJECT_HOME = "project.home";

    /** Base package for test cases to look for */
    private static final String BASE_PACKAGE = "test.base.package";
    
    /**
     * Lists all available Citrus test cases from classpath.
     * @return
     */
    public List<TestCaseType> getAllTests() {
        List<TestCaseType> testCases = new ArrayList<TestCaseType>();
        
        List<String> testFiles = findTestsInClasspath(System.getProperty(BASE_PACKAGE, "com.consol.citrus"));
        
        for (String file : testFiles) {
            String testName = file.substring(file.lastIndexOf(".") + 1);
            String testPackageName = file.substring(0, file.length() - testName.length() - 1)
                    .replace(File.separatorChar, '.');
            
            TestCaseType testCase = new TestCaseType();
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            
            addTestCaseInfo(testCase);
            
            testCases.add(testCase);
        }
        
        return testCases;
    }
    
    /**
     * Gets the source code for a given test case. Either getting the XML or Java part of the test.
     * @param testPackage
     * @param testName
     * @param type
     * @return
     */
    public String getSourceCode(String testPackage, String testName, String type) {
        Resource testFile = new PathMatchingResourcePatternResolver().getResource(testPackage.replaceAll("\\.", "/") + "/" + testName + "." + type);
        
        try {
            return FileUtils.readToString(testFile);
        } catch (IOException e) {
            return "Failed to load test case file: " + e.getMessage();
        }
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
            Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), new String[] { "-test", testName, "-testdir", System.getProperty(PROJECT_HOME) }));
            citrus.run();
            
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
        
        Map<String, TestReporter> reporters = appContextHolder.getApplicationContext().getBeansOfType(TestReporter.class);
        for (TestReporter reporter : reporters.values()) {
            reporter.clearTestResults();
        }
        
        return result;
    }
    
    /**
     * Finds test case related information and adds it to the test case type.
     * @param testCase
     */
    private void addTestCaseInfo(TestCaseType testCase) {
        try {
            Class<?> javaPart = Class.forName(testCase.getPackageName() + "." + testCase.getName());
            
            for (Method method : javaPart.getMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    testCase.setGroups(StringUtils.arrayToCommaDelimitedString(method.getAnnotation(Test.class).groups()));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Unable to find test case in classpath", e);
        }
        
        String xmlPart = getSourceCode(testCase.getPackageName(), testCase.getName(), "xml");
        
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
    
    /**
     * Finds all test cases in classpath starting in given base package. Searches for 
     * **.class files extending AbstractTestNGCitrusTest superclass.
     * 
     * @param basePackage
     * @return
     */
    private List<String> findTestsInClasspath(String basePackage) {
        List<String> testCaseNames = new ArrayList<String>();
        
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, new StandardServletEnvironment());
        
        scanner.addIncludeFilter(new CitrusTestTypeFilter());
        
        Set<BeanDefinition> findings = scanner.findCandidateComponents(basePackage);
        
        for (BeanDefinition bean : findings) {
            testCaseNames.add(bean.getBeanClassName());
        }
        
        return testCaseNames;
    }
    
    /**
     * Class type filter searches for subclasses of {@link AbstractTestNGCitrusTest}
     */
    private static final class CitrusTestTypeFilter extends AbstractClassTestingTypeFilter {
        @Override
        protected boolean match(ClassMetadata metadata) {
            return !metadata.getClassName().equals(TestNGCitrusTestBuilder.class.getName()) && 
                    metadata.getSuperClassName().equals(AbstractTestNGCitrusTest.class.getName());
        }
    }

}
