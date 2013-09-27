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

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.executor.ClasspathTestExecutor;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.util.FileHelper;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.io.*;
import java.util.*;

/**
 * Test case service reads tests from classpath and delegates to classpath test case executor for
 * test execution.
 * @author Christoph Deppisch
 * @since 1.4
 */
@Component
public class ClasspathTestCaseService extends AbstractTestCaseService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ClasspathTestCaseService.class);

    @Autowired
    private ConfigurationService configurationService;

    /** Test executor works on project classpath */
    @Autowired
    private ClasspathTestExecutor testExecutor;

    @Autowired
    private FileHelper fileHelper;

    @Override
    public List<TestCaseInfo> getTests() {
        List<TestCaseInfo> tests = new ArrayList<TestCaseInfo>();

        List<String> testFiles = findTestsInClasspath(configurationService.getBasePackage());

        for (String file : testFiles) {
            String testName = file.substring(file.lastIndexOf(".") + 1);
            String testPackageName = file.substring(0, file.length() - testName.length() - 1)
                    .replace(File.separatorChar, '.');

            TestCaseInfo testCase = new TestCaseInfo();
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            testCase.setFile(ClassUtils.convertClassNameToResourcePath(file));

            tests.add(testCase);
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
        Resource testFile = new PathMatchingResourcePatternResolver().getResource(packageName.replace('.', '/') + "/" + name + "." + type);

        try {
            return FileUtils.readToString(testFile);
        } catch (IOException e) {
            return "Failed to load test case file: " + e.getMessage();
        }
    }

    @Override
    public FileTreeModel getTestsAsFileTree(String dir) {
        FileTreeModel model = new FileTreeModel();

        String[] folders = null;
        String[] xmlFiles;
        String[] javaFiles;
        String compactFolder = "";

        if (!StringUtils.hasText(dir)) {
            compactFolder = configurationService.getBasePackage().replace('.', '/');
        }

        do {
            if (folders != null) {
                if (StringUtils.hasText(compactFolder)) {
                    compactFolder += File.separator + folders[0];
                } else {
                    compactFolder = folders[0];
                }
            }

            try {
                folders = fileHelper.getFolders(new ClassPathResource(dir + compactFolder).getFile());
                Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(dir + compactFolder + "/*Test.xml");
                xmlFiles = new String[fileResources.length];
                for (int i = 0; i < fileResources.length; i++) {
                    xmlFiles[i] = FilenameUtils.getBaseName(fileResources[i].getFilename());
                }

                fileResources = new PathMatchingResourcePatternResolver().getResources(dir + compactFolder + "/*.class");
                javaFiles = new String[fileResources.length];
                for (int i = 0; i < fileResources.length; i++) {
                    javaFiles[i] = FilenameUtils.getBaseName(fileResources[i].getFilename());
                }
            } catch (IOException e) {
                throw new CitrusAdminRuntimeException(e);
            }
        } while (folders.length == 1 && xmlFiles.length == 0 && javaFiles.length == 0);

        List<FileTreeModel.FileModel> xmlTestFiles = new ArrayList<FileTreeModel.FileModel>();
        for (String xmlFile : xmlFiles) {
            FileTreeModel.FileModel fileModel = new FileTreeModel.FileModel();

            fileModel.setFileName(xmlFile);
            fileModel.setExtension("xml");
            fileModel.setFilePath(dir + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            xmlTestFiles.add(fileModel);
        }

        List<FileTreeModel.FileModel> javaTestFiles = new ArrayList<FileTreeModel.FileModel>();
        for (String javaFile : javaFiles) {
            FileTreeModel.FileModel fileModel = new FileTreeModel.FileModel();

            fileModel.setFileName(javaFile);
            fileModel.setExtension("java");
            fileModel.setFilePath(dir + (StringUtils.hasText(compactFolder) ? compactFolder + File.separator : ""));

            javaTestFiles.add(fileModel);
        }

        model.setCompactFolder(compactFolder);
        model.setFolders(folders);
        model.setXmlFiles(xmlTestFiles);
        model.setJavaFiles(javaTestFiles);

        return model;
    }

    /**
     * Finds all test cases in classpath starting in given base package. Searches for
     * **.class files extending AbstractTestNGCitrusTest superclass.
     *
     * @param basePackage
     * @return
     */
    private List<String> findTestsInClasspath(String basePackage) {
        List<String> testCaseNames = new ArrayList<String>();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, new StandardServletEnvironment());
        scanner.addIncludeFilter(new CitrusTestTypeFilter());

        Set<BeanDefinition> findings = scanner.findCandidateComponents(basePackage);

        for (BeanDefinition bean : findings) {
            testCaseNames.add(bean.getBeanClassName());
        }

        return testCaseNames;
    }

    /**
     * Class type filter searches for subclasses of {@link com.consol.citrus.testng.AbstractTestNGCitrusTest}
     */
    private static final class CitrusTestTypeFilter extends AbstractClassTestingTypeFilter {
        @Override
        protected boolean match(ClassMetadata metadata) {
            return !metadata.getClassName().equals(TestNGCitrusTestBuilder.class.getName()) &&
                    (metadata.getSuperClassName().equals(AbstractTestNGCitrusTest.class.getName()) ||
                    metadata.getSuperClassName().equals(TestNGCitrusTestBuilder.class.getName()));
        }
    }
}
