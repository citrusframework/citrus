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

package com.consol.citrus.admin.executor;

import java.util.List;

import com.consol.citrus.admin.model.TestCaseDetail;
import com.consol.citrus.admin.model.TestCaseItem;

/**
 * @author Christoph Deppisch
 */
public interface TestExecutor {

    /**
     * Get all available test cases in default Citrus test directory
     * @return
     */
    List<TestCaseItem> getTests();
    
    /**
     * Run test and throw exception when failed.
     * @param testName
     * @throws Exception
     */
    void execute(String testName) throws Exception;
    
    /**
     * Gets the source code for a given test case. Either getting the XML or Java part of the test.
     * @param testPackage
     * @param testName
     * @param type
     * @return
     */
    String getSourceCode(String testPackage, String testName, String type);

    /**
     * Gets the test base directory.
     * @return
     */
    String getTestDirectory();

    /**
     * Gets the java base directory.
     * @return
     */
    String getJavaDirectory();
}
