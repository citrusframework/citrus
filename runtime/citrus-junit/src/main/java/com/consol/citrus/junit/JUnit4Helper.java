/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.junit;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 */
public final class JUnit4Helper {

    public static final String BUILDER_ATTRIBUTE = "builder";

    /**
     * Prevent instantiation of utility class
     */
    private JUnit4Helper() {
        // prevent instantiation
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param target
     * @param frameworkMethod
     * @param testCaseBuilder
     * @param context
     */
    public static void invokeTestMethod(Object target, CitrusFrameworkMethod frameworkMethod, TestCaseRunner testCaseBuilder, TestContext context) {
        final TestCase testCase = testCaseBuilder.getTestCase();
        try {
            Object[] params = JUnit4ParameterHelper.resolveParameter(frameworkMethod, testCase, context);
            testCaseBuilder.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), target, params);
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        } finally {
            testCaseBuilder.stop();
        }
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param testClass
     * @param context
     * @return
     */
    public static TestCaseRunner createTestRunner(CitrusFrameworkMethod frameworkMethod, Class<?> testClass, TestContext context) {
        TestCaseRunner testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(testClass);
        testCaseRunner.name(frameworkMethod.getTestName());
        testCaseRunner.packageName(frameworkMethod.getPackageName());

        return testCaseRunner;
    }
}
