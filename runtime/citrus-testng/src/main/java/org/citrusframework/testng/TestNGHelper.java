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

package org.citrusframework.testng;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.api.common.TestSourceAware;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.common.ResolvedTestSource;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.common.TestSourceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestNGHelper {

    public static final String BUILDER_ATTRIBUTE = "builder";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TestNGHelper.class);

    /**
     * Prevent instantiation of utility class
     */
    private TestNGHelper() {
        // prevent instantiation
    }

    /**
     * Invokes test method.
     * @param target
     * @param testResult
     * @param method
     * @param testLoader
     * @param context
     * @param invocationCount
     */
    public static void invokeTestMethod(Object target, ITestResult testResult, Method method,
                                        TestLoader testLoader, TestContext context, int invocationCount) {

        try {
            Object[] params = TestNGParameterHelper.resolveParameter(target, testResult, method,
                context, invocationCount);
            testLoader.configureTestCase(
                t -> TestNGParameterHelper.injectTestParameters(method, t, params));
            testLoader.doWithTestCase(t -> ReflectionHelper.invokeMethod(method, target, params));
            testLoader.load();
        } catch (CitrusRuntimeException e) {
            SkipException skipException = getCauseOfType(e, SkipException.class);
            if (skipException != null)  {
                throw skipException;
            }

            throw e;
        }
    }

    /**
     * Recursively checks if the cause of the given exception matches the target exception.
     *
     * @param exception The exception to check.
     * @param targetCause The exception cause to search for.
     * @return true if the target cause is found in the exception chain, false otherwise.
     */
    public static <T extends Throwable> T getCauseOfType(Throwable exception, Class<T> targetCause) {
        if (exception == null) {
            return null;
        }

        if (targetCause.isInstance(exception)) {
            return targetCause.cast(exception);
        }

        return getCauseOfType(exception.getCause(), targetCause);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param method
     * @param context
     * @return
     */
    public static TestCaseRunner createTestCaseRunner(Object target, Method method, TestContext context) {
        TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(target.getClass());
        testCaseRunner.name(target.getClass().getSimpleName());
        testCaseRunner.packageName(target.getClass().getPackage().getName());

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

    /**
     * Creates test loader from @CitrusTestSource(type = TestLoader.SPRING) annotated test method and saves those to local member.
     * Test loaders get executed later when actual method is called by TestNG. This way user can annotate
     * multiple methods in one single class each executing several Citrus XML tests.
     *
     * @param method
     * @param provider
     * @return
     */
    public static List<TestLoader> createMethodTestLoaders(Method method, TestLoaderProvider provider) {
        List<TestLoader> methodTestLoaders = new ArrayList<>();

        if (method.getAnnotation(CitrusTestSource.class) != null) {
            CitrusTestSource citrusTestAnnotation = method.getAnnotation(CitrusTestSource.class);
            for (ResolvedTestSource resolvedTestSource : TestSourceResolver.resolve(citrusTestAnnotation, method)) {
                TestLoader testLoader = provider.createTestLoader(resolvedTestSource.getTestName(),
                        resolvedTestSource.getPackageName(), citrusTestAnnotation.type());

                if (resolvedTestSource.getSource() != null) {
                    if (testLoader instanceof TestSourceAware testSourceAware) {
                        testSourceAware.setSource(TestSourceHelper.create(resolvedTestSource.getSource()));
                        methodTestLoaders.add(testLoader);
                    } else {
                        logger.warn("Test loader {} is not able to handle test source {}", testLoader.getClass(), resolvedTestSource.getSource());
                    }
                } else {
                    methodTestLoaders.add(testLoader);
                }
            }
        }

        return methodTestLoaders;
    }

    @FunctionalInterface
    public interface TestLoaderProvider {
        /**
         * Creates new test loader which has TestNG test annotations set for test execution. Only
         * suitable for tests that get created at runtime through factory method. Subclasses
         * may overwrite this in order to provide custom test loader with custom test annotations set.
         * @param testName
         * @param packageName
         * @param type
         * @return
         */
        TestLoader createTestLoader(String testName, String packageName, String type);
    }

    /**
     * Class faking test execution as callback. Used in run hookable method when test case
     * was executed before and callback is needed for super class run method invocation.
     */
    public static final class FakeExecutionCallBack implements IHookCallBack {
        private Object[] parameters;

        public FakeExecutionCallBack(Object[] parameters) {
            this.parameters = Arrays.copyOf(parameters, parameters.length);
        }

        @Override
        public void runTestMethod(ITestResult testResult) {
            // do nothing as test case was already executed
        }

        @Override
        public Object[] getParameters() {
            return Arrays.copyOf(parameters, parameters.length);
        }

    }
}
