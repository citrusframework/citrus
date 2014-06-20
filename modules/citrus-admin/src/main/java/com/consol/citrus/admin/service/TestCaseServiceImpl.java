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
import com.consol.citrus.dsl.*;
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
import java.util.*;

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

        List<File> testFiles = FileUtils.getTestFiles(getTestDirectory());
        for (File file : testFiles) {
            String testName = FilenameUtils.getBaseName(file.getName());
            String testPackageName = file.getPath().substring(getTestDirectory().length(), file.getPath().length() - file.getName().length())
                    .replace(File.separatorChar, '.');

            if (testPackageName.endsWith(".")) {
                testPackageName = testPackageName.substring(0, testPackageName.length() - 1);
            }

            TestCaseInfo testCase = new TestCaseInfo();
            testCase.setType(TestCaseType.XML);
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator + FilenameUtils.getBaseName(file.getName()));
            testCase.setLastModified(file.lastModified());

            tests.add(testCase);
        }

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(getJavaDirectory()) + "**/*.java");

            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackage = file.getParentFile().getAbsolutePath().substring(getJavaDirectory().length()).replace(File.separatorChar, '.');

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
    public Long getTestCount() {
        Long testCount = Long.valueOf(FileUtils.getTestFiles(getTestDirectory()).size());

        try {
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + FilenameUtils.separatorsToUnix(getJavaDirectory()) + "**/*.java");
            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = FilenameUtils.getBaseName(file.getName());
                String testPackage = file.getParentFile().getAbsolutePath().substring(getJavaDirectory().length()).replace(File.separatorChar, '.');

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
    public TestResult executeTest(String packageName, String testName, String runConfigurationId) {
        TestResult result = new TestResult();
        TestCaseInfo testCase = new TestCaseInfo();
        testCase.setName(testName);
        result.setTestCase(testCase);

        try {
            RunConfiguration configuration = projectService.getActiveProject().getRunConfiguration(runConfigurationId);
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
            throw new CitrusAdminRuntimeException("Failed to load test case source code", e);
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
                    String testPackage = file.getParentFile().getAbsolutePath().substring(getJavaDirectory().length()).replace(File.separatorChar, '.');

                    if (knownToClasspath(testPackage, testName)) {
                        javaTestFiles.addAll(getTestFileTreeFromClass(javaDirectory, compactFolder, testPackage, testName));
                    } else {
                        javaTestFiles.addAll(getTestFileTreeFromFile(javaDirectory, compactFolder, testPackage, testName, file));
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
     * Constructs test tree from class information such as Java 5 annotations. Method has to instantiate the class so class must
     * be available on classpath. Class itself and all testable methods are added as executables to file tree model.
     * @param javaDirectory
     * @param compactFolder
     * @param testPackage
     * @param testName
     * @return
     */
    private List<? extends FileTreeModel.TestFileModel> getTestFileTreeFromClass(String javaDirectory, String compactFolder, String testPackage, String testName) {
        List<FileTreeModel.TestFileModel> javaTestFiles = new ArrayList<FileTreeModel.TestFileModel>();

        try {
            Class<?> testBuilderClass = Class.forName(testPackage + "." + testName);

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
                log.debug("Skipping java source as it is not a valid Citrus test builder: " + testPackage + "." +  testName);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Java source is not part of classpath: " + testPackage + "." + testName);
        }

        return javaTestFiles;
    }

    /**
     * Construct test tree from pure file content information. Class has not to be instantiated so it also has not to be
     * part of the classpath. Class itself and all testable methods are added as executables to file tree model.
     * @param javaDirectory
     * @param compactFolder
     * @param testPackage
     * @param testName
     * @param file
     * @return
     */
    private List<? extends FileTreeModel.TestFileModel> getTestFileTreeFromFile(String javaDirectory, String compactFolder, String testPackage, String testName, File file) {
        List<FileTreeModel.TestFileModel> javaTestFiles = new ArrayList<FileTreeModel.TestFileModel>();

        try {
            String javaContent = FileUtils.readToString(new FileInputStream(file));
            javaContent = StringUtils.trimAllWhitespace(javaContent);
            String citrusAnnotation = "@CitrusTest";

            if (javaContent.contains(TestNGCitrusTestBuilder.class.getSimpleName()) ||
                    javaContent.contains(JUnit4CitrusTestBuilder.class.getSimpleName()) ||
                    javaContent.contains(citrusAnnotation)) {
                FileTreeModel.TestFileModel fileModel = new FileTreeModel.TestFileModel();

                fileModel.setFileName(testName);
                fileModel.setFilePath(javaDirectory + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

                List<String> methods = new ArrayList<String>();
                int position = javaContent.indexOf(citrusAnnotation);
                while (position > 0) {
                    String methodContent = javaContent.substring(position);
                    String methodName = null;
                    if (methodContent.startsWith(citrusAnnotation + "(")) {
                        String annotationProps = methodContent.substring(methodContent.indexOf('('), methodContent.indexOf(')'));
                        if (StringUtils.hasText(annotationProps) && annotationProps.contains("name=\"")) {
                            methodName = annotationProps.substring(annotationProps.indexOf("name=\"") + "name=\"".length());
                            methodName = methodName.substring(0, methodName.indexOf('"'));
                        }
                    }

                    if (!StringUtils.hasText(methodName)) {
                        methodName = methodContent.substring(methodContent.indexOf("publicvoid") + "publicvoid".length());
                        methodName = methodName.substring(0, methodName.indexOf("("));
                    }

                    methods.add(methodName);
                    position = javaContent.indexOf(citrusAnnotation, position + citrusAnnotation.length());
                }

                fileModel.setTestMethods(methods);
                javaTestFiles.add(fileModel);
            } else {
                log.debug("Skipping java source as it is not a valid Citrus test: " + testPackage + "." + testName);
            }
        } catch (IOException e) {
            log.warn("Unable to access Java source on file system: " + testPackage + "." + testName, e);
        }

        return javaTestFiles;
    }

    /**
     * Adds test info by reading file resource as text content. Searches for class annotations and method annotations
     * on a text based search. This approach does not need to instantiate the class so Java source must not necessarily be
     * part of the classpath.
     * @param testPackage
     * @param testName
     * @param file
     */
    private List<TestCaseInfo> getTestCaseInfoFromFile(String testPackage, String testName, File file) {
        List<TestCaseInfo> tests = new ArrayList<TestCaseInfo>();

        try {
            String javaContent = FileUtils.readToString(new FileInputStream(file));
            javaContent = StringUtils.trimAllWhitespace(javaContent);
            String citrusAnnotation = "@CitrusTest";

            if (javaContent.contains(citrusAnnotation)) {
                int position = javaContent.indexOf(citrusAnnotation);
                while (position > 0) {
                    String methodContent = javaContent.substring(position);
                    TestCaseInfo testCase = new TestCaseInfo();
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
                        methodName = methodName.substring(0, methodName.indexOf("("));
                        testCase.setName(methodName);
                    }

                    tests.add(testCase);
                    position = javaContent.indexOf(citrusAnnotation, position + citrusAnnotation.length());
                }
            } else if (javaContent.contains(TestNGCitrusTestBuilder.class.getSimpleName()) ||
                       javaContent.contains(JUnit4CitrusTestBuilder.class.getSimpleName())) {
                TestCaseInfo testCase = new TestCaseInfo();
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
    private List<TestCaseInfo> getTestCaseInfoFromClass(String testPackage, String testName, File file) {
        List<TestCaseInfo> tests = new ArrayList<TestCaseInfo>();

        try {
            Class<?> testBuilderClass = Class.forName(testPackage + "." + testName);

            if (TestBuilder.class.isAssignableFrom(testBuilderClass)) {
                List<String> methods = getTestMethods(testBuilderClass);
                for (String method : methods) {
                    TestCaseInfo testCase = new TestCaseInfo();
                    testCase.setType(TestCaseType.JAVA);
                    testCase.setName(method);
                    testCase.setPackageName(testPackage);
                    testCase.setFile(file.getParentFile().getAbsolutePath() + File.separator +  FilenameUtils.getBaseName(file.getName()) + "." + method);
                    testCase.setLastModified(file.lastModified());

                    tests.add(testCase);
                }

                if (tests.isEmpty()) {
                    // there were no Citrus annotated methods found so lets add the class itself as test case info
                    TestCaseInfo testCase = new TestCaseInfo();
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
            return fileSystemTestExecutor;
        } else if (configuration instanceof ClasspathRunConfiguration) {
            return classpathTestExecutor;
        }

        throw new CitrusAdminRuntimeException("Unable to execute test for run configuration: " + configuration.getId());
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
