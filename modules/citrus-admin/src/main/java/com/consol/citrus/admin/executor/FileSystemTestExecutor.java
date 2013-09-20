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

import com.consol.citrus.*;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.TestCaseInfo;
import com.consol.citrus.admin.service.ConfigurationService;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes test case from file system by starting new Citrus command line instance.
 * @author Christoph Deppisch
 */
public class FileSystemTestExecutor implements TestExecutor{

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FileSystemTestExecutor.class);

    @Autowired
    private ConfigurationService configService;
    
    /**
     * {@inheritDoc}
     */
    public List<TestCaseInfo> getTests() {
        List<TestCaseInfo> tests = new ArrayList<TestCaseInfo>();
        String testDirectory = getTestDirectory();
        
        List<File> testFiles = FileUtils.getTestFiles(testDirectory);
        
        for (File file : testFiles) {
            String testName = file.getName().substring(0, file.getName().lastIndexOf("."));
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
            Resource[] javaSources = new PathMatchingResourcePatternResolver().getResources("file:" + testDirectory + "**/*.java");

            for (Resource resource : javaSources) {
                File file = resource.getFile();
                String testName = file.getName().substring(0, file.getName().lastIndexOf("."));
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

    /**
     * {@inheritDoc}
     */
    public void execute(String testName) throws ParseException {
        String testDirectory = getTestDirectory();
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] { "-test", testName, "-testdir", testDirectory }));
        citrus.run();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getSourceCode(String testPackage, String testName, String type) {
        String dir = type.equals("java") ? getJavaDirectory() : getTestDirectory();

        try {
            String sourceFilePath = dir + File.separator + testPackage.replaceAll("\\.", File.separator) + File.separator + testName + "." + type;

            if (new File(sourceFilePath).exists()) {
                return FileUtils.readToString(new FileInputStream(sourceFilePath));
            } else {
                return "";
            }
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("Failed to load test case source code: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    public String getTestDirectory() {
        return new File(configService.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_TEST_DIRECTORY;
    }

    /**
     * Gets the current test directory based on project home and default test directory.
     * @return
     */
    public String getJavaDirectory() {
        return new File(configService.getProjectHome()).getAbsolutePath() + File.separator + CitrusConstants.DEFAULT_JAVA_DIRECTORY;
    }

}
