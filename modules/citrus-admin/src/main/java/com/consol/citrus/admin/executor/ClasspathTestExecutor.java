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
import com.consol.citrus.admin.websocket.WebSocketLoggingAppender;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.testng.TestNG;
import org.testng.xml.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Executes a test case from direct classpath using same JVM in which this server web application is running.
 * @author Christoph Deppisch
 */
public class ClasspathTestExecutor implements TestExecutor<ClasspathRunConfiguration> {

    @Autowired
    private ApplicationContextHolder applicationContextHolder;
    
    @Autowired
    private WebSocketLoggingAppender webSocketLoggingAppender;

    @Override
    public void execute(String packageName, String testName, ClasspathRunConfiguration configuration) {
        String methodName = null;
        String testClassName;

        int methodSeparatorIndex = testName.indexOf('.');
        if (methodSeparatorIndex > 0) {
            methodName = testName.substring(methodSeparatorIndex + 1);
            testClassName = testName.substring(0, methodSeparatorIndex);
        } else {
            testClassName = testName;
        }

        try {
            Class<?> testClass = Class.forName(packageName + "." + testClassName);

            webSocketLoggingAppender.setProcessId(testClassName);

            if (!applicationContextHolder.isApplicationContextLoaded()) {
                applicationContextHolder.loadApplicationContext();
            }

            if (TestNGCitrusTestDesigner.class.isAssignableFrom(testClass) ||
                    TestNGCitrusTestRunner.class.isAssignableFrom(testClass) ||
                    AbstractTestNGCitrusTest.class.isAssignableFrom(testClass)) {
                runTest(testClassName, methodName, testClass);
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusAdminRuntimeException("Failed to execute test case as it is not part of classpath: " + packageName + "." + testClassName, e);
        } catch (Exception e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source " + packageName + "." + testClassName, e);
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
     * @param methodName
     * @param testClass
     */
    private void runTest(String testName, String methodName, Class<?> testClass) {
        TestNG testng = new TestNG(true);

        XmlSuite suite = new XmlSuite();
        suite.setName("citrus-test-suite");

        XmlTest test = new XmlTest(suite);
        test.setName(testName);

        XmlClass xmlClass = new XmlClass(testClass);
        if (StringUtils.hasText(methodName)) {
            xmlClass.getIncludedMethods().add(new XmlInclude(getMethodName(methodName, testClass)));
        }

        test.setXmlClasses(Collections.singletonList(xmlClass));

        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(suite);
        testng.setXmlSuites(suites);
        testng.run();

        if (testng.hasFailure()) {
            throw new CitrusAdminRuntimeException("Citrus test run failed!");
        }
    }

    /**
     * Finds test method name in test class - either method with name or other method with test annotation
     * named accordingly.
     * @param methodName
     * @param testClass
     * @return
     */
    private String getMethodName(String methodName, Class<?> testClass) {
        if (ReflectionUtils.findMethod(testClass, methodName) != null) {
            return methodName;
        } else {
            for (Method method : ReflectionUtils.getAllDeclaredMethods(testClass)) {
                CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
                if (citrusTestAnnotation != null
                        && StringUtils.hasText(citrusTestAnnotation.name())
                        && citrusTestAnnotation.name().equals(methodName)) {
                    return method.getName();
                }
            }
        }

        throw new CitrusAdminRuntimeException("Could not find method with name or Citrus annotation name: " + methodName);
    }
}
