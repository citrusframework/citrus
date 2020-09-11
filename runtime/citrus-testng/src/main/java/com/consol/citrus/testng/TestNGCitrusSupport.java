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

package com.consol.citrus.testng;

import java.lang.reflect.Method;
import java.util.Date;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.testng.IHookCallBack;
import org.testng.ITestResult;

/**
 * Basic Citrus TestNG support base class automatically handles test case runner creation. Also provides method parameter resolution
 * and resource injection. Users can just extend this class and make use of the action runner methods provided in {@link com.consol.citrus.TestActionRunner}
 * and {@link GherkinTestActionRunner}.
 *
 * @author Christoph Deppisch
 * @since 3.0.0
 */
public class TestNGCitrusSupport extends AbstractTestNGCitrusTest implements GherkinTestActionRunner {

    private static final String BUILDER_ATTRIBUTE = "builder";

    /** Test builder delegate */
    private TestCaseRunner testCaseRunner;

    @Override
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusTest.class) != null) {
            try {
                run(testResult, method, null, testResult.getMethod().getCurrentInvocationCount());
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
            try {
                if (citrus == null) {
                    citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
                }

                TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

                TestCaseRunner testCaseBuilder = createTestCaseRunner(method, ctx);
                testCaseBuilder.groups(testResult.getMethod().getGroups());
                testResult.setAttribute(BUILDER_ATTRIBUTE, testCaseBuilder);

                CitrusAnnotations.injectAll(this, citrus, ctx);

                invokeTestMethod(testResult, method, testCaseBuilder, ctx, invocationCount);
            } finally {
                testResult.removeAttribute(BUILDER_ATTRIBUTE);
            }
        }
    }

    /**
     * Invokes test method.
     * @param testResult
     * @param method
     * @param testCaseBuilder
     * @param context
     * @param invocationCount
     */
    protected void invokeTestMethod(ITestResult testResult, Method method, TestCaseRunner testCaseBuilder, TestContext context, int invocationCount) {
        final TestCase testCase = testCaseBuilder.getTestCase();
        try {
            Object[] params = resolveParameter(testResult, method, testCase, context, invocationCount);
            testCaseBuilder.start();
            ReflectionUtils.invokeMethod(method, this, params);
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        } finally {
            testCaseBuilder.stop();
        }
    }

    @Override
    protected Object resolveAnnotatedResource(ITestResult testResult, Class<?> parameterType, TestContext context) {
        Object storedBuilder = testResult.getAttribute(BUILDER_ATTRIBUTE);
        if (TestCaseRunner.class.isAssignableFrom(parameterType)) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        }

        return super.resolveAnnotatedResource(testResult, parameterType, context);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param method
     * @param context
     * @return
     */
    protected TestCaseRunner createTestCaseRunner(Method method, TestContext context) {
        testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(this.getClass());
        testCaseRunner.name(this.getClass().getSimpleName());
        testCaseRunner.packageName(this.getClass().getPackage().getName());

        if (method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                testCaseRunner.name(citrusTestAnnotation.name());
            } else {
                testCaseRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
            }
        }  else {
            testCaseRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        }

        return testCaseRunner;
    }

    @Override
    public <T extends TestAction> T run(TestActionBuilder<T> builder) {
        return testCaseRunner.run(builder);
    }

    @Override
    public <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior) {
        return testCaseRunner.applyBehavior(behavior);
    }

    public <T> T variable(String name, T value) {
        return testCaseRunner.variable(name, value);
    }

    public void name(String name) {
        testCaseRunner.name(name);
    }

    public void description(String description) {
        testCaseRunner.description(description);
    }

    public void author(String author) {
        testCaseRunner.author(author);
    }

    public void status(TestCaseMetaInfo.Status status) {
        testCaseRunner.status(status);
    }

    public void creationDate(Date date) {
        testCaseRunner.creationDate(date);
    }
}
