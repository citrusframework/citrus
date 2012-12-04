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

package com.consol.citrus.admin.controller;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.GnuParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusCliOptions;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.service.AppContextHolder;
import com.consol.citrus.admin.service.TestCaseService;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.util.FileUtils;

/**
 * Controller manages test case related queries like get all tests
 * or getting a specific test case info.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/testcase")
public class TestCaseController {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCaseController.class);
    
    @Autowired
    private AppContextHolder appContextHolder;
    
    @Autowired
    private TestCaseService testCaseService;
    
    /** Base package for test cases to look for */
    private String basePackage = "com.consol.citrus";
    
    /** Project home property name */
    private static final String PROJECT_HOME = "project.home";
    
    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseList list(HttpEntity<String> requestEntity) {
        TestCaseList testCaseList = new TestCaseList();
        
        List<String> testFiles = testCaseService.findTestsInClasspath(basePackage); // TODO: make base package configurable
        
        for (String file : testFiles) {
            TestCaseInfoType testCaseInfo = new TestCaseInfoType();
            testCaseInfo.setName(file.substring(file.lastIndexOf(".") + 1));
            testCaseInfo.setPackageName(file.substring(0, file.length() - testCaseInfo.getName().length() - 1)
                                            .replace(File.separatorChar, '.'));
            testCaseList.getTestCaseInfos().add(testCaseInfo);
        }
        
        return testCaseList;
    }
    
    @RequestMapping(value="/{package}/{name}/{type}", method = { RequestMethod.GET })
    @ResponseBody
    public String getTestCase(@PathVariable("package") String testPackage, @PathVariable("name") String testName,
            @PathVariable("type") String type) {
        Resource testFile = new PathMatchingResourcePatternResolver().getResource(testPackage.replaceAll("\\.", File.separator) + File.separator + testName + "." + type);
        
        try {
            return FileUtils.readToString(testFile);
        } catch (IOException e) {
            return "Failed to load test case file: " + e.getMessage();
        }
    }
    
    @RequestMapping(value="/execute/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public TestResult execute(@PathVariable("name") String testName) {
        TestResult result = new TestResult();
        TestCaseInfoType testCaseInfo = new TestCaseInfoType();
        testCaseInfo.setName(testName);
        result.setTestCase(testCaseInfo);
        
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
}
