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

import java.util.List;

import com.consol.citrus.admin.launcher.ProcessMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.model.TestCaseType;
import com.consol.citrus.admin.service.TestCaseService;

/**
 * Controller manages test case related queries like get all tests
 * or getting a specific test case.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/testcase")
public class TestCaseController {

    @Autowired
    private ProcessMonitor processMonitor;

    @Autowired
    private TestCaseService testCaseService;
    
    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public List<TestCaseType> list() {
        return testCaseService.getAllTests();
    }

    @RequestMapping(value="/details/{package}/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseType executeTest(@PathVariable("package") String testPackage, @PathVariable("name") String testName) {
        return testCaseService.getTestDetails(testPackage, testName);
    }
    
    @RequestMapping(value="/source/{package}/{name}/{type}", method = { RequestMethod.GET })
    @ResponseBody
    public String getSourceCode(@PathVariable("package") String testPackage, @PathVariable("name") String testName,
            @PathVariable("type") String type) {
        return testCaseService.getTestSources(testPackage, testName, type);
    }
    
    @RequestMapping(value="/execute/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public String executeTest(@PathVariable("name") String testName) {
        testCaseService.executeTest(testName);
        return "LAUNCHED";
    }

    @RequestMapping(value="/stop/{processId}", method = { RequestMethod.GET })
    @ResponseBody
    public String stopTest(@PathVariable("processId") String processId) {
        processMonitor.stopProcess(processId);
        return "STOPPED";
    }
}
