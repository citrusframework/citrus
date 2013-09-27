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

import com.consol.citrus.admin.launcher.ProcessMonitor;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.service.TestCaseServiceDelegate;
import com.consol.citrus.admin.util.FileHelper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.List;

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
    private TestCaseServiceDelegate testCaseService;

    @Autowired
    private FileHelper fileHelper;
    
    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public List<TestCaseInfo> list() {
        return testCaseService.getTests();
    }

    @RequestMapping(method = { RequestMethod.POST })
    @ResponseBody
    public ModelAndView list(@RequestParam("dir") String dir) {
        ModelAndView view = new ModelAndView("FileTree");

        FileTreeModel model = testCaseService.getTestFileTree(FilenameUtils.separatorsToSystem(fileHelper.decodeDirectoryUrl(dir, "")));

        if (StringUtils.hasText(model.getCompactFolder())) {
            view.addObject("compactFolder", FilenameUtils.separatorsToUnix(model.getCompactFolder()));
            view.addObject("baseDir", FilenameUtils.separatorsToUnix(fileHelper.decodeDirectoryUrl(dir, "")
                    + model.getCompactFolder() + File.separator));
        } else {
            view.addObject("baseDir", FilenameUtils.separatorsToUnix(fileHelper.decodeDirectoryUrl(dir, "")));
        }

        view.addObject("folders", model.getFolders());
        view.addObject("xmlFiles", model.getXmlFiles());
        view.addObject("javaFiles", model.getJavaFiles());

        return view;
    }

    @RequestMapping(value="/details/{package}/{name}/{type}", method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseDetail getTestDetail(@PathVariable("package") String testPackage, @PathVariable("name") String testName,
                                        @PathVariable("type") String type) {
        return testCaseService.getTestDetail(testPackage, testName, TestCaseType.valueOf(type.toUpperCase()));
    }
    
    @RequestMapping(value="/source/{package}/{name}/{type}", method = { RequestMethod.GET })
    @ResponseBody
    public String getSourceCode(@PathVariable("package") String testPackage, @PathVariable("name") String testName,
                                @PathVariable("type") String type) {
        return testCaseService.getSourceCode(testPackage, testName, TestCaseType.valueOf(type.toUpperCase()));
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
