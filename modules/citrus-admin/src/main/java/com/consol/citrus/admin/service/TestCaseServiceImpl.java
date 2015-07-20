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
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
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

    /** Test executor works on filesystem */
    @Autowired
    private MavenTestExecutor mavenTestExecutor;

    /** Test executor works on project classpath */
    @Autowired
    private ClasspathTestExecutor classpathTestExecutor;

    @Override
    public List<TestCaseData> getTests(Project project) {
        List<TestCaseData> tests = new ArrayList<TestCaseData>();

        List<File> testFiles = FileUtils.getTestFiles(getTestDirectory(project));
        for (File file : testFiles) {
            String testName = FilenameUtils.getBaseName(file.getName());
            String testPackageName = file.getPath().substring(getTestDirectory(project).length(), file.getPath().length() - file.getName().length())
                    .replace(File.separatorChar, '.');

            if (testPackageName.endsWith(".")) {
                testPackageName = testPackageName.substring(0, testPackageName.length() - 1);
            }

            TestCaseData testCase = new TestCaseData();
            testCase.setType(TestCaseType.XML);
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator + FilenameUtils.getBaseName(file.getName()));
            testCase.setLastModified(file.lastModified());

            tests.add(testCase);
        }

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(getJavaDirectory(project)) + "**/*.java");

            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackage = file.getParentFile().getAbsolutePath().substring(getJavaDirectory(project).length()).replace(File.separatorChar, '.');

                if (knownToClasspath(testPackage, testName)) {
                    tests.addAll(getTestCaseInfoFromClass(testPackage, testName, file));
                } else {
                    tests.addAll(getTestCaseInfoFromFile(testPackage, testName, file));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read Java source files - list of test cases for this project is incomplete", e);
        }

        return tests;
    }

    @Override
    public Long getTestCount(Project project) {
        Long testCount = Long.valueOf(FileUtils.getTestFiles(getTestDirectory(project)).size());

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(getJavaDirectory(project)) + "**/*.java");
            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackage = file.getParentFile().getAbsolutePath().substring(getJavaDirectory(project).length()).replace(File.separatorChar, '.');

                if (knownToClasspath(testPackage, testName)) {
                    testCount += getTestCaseInfoFromClass(testPackage, testName, file).size();
                } else {
                    testCount += getTestCaseInfoFromFile(testPackage, testName, file).size();
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read Java source files - list of test cases for this project is incomplete", e);
        }

        return testCount;
    }

    @Override
    public TestResult executeTest(Project project, String packageName, String testName, String runConfigurationId) {
        TestResult result = new TestResult();
        TestCaseData testCase = new TestCaseData();
        testCase.setName(testName);
        result.setTestCase(testCase);

        try {
            RunConfiguration configuration = project.getRunConfiguration(runConfigurationId);
            TestExecutor<RunConfiguration> testExecutor = getTestExecutor(configuration);
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

    @Override
    public String getSourceCode(Project project, String packageName, String name, TestCaseType type) {
        String dir = type.equals(TestCaseType.JAVA) ? getJavaDirectory(project) : getTestDirectory(project);

        try {
            String sourceFilePath = dir + File.separator + packageName.replace('.', File.separatorChar) + File.separator + name + "." + type.name().toLowerCase();

            if (new File(sourceFilePath).exists()) {
                return FileUtils.readToString(new FileInputStream(sourceFilePath));
            } else {
                return "";
            }
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("Failed to load test case source code", e);
        }
    }

    /**
     * Adds test info by reading file resource as text content. Searches for class annotations and method annotations
     * on a text based search. This approach does not need to instantiate the class so Java source must not necessarily be
     * part of the classpath.
     * @param testPackage
     * @param testName
     * @param file
     */
    private List<TestCaseData> getTestCaseInfoFromFile(String testPackage, String testName, File file) {
        List<TestCaseData> tests = new ArrayList<TestCaseData>();

        try {
            String javaContent = FileUtils.readToString(new FileInputStream(file));
            javaContent = StringUtils.trimAllWhitespace(javaContent);
            String citrusAnnotation = "@CitrusTest";

            if (javaContent.contains(citrusAnnotation)) {
                int position = javaContent.indexOf(citrusAnnotation);
                while (position > 0) {
                    String methodContent = javaContent.substring(position);
                    TestCaseData testCase = new TestCaseData();
                    testCase.setType(TestCaseType.JAVA);
                    testCase.setPackageName(testPackage);
                    testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()));
                    testCase.setLastModified(file.lastModified());

                    if (methodContent.startsWith(citrusAnnotation + "(")) {
                        String annotationProps = methodContent.substring(methodContent.indexOf('('), methodContent.indexOf(')'));
                        if (StringUtils.hasText(annotationProps) && annotationProps.contains("name=\"")) {
                            String methodName = annotationProps.substring(annotationProps.indexOf("name=\"") + "name=\"".length());
                            methodName = methodName.substring(0, methodName.indexOf('"'));
                            testCase.setName(methodName);
                        }
                    }

                    if (!StringUtils.hasText(testCase.getName())) {
                        String methodName = methodContent.substring(methodContent.indexOf("publicvoid") + "publicvoid".length());
                        methodName = methodName.substring(0, methodName.indexOf('('));
                        testCase.setName(methodName);
                    }

                    tests.add(testCase);
                    position = javaContent.indexOf(citrusAnnotation, position + citrusAnnotation.length());
                }
            } else if (javaContent.contains(TestNGCitrusTestDesigner.class.getSimpleName()) ||
                       javaContent.contains(JUnit4CitrusTestDesigner.class.getSimpleName())) {
                TestCaseData testCase = new TestCaseData();
                testCase.setType(TestCaseType.JAVA);
                testCase.setName(testName);
                testCase.setPackageName(testPackage);
                testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()));
                testCase.setLastModified(file.lastModified());

                tests.add(testCase);
            } else {
                log.debug("Skipping java source as it is not a valid Citrus test: " + testPackage + "." + testName);
            }
        } catch (IOException e) {
            log.warn("Unable to access Java source on file system: " + testPackage + "." + testName, e);
        }

        return tests;
    }

    /**
     * Adds test info from class information such as Java 5 annotations. Method has to
     * instantiate class in order to read this information.
     * @param testPackage
     * @param testName
     * @param file
     */
    private List<TestCaseData> getTestCaseInfoFromClass(String testPackage, String testName, File file) {
        List<TestCaseData> tests = new ArrayList<TestCaseData>();

        try {
            Class<?> testClass = Class.forName(testPackage + "." + testName);

            if (TestDesigner.class.isAssignableFrom(testClass) ||
                    TestRunner.class.isAssignableFrom(testClass)) {
                List<String> methods = getTestMethods(testClass);
                for (String method : methods) {
                    TestCaseData testCase = new TestCaseData();
                    testCase.setType(TestCaseType.JAVA);
                    testCase.setName(method);
                    testCase.setPackageName(testPackage);
                    testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()) + "." + method);
                    testCase.setLastModified(file.lastModified());

                    tests.add(testCase);
                }

                if (tests.isEmpty()) {
                    // there were no Citrus annotated methods found so lets add the class itself as test case info
                    TestCaseData testCase = new TestCaseData();
                    testCase.setType(TestCaseType.JAVA);
                    testCase.setName(testName);
                    testCase.setPackageName(testPackage);
                    testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()));
                    testCase.setLastModified(file.lastModified());

                    tests.add(testCase);
                }
            } else {
                log.debug("Skipping java source as it is not a valid Citrus test builder: " + testPackage + "." + testName);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Java source is not part of classpath: " + testPackage + "." + testName);
        }

        return tests;
    }

    /**
     * Try to instantiate class with package and name. If class is accessible from classpath
     * method returns true otherwise false.
     * @param testPackage
     * @param testName
     * @return
     */
    private boolean knownToClasspath(String testPackage, String testName) {
        try {
            Class.forName(testPackage + "." + testName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
                CitrusTest citrusAnnotation = method.getAnnotation(CitrusTest.class);

                if (StringUtils.hasText(citrusAnnotation.name())) {
                    methodNames.add(citrusAnnotation.name());
                } else {
                    // use default method name as test
                    methodNames.add(method.getName());
                }
            }
        }

        return methodNames;
    }

    /**
     * Finds proper test case executor implementation for run configuration.
     * @param configuration
     * @return
     */
    private TestExecutor getTestExecutor(RunConfiguration configuration) {
        if (configuration instanceof MavenRunConfiguration) {
            return mavenTestExecutor;
        } else if (configuration instanceof ClasspathRunConfiguration) {
            return classpathTestExecutor;
        }

        throw new CitrusAdminRuntimeException("Unable to execute test for run configuration: " + configuration.getId());
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getTestDirectory(Project project) {
        return new File(project.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_TEST_DIRECTORY;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    private String getJavaDirectory(Project project) {
        return new File(project.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_JAVA_DIRECTORY;
    }
}
