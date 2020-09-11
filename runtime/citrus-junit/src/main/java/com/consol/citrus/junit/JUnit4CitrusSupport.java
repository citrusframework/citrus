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

package com.consol.citrus.junit;

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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JUnit4CitrusSupport extends AbstractJUnit4CitrusTest implements GherkinTestActionRunner {

    private static final String BUILDER_ATTRIBUTE = "builder";

    /** Test builder delegate */
    private TestCaseRunner testCaseRunner;

    @Override
    protected void run(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod) {
        if (citrus == null) {
            citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
        }

        TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

        TestCaseRunner testCaseBuilder = createTestRunner(frameworkMethod, ctx);
        frameworkMethod.setAttribute(BUILDER_ATTRIBUTE, testCaseBuilder);
        CitrusAnnotations.injectAll(this, citrus, ctx);

        invokeTestMethod(frameworkMethod, testCaseBuilder, ctx);
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param frameworkMethod
     * @param testCaseBuilder
     * @param context
     */
    protected void invokeTestMethod(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestCaseRunner testCaseBuilder, TestContext context) {
        final TestCase testCase = testCaseBuilder.getTestCase();
        try {
            Object[] params = resolveParameter(frameworkMethod, testCase, context);
            testCaseBuilder.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this, params);
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        } finally {
            testCaseBuilder.stop();
        }
    }

    @Override
    protected Object resolveAnnotatedResource(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, Class<?> parameterType, TestContext context) {
        Object storedBuilder = frameworkMethod.getAttribute(BUILDER_ATTRIBUTE);
        if (TestCaseRunner.class.isAssignableFrom(parameterType)) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        }

        return super.resolveAnnotatedResource(frameworkMethod, parameterType, context);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param context
     * @return
     */
    protected TestCaseRunner createTestRunner(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestContext context) {
        testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(this.getClass());
        testCaseRunner.name(frameworkMethod.getTestName());
        testCaseRunner.packageName(frameworkMethod.getPackageName());

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
