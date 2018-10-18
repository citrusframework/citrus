/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.dsl.testng;

import com.consol.citrus.*;
import com.consol.citrus.annotations.*;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.*;
import org.testng.IHookCallBack;
import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class TestNGCitrusTest extends AbstractTestNGCitrusTest {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DESIGNER_ATTRIBUTE = "designer";
    private static final String RUNNER_ATTRIBUTE = "runner";

    @Override
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusTest.class) != null) {
            try {
                run(testResult, method, null, testResult.getMethod().getCurrentInvocationCount());
            } catch (RuntimeException e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            super.run(new FakeExecutionCallBack(callBack.getParameters()), testResult);

            if (testResult.getThrowable() != null) {
                if (testResult.getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) testResult.getThrowable();
                } else {
                    throw new CitrusRuntimeException(testResult.getThrowable());
                }
            }
        } else {
            super.run(callBack, testResult);
        }
    }

    @Override
    protected void run(ITestResult testResult, Method method, TestLoader testLoader, int invocationCount) {
        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            super.run(testResult, method, testLoader, invocationCount);
        } else {
            TestDesigner testDesigner = null;
            TestRunner testRunner = null;

            try {
                if (citrus == null) {
                    citrus = Citrus.newInstance(applicationContext);
                }

                TestContext ctx = prepareTestContext(citrus.createTestContext());

                if (isDesignerMethod(method)) {
                    testDesigner = createTestDesigner(method, ctx);
                } else if (isRunnerMethod(method)) {
                    testRunner = createTestRunner(method, ctx);
                } else {
                    throw new CitrusRuntimeException("Missing designer or runner method parameter");
                }

                testResult.setAttribute(DESIGNER_ATTRIBUTE, testDesigner);
                testResult.setAttribute(RUNNER_ATTRIBUTE, testRunner);

                TestCase testCase = testDesigner != null ? testDesigner.getTestCase() : testRunner.getTestCase();
                testCase.setGroups(testResult.getMethod().getGroups());

                CitrusAnnotations.injectAll(this, citrus, ctx);

                invokeTestMethod(testResult, method, testCase, ctx, invocationCount);
            } finally {
                testResult.removeAttribute(DESIGNER_ATTRIBUTE);
                testResult.removeAttribute(RUNNER_ATTRIBUTE);
            }
        }
    }

    @Override
    protected void invokeTestMethod(ITestResult testResult, Method method, TestCase testCase, TestContext context, int invocationCount) {
        if (testResult.getAttribute(DESIGNER_ATTRIBUTE) != null) {
            super.invokeTestMethod(testResult, method, testCase, context, invocationCount);
        } else if (testResult.getAttribute(RUNNER_ATTRIBUTE) != null) {
            TestRunner testRunner = (TestRunner) testResult.getAttribute(RUNNER_ATTRIBUTE);

            try {
                Object[] params = resolveParameter(testResult, method, testCase, context, invocationCount);
                testRunner.start();
                ReflectionUtils.invokeMethod(method, this, params);
            } catch (Exception | AssertionError e) {
                testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                throw new TestCaseFailedException(e);
            } finally {
                testRunner.stop();
            }
        }
    }

    @Override
    protected Object resolveAnnotatedResource(ITestResult testResult, Class<?> parameterType, TestContext context) {
        if (TestDesigner.class.isAssignableFrom(parameterType)) {
            return testResult.getAttribute(DESIGNER_ATTRIBUTE);
        } else if (TestRunner.class.isAssignableFrom(parameterType)) {
            return testResult.getAttribute(RUNNER_ATTRIBUTE);
        }

        return super.resolveAnnotatedResource(testResult, parameterType, context);
    }

    /**
     * Creates new test designer instance for this test method.
     * @param method
     * @param context
     * @return
     */
    protected TestDesigner createTestDesigner(Method method, TestContext context) {
        TestDesigner testDesigner = new DefaultTestDesigner(applicationContext, context);
        testDesigner.testClass(getClass());
        testDesigner.packageName(this.getClass().getPackage().getName());

        if (method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                testDesigner.name(citrusTestAnnotation.name());
            } else {
                testDesigner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
            }
        } else {
            testDesigner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        }

        return testDesigner;
    }

    /**
     * Creates new test runner instance for this test method.
     * @param method
     * @param context
     * @return
     */
    protected TestRunner createTestRunner(Method method, TestContext context) {
        TestRunner testRunner = new DefaultTestRunner(applicationContext, context);
        testRunner.testClass(getClass());
        testRunner.packageName(this.getClass().getPackage().getName());

        if (method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                testRunner.name(citrusTestAnnotation.name());
            } else {
                testRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
            }
        }  else {
            testRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        }

        return testRunner;
    }

    /**
     * Searches for method parameter of type test designer.
     * @param method
     * @return
     */
    protected boolean isDesignerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isAssignableFrom(TestDesigner.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Searches for method parameter of type test runner.
     * @param method
     * @return
     */
    protected boolean isRunnerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isAssignableFrom(TestRunner.class)) {
                return true;
            }
        }

        return false;
    }

}
