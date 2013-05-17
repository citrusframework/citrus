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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusCliOptions;
import com.consol.citrus.admin.model.TestCaseType;
import com.consol.citrus.admin.service.ConfigurationService;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.util.FileUtils;

/**
 *
 * @author Christoph Deppisch
 */
public class ClasspathTestExecutor implements TestExecutor {

    @Autowired
    private ApplicationContextHolder appContextHolder;
    
    @Autowired
    private ConfigurationService configService;
    
    /** Base package for test cases to look for */
    private static final String BASE_PACKAGE = "test.base.package";

    /**
     * {@inheritDoc}
     */
    public List<TestCaseType> getTests() {
        List<TestCaseType> tests = new ArrayList<TestCaseType>();
        
        List<String> testFiles = findTestsInClasspath(System.getProperty(BASE_PACKAGE, "com.consol.citrus"));
        
        for (String file : testFiles) {
            String testName = file.substring(file.lastIndexOf(".") + 1);
            String testPackageName = file.substring(0, file.length() - testName.length() - 1)
                    .replace(File.separatorChar, '.');
            
            TestCaseType testCase = new TestCaseType();
            testCase.setName(testName);
            testCase.setPackageName(testPackageName);
            
            tests.add(testCase);
        }
        
        return tests;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(String testName) throws ParseException {
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] { "-test", testName, "-testdir", new File(configService.getProjectHome()).getAbsolutePath() }));
        citrus.run();
        
        Map<String, TestReporter> reporters = appContextHolder.getApplicationContext().getBeansOfType(TestReporter.class);
        for (TestReporter reporter : reporters.values()) {
            reporter.clearTestResults();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getSourceCode(String testPackage, String testName, String type) {
        Resource testFile = new PathMatchingResourcePatternResolver().getResource(testPackage.replaceAll("\\.", "/") + "/" + testName + "." + type);
        
        try {
            return FileUtils.readToString(testFile);
        } catch (IOException e) {
            return "Failed to load test case file: " + e.getMessage();
        }
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
     * Class type filter searches for subclasses of {@link AbstractTestNGCitrusTest}
     */
    private static final class CitrusTestTypeFilter extends AbstractClassTestingTypeFilter {
        @Override
        protected boolean match(ClassMetadata metadata) {
            return !metadata.getClassName().equals(TestNGCitrusTestBuilder.class.getName()) && 
                    metadata.getSuperClassName().equals(AbstractTestNGCitrusTest.class.getName());
        }
    }
}
