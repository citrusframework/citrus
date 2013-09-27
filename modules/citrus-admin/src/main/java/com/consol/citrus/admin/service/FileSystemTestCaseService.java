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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.executor.FileSystemTestExecutor;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.util.FileHelper;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Test case service reads tests from file system and delegates to file system test executor for
 * test execution.
 * @author Christoph Deppisch
 * @since 1.4
 */
@Component
public class FileSystemTestCaseService extends AbstractTestCaseService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FileSystemTestCaseService.class);

    @Autowired
    private ConfigurationService configurationService;

    /** Test executor works on filesystem */
    @Autowired
    private FileSystemTestExecutor testExecutor;

    @Autowired
    private FileHelper fileHelper;

    @Override
    public List<TestCaseInfo> getTests() {
        List<TestCaseInfo> tests = new ArrayList<TestCaseInfo>();
        String testDirectory = getTestDirectory();

        List<File> testFiles = FileUtils.getTestFiles(testDirectory);

        for (File file : testFiles) {
            String testName = FilenameUtils.getBaseName(file.getName());
            String testPackageName = file.getPath().substring(testDirectory.length(), file.getPath().length() - file.getName().length())
                    .replace(File.separatorChar, '.');

            if (testPackageName.endsWith(".")) {
                testPackageName = testPackageName.substring(0, testPackageName.length() - 1);
            }

            TestCaseInfo testCase = new TestCaseInfo();
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            testCase.setFile(file.getAbsolutePath());

            tests.add(testCase);
        }

        testDirectory = getJavaDirectory();

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(testDirectory) + "**/*.java");

            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackageName = file.getPath().substring(testDirectory.length(), file.getPath().length() - file.getName().length())
                        .replace(File.separatorChar, '.');

                if (testPackageName.endsWith(".")) {
                    testPackageName = testPackageName.substring(0, testPackageName.length() - 1);
                }

                TestCaseInfo testCase = new TestCaseInfo();
                testCase.setName(testName);
                testCase.setPackageName(testPackageName);
                testCase.setFile(file.getAbsolutePath());

                tests.add(testCase);
            }
        } catch (IOException e) {
            log.warn("Failed to read Java source files - list of test cases for this project is incomplete", e);
        }

        return tests;
    }

    @Override
    public TestResult executeTest(String testName) {
        TestResult result = new TestResult();
        TestCaseInfo testCase = new TestCaseInfo();
        testCase.setName(testName);
        result.setTestCase(testCase);

        try {
            testExecutor.execute(testName);

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

        return result;
    }

    @Override
    public String getSourceCode(String packageName, String name, String type) {
        String dir = type.equals("java") ? getJavaDirectory() : getTestDirectory();

        try {
            String sourceFilePath = dir + File.separator + packageName.replace('.', File.separatorChar) + File.separator + name + "." + type;

            if (new File(sourceFilePath).exists()) {
                return FileUtils.readToString(new FileInputStream(sourceFilePath));
            } else {
                return "";
            }
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("Failed to load test case source code: " + e.getMessage());
        }
    }

    @Override
    public FileTreeModel getTestsAsFileTree(String dir) {
        FileTreeModel model = new FileTreeModel();

        String testDirectory = getTestDirectory() + dir;
        String javaDirectory = getJavaDirectory() + dir;

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

            folders = fileHelper.getFolders(javaDirectory + compactFolder);
            xmlFiles = fileHelper.getFiles(testDirectory + compactFolder, ".xml");
            javaFiles = fileHelper.getFiles(javaDirectory + compactFolder, ".java");
        } while (folders.length == 1 && xmlFiles.length == 0 && javaFiles.length == 0);

        List<FileTreeModel.FileModel> xmlTestFiles = new ArrayList<FileTreeModel.FileModel>();
        for (String xmlFile : xmlFiles) {
            FileTreeModel.FileModel fileModel = new FileTreeModel.FileModel();

            fileModel.setFileName(xmlFile);
            fileModel.setExtension("xml");
            fileModel.setFilePath(testDirectory + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            xmlTestFiles.add(fileModel);
        }

        List<FileTreeModel.FileModel> javaTestFiles = new ArrayList<FileTreeModel.FileModel>();
        for (String javaFile : javaFiles) {
            FileTreeModel.FileModel fileModel = new FileTreeModel.FileModel();

            fileModel.setFileName(javaFile);
            fileModel.setExtension("java");
            fileModel.setFilePath(javaDirectory + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            javaTestFiles.add(fileModel);
        }

        model.setCompactFolder(compactFolder);
        model.setFolders(folders);
        model.setXmlFiles(xmlTestFiles);
        model.setJavaFiles(javaTestFiles);

        return model;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getTestDirectory() {
        return new File(configurationService.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_TEST_DIRECTORY;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getJavaDirectory() {
        return new File(configurationService.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_JAVA_DIRECTORY;
    }
}
