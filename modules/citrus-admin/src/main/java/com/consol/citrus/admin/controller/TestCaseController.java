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
import com.consol.citrus.admin.model.TestCaseDetail;
import com.consol.citrus.admin.model.TestCaseInfo;
import com.consol.citrus.admin.service.TestCaseService;
import com.consol.citrus.admin.util.FileHelper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private FileHelper fileHelper;
    
    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public List<TestCaseInfo> list() {
        return testCaseService.getAllTests();
    }

    @RequestMapping(method = { RequestMethod.POST })
    @ResponseBody
    public ModelAndView list(@RequestParam("dir") String dir) {
        String testDirectory = testCaseService.getTestDirectory() + FilenameUtils.separatorsToSystem(fileHelper.decodeDirectoryUrl(dir, ""));
        String javaDirectory = testCaseService.getJavaDirectory() + FilenameUtils.separatorsToSystem(fileHelper.decodeDirectoryUrl(dir, ""));

        String[] folders = null;
        String[] xmlFiles;
        String[] javaFiles;
        String compactFolder = "";
        do {
            if (folders != null) {
                if (StringUtils.hasText(compactFolder)) {
                    compactFolder += File.separator + folders[0];
                } else {
                    compactFolder = folders[0];
                }
            }

            folders = fileHelper.getFolders(StringUtils.hasText(compactFolder) ? javaDirectory + compactFolder : javaDirectory);
            xmlFiles = fileHelper.getFiles(StringUtils.hasText(compactFolder) ? testDirectory + compactFolder : testDirectory, ".xml");
            javaFiles = fileHelper.getFiles(StringUtils.hasText(compactFolder) ? javaDirectory + compactFolder : javaDirectory, ".java");
        } while (folders.length == 1 && xmlFiles.length == 0 && javaFiles.length == 0);

        List<Map<String, String>> xmlTestFiles = new ArrayList<Map<String, String>>();
        for (String xmlFile : xmlFiles) {
            HashMap<String, String> properties = new HashMap<String, String>();

            properties.put("fileName", xmlFile);
            properties.put("extension", "xml");
            properties.put("filePath", testDirectory
                    + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            xmlTestFiles.add(properties);
        }

        List<Map<String, String>> javaTestFiles = new ArrayList<Map<String, String>>();
        for (String javaFile : javaFiles) {
            HashMap<String, String> properties = new HashMap<String, String>();

            properties.put("fileName", javaFile);
            properties.put("extension", "java");
            properties.put("filePath", javaDirectory
                    + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            javaTestFiles.add(properties);
        }

        ModelAndView view = new ModelAndView("FileTree");

        if (StringUtils.hasText(compactFolder)) {
            view.addObject("compactFolder", FilenameUtils.separatorsToUnix(compactFolder));
        }

        view.addObject("baseDir", FilenameUtils.separatorsToUnix(fileHelper.decodeDirectoryUrl(dir, "")
                + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : "")));
        view.addObject("folders", folders);
        view.addObject("xmlFiles", xmlTestFiles);
        view.addObject("javaFiles", javaTestFiles);

        return view;
    }

    @RequestMapping(value="/details/{package}/{name}", method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseDetail getTestDetails(@PathVariable("package") String testPackage, @PathVariable("name") String testName) {
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
