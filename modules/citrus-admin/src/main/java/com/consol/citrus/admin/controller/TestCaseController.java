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
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.model.TestCaseInfoType;
import com.consol.citrus.admin.model.TestCaseList;
import com.consol.citrus.util.FileUtils;

/**
 *
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/testcase")
public class TestCaseController {

    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public TestCaseList list(HttpEntity<String> requestEntity) {
        TestCaseList testCaseList = new TestCaseList();
        
        List<File> testFiles = FileUtils.getTestFiles(System.getProperty("working.directory"));
        
        for (File file : testFiles) {
            TestCaseInfoType testCaseInfo = new TestCaseInfoType();
            testCaseInfo.setName(file.getName());
            testCaseList.getTestCaseInfos().add(testCaseInfo);
        }
        
        return testCaseList;
    }
}
