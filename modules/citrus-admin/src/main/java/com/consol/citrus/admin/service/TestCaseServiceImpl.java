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
import com.consol.citrus.admin.configuration.*;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.executor.*;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.util.FileHelper;
import com.consol.citrus.dsl.TestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case service reads tests from file system and delegates to file system test executor for
 * test execution.
 * @author Christoph Deppisch
 * @since 1.4
 */
@Component
public class TestCaseServiceImpl extends AbstractTestCaseService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCaseServiceImpl.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ProjectService projectService;

    /** Test executor works on filesystem */
    @Autowired
    private FileSystemTestExecutor fileSystemTestExecutor;

    /** Test executor works on project classpath */
    @Autowired
    private ClasspathTestExecutor classpathTestExecutor;

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
            testCase.setType(TestCaseType.XML);
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator + FilenameUtils.getBaseName(file.getName()));

            tests.add(testCase);
        }

        testDirectory = getJavaDirectory();

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(testDirectory) + "**/*.java");

            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackageName = file.getParentFile().getAbsolutePath().substring(testDirectory.length()).replace(File.separatorChar, '.');

                try {
                    Class<?> testBuilderClass = Class.forName(testPackageName + "." + testName);

                    if (TestBuilder.class.isAssignableFrom(testBuilderClass)) {
                        TestCaseInfo testCase = new TestCaseInfo();
                        testCase.setType(TestCaseType.JAVA);
                        testCase.setName(testName);
                        testCase.setPackageName(testPackageName);
                        testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()));

                        tests.add(testCase);

                        List<String> methods = getTestMethods(testBuilderClass);
                        for (String method : methods) {
                            testCase = new TestCaseInfo();
                            testCase.setType(TestCaseType.JAVA);
                            testCase.setName(testName + "." + method);
                            testCase.setPackageName(testPackageName);
                            testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()) + "." + method);

                            tests.add(testCase);
                        }
                    } else {
                        log.debug("Skipping java source as it is not a test builder: " + testPackageName + "." + testName);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Skipping java source as it is not part of classpath: " + testPackageName + "." + testName);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read Java source files - list of test cases for this project is incomplete", e);
        }

        return tests;
    }

    @Override
    public TestResult executeTest(String packageName, String testName, String runConfigurationId) {
        TestResult result = new TestResult();
        TestCaseInfo testCase = new TestCaseInfo();
        testCase.setName(testName);
        result.setTestCase(testCase);

        try {
            RunConfiguration configuration = projectService.getActiveProject().getRunConfiguration(runConfigurationId);
            TestExecutor<RunConfiguration> testExecutor = getTestExecutorForConfiguration(configuration);
            testExecutor.execute(packageName, testName, configuration);

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

    /**
     * Finds proper test case executor implementation for run configuration.
     * @param configuration
     * @return
     */
    private TestExecutor getTestExecutorForConfiguration(RunConfiguration configuration) {
        if (configuration instanceof MavenRunConfiguration) {
            return fileSystemTestExecutor;
        } else if (configuration instanceof ClasspathRunConfiguration) {
            return classpathTestExecutor;
        }

        throw new CitrusAdminRuntimeException("Unable to execute test for run configuration: " + configuration.getId());
    }

    @Override
    public String getSourceCode(String packageName, String name, TestCaseType type) {
        String dir = type.equals(TestCaseType.JAVA) ? getJavaDirectory() : getTestDirectory();

        try {
            String sourceFilePath = dir + File.separator + packageName.replace('.', File.separatorChar) + File.separator + name + "." + type.name().toLowerCase();

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
    public FileTreeModel getTestFileTree(String dir) {
        FileTreeModel model = new FileTreeModel();

        String testDirectory = getTestDirectory() + dir;
        String javaDirectory = getJavaDirectory() + dir;

        String[] folders = null;
        List<FileTreeModel.TestFileModel> xmlTestFiles = new ArrayList<FileTreeModel.TestFileModel>();
        List<FileTreeModel.TestFileModel> javaTestFiles = new ArrayList<FileTreeModel.TestFileModel>();
        String compactFolder = "";
        do {
            if (folders != null) {
                if (StringUtils.hasText(compactFolder)) {
                    compactFolder += File.separator + folders[0];
                } else {
                    compactFolder = folders[0];
                }
            }

            folders = fileHelper.getFolders(new File(javaDirectory + compactFolder));
            String[] xmlFiles = fileHelper.getFiles(new File(testDirectory + compactFolder), ".xml");

            for (String xmlFile : xmlFiles) {
                FileTreeModel.TestFileModel fileModel = new FileTreeModel.TestFileModel();

                fileModel.setFileName(xmlFile);
                fileModel.setFilePath(testDirectory + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

                xmlTestFiles.add(fileModel);
            }

            try {
                Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + javaDirectory + compactFolder + "/*.java");
                for (Resource resource : javaSources) {
                    File file = resource.getFile();
                    String testName = FilenameUtils.getBaseName(file.getName());
                    String testPackageName = file.getParentFile().getAbsolutePath().substring(getJavaDirectory().length()).replace(File.separatorChar, '.');

                    try {
                        Class<?> testBuilderClass = Class.forName(testPackageName + "." + testName);

                        if (TestBuilder.class.isAssignableFrom(testBuilderClass)) {
                            FileTreeModel.TestFileModel fileModel = new FileTreeModel.TestFileModel();

                            fileModel.setFileName(testName);
                            fileModel.setFilePath(javaDirectory + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

                            List<String> methods = getTestMethods(testBuilderClass);
                            // only add methods in case several test methods found - one single method is represented by test class
                            if (methods.size() > 1) {
                                fileModel.setTestMethods(methods);
                            }

                            javaTestFiles.add(fileModel);
                        } else {
                            log.debug("Skipping java source as it is not a test builder: " + testPackageName + "." +  testName);
                        }
                    } catch (ClassNotFoundException e) {
                        log.warn("Skipping java source as it is not part of classpath: " + testPackageName + "." + testName);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to read Java source files - list of test cases for this project is incomplete", e);
            }
        } while (folders.length == 1 && xmlTestFiles.size() == 0 && javaTestFiles.size() == 0);

        model.setCompactFolder(compactFolder);
        model.setFolders(folders);
        model.setXmlFiles(xmlTestFiles);
        model.setJavaFiles(javaTestFiles);

        return model;
    }

    /**
     * Finds all Citrus annotated test methods in test class. Do only return methods when more than one test method
     * is found. Otherwise use class itself as test representation.
     * @param testClass
     * @return
     */
    private List<String> getTestMethods(Class<?> testClass) {
        List<String> methodNames = new ArrayList<String>();
        for (Method method : ReflectionUtils.getAllDeclaredMethods(testClass)) {
            if (method.getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);

                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    methodNames.add(citrusTestAnnotation.name());
                } else {
                    // use default method name as test
                    methodNames.add(method.getName());
                }
            }
        }

        return methodNames;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getTestDirectory() {
        return new File(projectService.getActiveProject().getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_TEST_DIRECTORY;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getJavaDirectory() {
        return new File(projectService.getActiveProject().getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_JAVA_DIRECTORY;
    }
}
