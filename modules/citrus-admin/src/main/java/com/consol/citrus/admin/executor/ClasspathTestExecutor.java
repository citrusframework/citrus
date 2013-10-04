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

import com.consol.citrus.admin.configuration.ClasspathRunConfiguration;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.service.ConfigurationService;
import com.consol.citrus.admin.websocket.WebSocketLoggingAppender;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.TestNG;
import org.testng.xml.*;

import java.util.*;

/**
 * Executes a test case from direct classpath using same JVM in which this server web application is running.
 * @author Christoph Deppisch
 */
public class ClasspathTestExecutor implements TestExecutor<ClasspathRunConfiguration> {

    @Autowired
    private ApplicationContextHolder applicationContextHolder;
    
    @Autowired
    private ConfigurationService configService;

    @Autowired
    private WebSocketLoggingAppender webSocketLoggingAppender;

    @Override
    public void execute(String packageName, String testName, ClasspathRunConfiguration configuration) {
        try {
            Class<?> testClass = Class.forName(packageName + "." + testName);

            webSocketLoggingAppender.setProcessId(testName);

            if (!applicationContextHolder.isApplicationContextLoaded()) {
                applicationContextHolder.loadApplicationContext();
            }

            if (TestNGCitrusTestBuilder.class.isAssignableFrom(testClass)) {
                runTestBuilder(testName, testClass);
            } else if (AbstractTestNGCitrusTest.class.isAssignableFrom(testClass)) {
                runTest(testName, testClass);
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusAdminRuntimeException("Failed to execute test case as it is not part of classpath: " + packageName + "." + testName, e);
        } catch (Exception e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source " + packageName + "." + testName, e);
        } finally {
            Map<String, TestReporter> reporters = applicationContextHolder.getApplicationContext().getBeansOfType(TestReporter.class);
            for (TestReporter reporter : reporters.values()) {
                reporter.clearTestResults();
            }

            webSocketLoggingAppender.setProcessId(null);
        }
    }

    /**
     * Instantiates and runs Citrus test class.
     * @param testName
     * @param testClass
     */
    private void runTest(String testName, Class<?> testClass) {
        TestNG testng = new TestNG(true);

        XmlSuite suite = new XmlSuite();
        suite.setName("citrus-test-suite");

        XmlTest test = new XmlTest(suite);
        test.setName(testName);
        test.setXmlClasses(Collections.singletonList(new XmlClass(testClass)));

        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(suite);
        testng.setXmlSuites(suites);
        testng.run();

        if (testng.hasFailure()) {
            throw new CitrusAdminRuntimeException("Citrus test run failed!");
        }
    }

    /**
     * Instantiates and runs Citrus Java DSL test builder class.
     * @param testName
     * @param testClass
     */
    private void runTestBuilder(String testName, Class<?> testClass) {
        runTest(testName, testClass);
    }

}
