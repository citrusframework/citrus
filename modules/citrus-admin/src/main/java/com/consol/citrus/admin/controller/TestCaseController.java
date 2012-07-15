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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.GnuParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusCliOptions;
import com.consol.citrus.admin.model.TestCaseInfoType;
import com.consol.citrus.admin.model.TestCaseList;
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

    /** Working directory = base of Citrus project */
    private static final String WORKING_DIRECTORY = "working.directory";
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCaseController.class);

    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseList list(HttpEntity<String> requestEntity) {
        TestCaseList testCaseList = new TestCaseList();
        
        List<File> testFiles = FileUtils.getTestFiles(System.getProperty(WORKING_DIRECTORY));
        
        for (File file : testFiles) {
            TestCaseInfoType testCaseInfo = new TestCaseInfoType();
            testCaseInfo.setName(file.getName().substring(0, file.getName().length() - 4));
            testCaseInfo.setPackageName(file.getAbsolutePath().substring(System.getProperty(WORKING_DIRECTORY).length() + 1, 
                                            file.getAbsolutePath().length() - file.getName().length() - 1)
                                            .replace(File.separatorChar, '.'));
            testCaseList.getTestCaseInfos().add(testCaseInfo);
        }
        
        return testCaseList;
    }
    
    @RequestMapping(value="/{package}/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public String getTestCase(@PathVariable("package") String testPackage, @PathVariable("name") String testName) {
        Resource testFile = FileUtils.getResourceFromFilePath(System.getProperty(WORKING_DIRECTORY) + File.separator + testPackage.replaceAll("\\.", File.separator) + File.separator + testName + ".xml");
        
        try {
            return FileUtils.readToString(testFile);
        } catch (IOException e) {
            return "Failed to load test case file: " + e.getMessage();
        }
    }
    
    @RequestMapping(value="/execute/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public ResponseEntity<?> execute(@PathVariable("name") String testName) {
        try {
            Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), new String[] { "-test", testName, "-testdir", System.getProperty(WORKING_DIRECTORY) }));
            citrus.run();
        } catch (Exception e) {
            log.warn("Failed to execute Citrus test case '" + testName + "'", e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
