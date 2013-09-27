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

import com.consol.citrus.admin.configuration.ProjectNature;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default test case service implementation delegates to file system or classpath test case service
 * according to project configuration settings.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
@Component
public class TestCaseServiceDelegate implements TestCaseService {

    @Autowired
    private ClasspathTestCaseService classpathTestCaseService;
    @Autowired
    private FileSystemTestCaseService fileSystemTestCaseService;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public List<TestCaseInfo> getTests() {
        if (configurationService.getProjectNature().equals(ProjectNature.FILESYSTEM)) {
            return fileSystemTestCaseService.getTests();
        } else if (configurationService.getProjectNature().equals(ProjectNature.CLASSPATH)) {
            return classpathTestCaseService.getTests();
        } else {
            throw getUnsupportedProjectNatureException();
        }
    }

    @Override
    public TestCaseDetail getTestDetail(String packageName, String testName, TestCaseType type) {
        if (configurationService.getProjectNature().equals(ProjectNature.FILESYSTEM)) {
            return fileSystemTestCaseService.getTestDetail(packageName, testName, type);
        } else if (configurationService.getProjectNature().equals(ProjectNature.CLASSPATH)) {
            return classpathTestCaseService.getTestDetail(packageName, testName, type);
        } else {
            throw getUnsupportedProjectNatureException();
        }
    }

    @Override
    public TestResult executeTest(String testName) {
        if (configurationService.getProjectNature().equals(ProjectNature.FILESYSTEM)) {
            return fileSystemTestCaseService.executeTest(testName);
        } else if (configurationService.getProjectNature().equals(ProjectNature.CLASSPATH)) {
            return classpathTestCaseService.executeTest(testName);
        } else {
            throw getUnsupportedProjectNatureException();
        }
    }

    @Override
    public String getSourceCode(String packageName, String name, TestCaseType type) {
        if (configurationService.getProjectNature().equals(ProjectNature.FILESYSTEM)) {
            return fileSystemTestCaseService.getSourceCode(packageName, name, type);
        } else if (configurationService.getProjectNature().equals(ProjectNature.CLASSPATH)) {
            return classpathTestCaseService.getSourceCode(packageName, name, type);
        } else {
            throw getUnsupportedProjectNatureException();
        }
    }

    @Override
    public FileTreeModel getTestFileTree(String dir) {
        if (configurationService.getProjectNature().equals(ProjectNature.FILESYSTEM)) {
            return fileSystemTestCaseService.getTestFileTree(dir);
        } else if (configurationService.getProjectNature().equals(ProjectNature.CLASSPATH)) {
            return classpathTestCaseService.getTestFileTree(dir);
        } else {
            throw getUnsupportedProjectNatureException();
        }
    }

    /**
     * Just construct admin runtime exception due to unsupported project nature.
     * @return
     */
    private CitrusAdminRuntimeException getUnsupportedProjectNatureException() {
        return new CitrusAdminRuntimeException("Unsupported project nature: " + configurationService.getProjectNature());
    }
}
