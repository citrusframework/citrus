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

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCase;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JUnit4CitrusTest extends AbstractJUnit4CitrusTest {
    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DESIGNER_ATTRIBUTE = "designer";
    private static final String RUNNER_ATTRIBUTE = "runner";

    @Override
    protected void run(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod) {
        TestDesigner testDesigner = null;
        TestRunner testRunner = null;

        try {
            if (citrus == null) {
                citrus = Citrus.newInstance(applicationContext);
            }

            TestContext ctx = prepareTestContext(citrus.createTestContext());

            if (isDesignerMethod(frameworkMethod.getMethod())) {
                testDesigner = createTestDesigner(frameworkMethod, ctx);
            } else if (isRunnerMethod(frameworkMethod.getMethod())) {
                testRunner = createTestRunner(frameworkMethod, ctx);
            } else {
                throw new CitrusRuntimeException("Missing designer or runner method parameter");
            }

            TestCase testCase = testDesigner != null ? testDesigner.getTestCase() : testRunner.getTestCase();

            CitrusAnnotations.injectAll(this, citrus, ctx);

            invokeTestMethod(frameworkMethod, testCase, ctx);
        } finally {
            if (testRunner != null) {
                testRunner.stop();
            }
        }
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param frameworkMethod
     * @param testCase
     * @param context
     */
    protected void invokeTestMethod(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestCase testCase, TestContext context) {
        if (frameworkMethod.getAttribute(DESIGNER_ATTRIBUTE) != null) {
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this,
                    resolveParameter(frameworkMethod, testCase, context));

            citrus.run(testCase, context);
        } else if (frameworkMethod.getAttribute(RUNNER_ATTRIBUTE) != null) {
            TestRunner testRunner = (TestRunner) frameworkMethod.getAttribute(RUNNER_ATTRIBUTE);

            Object[] params = resolveParameter(frameworkMethod, testCase, context);
            testRunner.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this, params);
        }
    }

    @Override
    protected Object resolveAnnotatedResource(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, Class<?> parameterType, TestContext context) {
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
    protected TestDesigner createTestDesigner(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestContext context) {
        TestDesigner testDesigner = new DefaultTestDesigner(applicationContext, context);
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
    protected TestRunner createTestRunner(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestContext context) {
        TestRunner testRunner = new DefaultTestRunner(applicationContext, context);
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
