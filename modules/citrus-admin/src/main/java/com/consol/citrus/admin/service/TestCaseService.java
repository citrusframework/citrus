/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.admin.model.*;

import java.util.List;

/**
 * Test case related activities get bundled in this service implementation. Service lists all test cases,
 * executes tests and provides test case information.
 * 
 * @author Christoph Deppisch
 */
public interface TestCaseService {
    
    /**
     * Lists all available Citrus test cases.
     * @return
     */
    List<TestCaseInfo> getTests();

    /**
     * Gets test case details such as status, description, author.
     * @param packageName
     * @param testName
     * @param type
     * @return
     */
    TestCaseDetail getTestDetail(String packageName, String testName, TestCaseType type);
    
    /**
     * Runs a test case and returns result outcome (success or failure).
     * @param testName
     * @return
     */
    TestResult executeTest(String testName);
    
    /**
     * Gets the source code for the given test.
     * @param packageName
     * @param name
     * @param type
     * @return
     */
    String getSourceCode(String packageName, String name, TestCaseType type);

    /**
     * Searches directory for Citrus test cases and constructs proper file tree model.
     * @param dir
     * @return
     */
    FileTreeModel getTestFileTree(String dir);
}
