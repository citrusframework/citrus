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

package com.consol.citrus.dsl.junit;

import java.lang.reflect.Method;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseBuilder;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.junit.CitrusFrameworkMethod;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 * @deprecated in favor of using {@link com.consol.citrus.junit.spring.JUnit4CitrusSpringSupport}
 */
@Deprecated
public class JUnit4CitrusTest extends AbstractJUnit4CitrusTest {

    private static final String DESIGNER_ATTRIBUTE = "designer";
    private static final String RUNNER_ATTRIBUTE = "runner";

    @Override
    public void run(CitrusFrameworkMethod frameworkMethod) {
        if (citrus == null) {
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        }

        TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

        TestCaseBuilder testBuilder;
        if (isDesignerMethod(frameworkMethod.getMethod())) {
            testBuilder = createTestDesigner(frameworkMethod, ctx);
        } else if (isRunnerMethod(frameworkMethod.getMethod())) {
            testBuilder = createTestRunner(frameworkMethod, ctx);
        } else {
            throw new CitrusRuntimeException("Missing designer or runner method parameter");
        }

        CitrusAnnotations.injectAll(this, citrus, ctx);

        invokeTestMethod(frameworkMethod, testBuilder.getTestCase(), ctx);
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param frameworkMethod
     * @param testCase
     * @param context
     */
    protected void invokeTestMethod(CitrusFrameworkMethod frameworkMethod, TestCase testCase, TestContext context) {
        if (frameworkMethod.getAttribute(DESIGNER_ATTRIBUTE) != null) {
            try {
                ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this,
                        resolveParameter(frameworkMethod, context));
            } catch (TestCaseFailedException e) {
                throw e;
            } catch (Exception | AssertionError e) {
                testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                testCase.finish(context);
                throw new TestCaseFailedException(e);
            }

            citrus.run(testCase, context);
        } else if (frameworkMethod.getAttribute(RUNNER_ATTRIBUTE) != null) {
            TestRunner testRunner = (TestRunner) frameworkMethod.getAttribute(RUNNER_ATTRIBUTE);

            try {
                Object[] params = resolveParameter(frameworkMethod, context);
                testRunner.start();
                ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this, params);
            } catch (Exception | AssertionError e) {
                testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                throw new TestCaseFailedException(e);
            } finally {
                testRunner.stop();
            }
        }
    }

    @Override
    protected Object resolveAnnotatedResource(CitrusFrameworkMethod frameworkMethod, Class<?> parameterType, TestContext context) {
        if (TestDesigner.class.isAssignableFrom(parameterType)) {
            return frameworkMethod.getAttribute(DESIGNER_ATTRIBUTE);
        } else if (TestRunner.class.isAssignableFrom(parameterType)) {
            return frameworkMethod.getAttribute(RUNNER_ATTRIBUTE);
        }

        return super.resolveAnnotatedResource(frameworkMethod, parameterType, context);
    }

    /**
     * Creates new test designer instance for this test method.
     * @param frameworkMethod
     * @param context
     * @return
     */
    protected TestDesigner createTestDesigner(CitrusFrameworkMethod frameworkMethod, TestContext context) {
        TestDesigner testDesigner = new DefaultTestDesigner(context);
        testDesigner.testClass(getClass());
        testDesigner.name(frameworkMethod.getTestName());
        testDesigner.packageName(frameworkMethod.getPackageName());

        frameworkMethod.setAttribute(DESIGNER_ATTRIBUTE, testDesigner);

        return testDesigner;
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param context
     * @return
     */
    protected TestRunner createTestRunner(CitrusFrameworkMethod frameworkMethod, TestContext context) {
        TestRunner testRunner = new DefaultTestRunner(context);
        testRunner.testClass(getClass());
        testRunner.name(frameworkMethod.getTestName());
        testRunner.packageName(frameworkMethod.getPackageName());

        frameworkMethod.setAttribute(RUNNER_ATTRIBUTE, testRunner);

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
