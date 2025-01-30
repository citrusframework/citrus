/*
 * Copyright the original author or authors.
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

package org.citrusframework.junit.jupiter;

import java.lang.reflect.Method;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import static org.citrusframework.annotations.CitrusAnnotations.injectCitrusFramework;
import static org.citrusframework.junit.jupiter.CitrusExtensionHelper.*;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 * <p>
 * Extension resolves method parameter of type {@link org.citrusframework.context.TestContext}, {@link TestCaseRunner}
 * or {@link org.citrusframework.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 * <p>
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 */
public class CitrusExtension implements BeforeAllCallback, InvocationInterceptor, AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor, TestExecutionExceptionHandler, AfterEachCallback {

    /**
     * Test suite name
     */
    private static final String SUITE_NAME = "citrus-junit5-suite";

    private static boolean beforeSuite = true;
    private static boolean afterSuite = true;

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CitrusExtension.class);

    private static void failTestCaseIfNotDoneYet(ExtensionContext extensionContext, Throwable throwable) {
        var testCase = getTestCase(extensionContext);
        var testResult = testCase.getTestResult();

        if (testResult == null || !testResult.isFailed()) {
            testCase.fail(throwable);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (requiresCitrus(extensionContext)) {
            setCitrus(CitrusInstanceManager.getOrDefault(), extensionContext);
        }

        if (beforeSuite) {
            beforeSuite = false;

            // Assertion: If the beforeAll callback is called for a test, the annotated tags are currently
            // included by the groups filter of surefire / failsafe or no specific filter is defined and
            // all groups / tags will run anyway.

            //initialize "after all test run hook"
            String[] tags = extensionContext.getTags().toArray(String[]::new);
            String suiteName = extensionContext.getTestClass()
                    .map(Class::getName)
                    .orElseGet(() -> extensionContext.getTestMethod()
                            .map(meth -> meth.getDeclaringClass().getName() + ":" + meth.getName())
                            .orElse(SUITE_NAME));

            extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("afterSuiteCallback", new AfterSuiteCallback(extensionContext, suiteName, tags));

            getCitrus(extensionContext).beforeSuite(suiteName, tags);
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        failTestCaseIfNotDoneYet(extensionContext, throwable);
        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        extensionContext.getExecutionException()
                .ifPresent(e -> failTestCaseIfNotDoneYet(extensionContext, e));

        extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestContext.class.getSimpleName());
        extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestCase.class.getSimpleName());

        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof TestListener testListener) {
            testListener.after(getCitrus(extensionContext).getCitrusContext());
        }
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Object testInstance = extensionContext.getRequiredTestInstance();
        Citrus citrus = getCitrus(extensionContext);
        TestContext context = getTestContext(extensionContext);

        CitrusAnnotations.injectAll(testInstance, citrus, context);

        if (testInstance instanceof TestListener testListener) {
            testListener.before(citrus.getCitrusContext());
        }

        return invocation.proceed();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        Citrus citrus = getCitrus(extensionContext);
        TestContext context = getTestContext(extensionContext);

        TestCaseRunner testRunner = getTestRunner(extensionContext);
        CitrusAnnotations.injectAll(testInstance, citrus, context);
        CitrusAnnotations.injectTestRunner(testInstance, testRunner);

        if (testInstance instanceof TestListener testListener) {
            testListener.before(citrus.getCitrusContext());
        }

        TestLoader testLoader = getTestLoader(extensionContext);
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, testRunner);

        testLoader.doWithTestCase(testCase -> {
            try {
                invocation.proceed();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new CitrusRuntimeException("Test failed", e);
            }
        });

        testLoader.load();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE)
                .remove(getBaseKey(extensionContext) + TestCaseRunner.class.getSimpleName());
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        injectCitrusFramework(testInstance, getCitrus(extensionContext));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return CitrusExtensionHelper.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Listener able to perform changes on Citrus context before/after a test.
     */
    public interface TestListener {

        /**
         * Runs tasks on given Citrus context before test.
         */
        default void before(CitrusContext context) {
        }

        /**
         * Runs tasks on given Citrus context after test.
         */
        default void after(CitrusContext context) {
        }
    }

    private static class AfterSuiteCallback implements ExtensionContext.Store.CloseableResource {

        private final ExtensionContext extensionContext;
        private final String suiteName;
        private final String[] tags;

        public AfterSuiteCallback(ExtensionContext extensionContext, String suiteName, String... tags) {
            this.extensionContext = extensionContext;
            this.suiteName = suiteName;
            this.tags = tags;
        }

        @Override
        public void close() throws Throwable {
            if (afterSuite) {
                afterSuite = false;
                getCitrus(extensionContext).afterSuite(suiteName, tags);
            }
        }
    }
}
