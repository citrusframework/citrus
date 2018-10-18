/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.*;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.util.*;
import org.testng.*;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Abstract base test implementation for testng test cases. Providing test listener support and
 * loading basic application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Listeners( { PrepareTestNGMethodInterceptor.class } )
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Citrus instance */
    protected Citrus citrus;

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            List<TestLoader> methodTestLoaders = createTestLoadersForMethod(method);

            if (!CollectionUtils.isEmpty(methodTestLoaders)) {
                try {
                    run(testResult, method, methodTestLoaders.get(testResult.getMethod().getCurrentInvocationCount() % methodTestLoaders.size()),
                            testResult.getMethod().getCurrentInvocationCount());
                } catch (Exception e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                }
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

    /**
     * Run method prepares and executes test case.
     * @param testResult
     * @param method
     * @param testLoader
     * @param invocationCount
     */
    protected void run(ITestResult testResult, Method method, TestLoader testLoader, int invocationCount) {
        if (citrus == null) {
            citrus = Citrus.newInstance(applicationContext);
        }

        TestContext ctx = prepareTestContext(citrus.createTestContext());
        TestCase testCase = testLoader.load();
        testCase.setGroups(testResult.getMethod().getGroups());

        invokeTestMethod(testResult, method, testCase, ctx, invocationCount);
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param testResult
     * @param method
     * @param testCase
     * @param context
     * @param invocationCount
     */
    protected void invokeTestMethod(ITestResult testResult, Method method, TestCase testCase, TestContext context, int invocationCount) {
        try {
            ReflectionUtils.invokeMethod(method, this,
                    resolveParameter(testResult, method, testCase, context, invocationCount));
        } catch (TestCaseFailedException e) {
            throw e;
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            testCase.finish(context);
            throw new TestCaseFailedException(e);
        }

        citrus.run(testCase, context);
    }

    /**
     * Resolves method arguments supporting TestNG data provider parameters as well as
     * {@link CitrusResource} annotated methods.
     *
     * @param testResult
     * @param method
     * @param testCase
     * @param context
     * @param invocationCount
     * @return
     */
    protected Object[] resolveParameter(ITestResult testResult, final Method method, TestCase testCase, TestContext context, int invocationCount) {
        Object[] dataProviderParams = null;
        if (method.getAnnotation(Test.class) != null &&
                StringUtils.hasText(method.getAnnotation(Test.class).dataProvider())) {
            final Method[] dataProvider = new Method[1];
            ReflectionUtils.doWithMethods(method.getDeclaringClass(), current -> {
                if (StringUtils.hasText(current.getAnnotation(DataProvider.class).name()) &&
                        current.getAnnotation(DataProvider.class).name().equals(method.getAnnotation(Test.class).dataProvider())) {
                    dataProvider[0] = current;
                } else if (current.getName().equals(method.getAnnotation(Test.class).dataProvider())) {
                    dataProvider[0] = current;
                }

            }, toFilter -> toFilter.getAnnotation(DataProvider.class) != null);

            if (dataProvider[0] == null) {
                throw new CitrusRuntimeException("Unable to find data provider: " + method.getAnnotation(Test.class).dataProvider());
            }

            Object[][] parameters = (Object[][]) ReflectionUtils.invokeMethod(dataProvider[0], this,
                    resolveParameter(testResult, dataProvider[0], testCase, context, -1));
            if (parameters != null) {
                dataProviderParams = parameters[invocationCount % parameters.length];
                injectTestParameters(method, testCase, dataProviderParams);
            }
        }

        Object[] values = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
            Class<?> parameterType = parameterTypes[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof CitrusResource) {
                    values[i] = resolveAnnotatedResource(testResult, parameterType, context);
                }
            }

            if (parameterType.equals(ITestResult.class)) {
                values[i] = testResult;
            } else if (parameterType.equals(ITestContext.class)) {
                values[i] = testResult.getTestContext();
            } else if (values[i] == null && dataProviderParams != null && i < dataProviderParams.length) {
                values[i] = dataProviderParams[i];
            }
        }

        return values;
    }

    /**
     * Resolves value for annotated method parameter.
     *
     * @param testResult
     * @param parameterType
     * @return
     */
    protected Object resolveAnnotatedResource(ITestResult testResult, Class<?> parameterType, TestContext context) {
        if (TestContext.class.isAssignableFrom(parameterType)) {
            return context;
        } else {
            throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + parameterType);
        }
    }

    /**
     * Creates test loader from @CitrusXmlTest annotated test method and saves those to local member.
     * Test loaders get executed later when actual method is called by TestNG. This way user can annotate
     * multiple methods in one single class each executing several Citrus XML tests.
     *
     * @param method
     * @return
     */
    private List<TestLoader> createTestLoadersForMethod(Method method) {
        List<TestLoader> methodTestLoaders = new ArrayList<TestLoader>();

        if (method.getAnnotation(CitrusXmlTest.class) != null) {
            CitrusXmlTest citrusTestAnnotation = method.getAnnotation(CitrusXmlTest.class);

            String[] testNames = new String[] {};
            if (citrusTestAnnotation.name().length > 0) {
                testNames = citrusTestAnnotation.name();
            } else if (citrusTestAnnotation.packageScan().length == 0) {
                // only use default method name as test in case no package scan is set
                testNames = new String[] { method.getName() };
            }

            String testPackage;
            if (StringUtils.hasText(citrusTestAnnotation.packageName())) {
                testPackage = citrusTestAnnotation.packageName();
            } else {
                testPackage = method.getDeclaringClass().getPackage().getName();
            }

            for (String testName : testNames) {
                methodTestLoaders.add(createTestLoader(testName, testPackage));
            }

            String[] packagesToScan = citrusTestAnnotation.packageScan();
            for (String packageScan : packagesToScan) {
                try {
                    for (String fileNamePattern : Citrus.getXmlTestFileNamePattern()) {
                        Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageScan.replace('.', File.separatorChar) + fileNamePattern);
                        for (Resource fileResource : fileResources) {
                            String filePath = fileResource.getFile().getParentFile().getCanonicalPath();

                            if (packageScan.startsWith("file:")) {
                                filePath = "file:" + filePath;
                            }
                            
                            filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                            methodTestLoaders.add(createTestLoader(fileResource.getFilename().substring(0, fileResource.getFilename().length() - ".xml".length()), filePath));
                        }
                    }
                } catch (RuntimeException | IOException e) {
                    throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
                }
            }
        }

        return methodTestLoaders;
    }

    /**
     * Runs tasks before test suite.
     * @param testContext the test context.
     * @throws Exception on error.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        springTestContextPrepareTestInstance();
        Assert.notNull(applicationContext);

        citrus = Citrus.newInstance(applicationContext);
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        if (citrus != null) {
            citrus.afterSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
        }
    }

    /**
     * Executes the test case.
     * @deprecated in favor of using {@link com.consol.citrus.annotations.CitrusXmlTest} or {@link com.consol.citrus.annotations.CitrusTest} annotations on test methods.
     */
    @Deprecated
    protected void executeTest() {
        if (citrus == null) {
            citrus = Citrus.newInstance(applicationContext);
        }

        ITestResult result = Reporter.getCurrentTestResult();
        ITestNGMethod testNGMethod = result.getMethod();

        TestContext context = prepareTestContext(citrus.createTestContext());
        TestLoader testLoader = createTestLoader(this.getClass().getSimpleName(), this.getClass().getPackage().getName());
        TestCase testCase = testLoader.load();
        testCase.setGroups(testNGMethod.getGroups());

        resolveParameter(result, testNGMethod.getConstructorOrMethod().getMethod(), testCase, context, testNGMethod.getCurrentInvocationCount());

        citrus.run(testCase, context);
    }

    /**
     * Prepares the test context.
     *
     * Provides a hook for test context modifications before the test gets executed.
     *
     * @param testContext the test context.
     * @return the (prepared) test context.
     */
    protected TestContext prepareTestContext(final TestContext testContext) {
        return testContext;
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param testName
     * @param packageName
     * @return
     */
    protected TestLoader createTestLoader(String testName, String packageName) {
        return new XmlTestLoader(getClass(), testName, packageName, applicationContext);
    }

    /**
     * Constructs the test case to execute.
     * @return
     */
    protected TestCase getTestCase() {
        return createTestLoader(this.getClass().getSimpleName(), this.getClass().getPackage().getName()).load();
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     *
     * @param method the method currently executed
     * @param testCase the constructed Citrus test.
     */
    protected void injectTestParameters(Method method, TestCase testCase, Object[] parameterValues) {
        testCase.setParameters(getParameterNames(method), parameterValues);
    }

    /**
     * Read parameter names form method annotation.
     * @param method
     * @return
     */
    protected String[] getParameterNames(Method method) {
        String[] parameterNames;
        CitrusParameters citrusParameters = method.getAnnotation(CitrusParameters.class);
        Parameters testNgParameters = method.getAnnotation(Parameters.class);
        if (citrusParameters != null) {
            parameterNames = citrusParameters.value();
        } else if (testNgParameters != null) {
            parameterNames = testNgParameters.value();
        } else {
            List<String> methodParameterNames = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                methodParameterNames.add(parameter.getName());
            }
            parameterNames = methodParameterNames.toArray(new String[methodParameterNames.size()]);
        }

        return parameterNames;
    }

    /**
     * Class faking test execution as callback. Used in run hookable method when test case
     * was executed before and callback is needed for super class run method invocation.
     */
    protected static final class FakeExecutionCallBack implements IHookCallBack {
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
