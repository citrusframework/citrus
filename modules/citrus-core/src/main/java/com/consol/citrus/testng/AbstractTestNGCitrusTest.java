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

import com.consol.citrus.TestCase;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.*;
import org.springframework.util.Assert;
import org.testng.*;
import org.testng.annotations.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract base test implementation for testng test cases. Providing test listener support and
 * loading basic application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(locations = { "classpath:com/consol/citrus/spring/root-application-ctx.xml", 
                                    "classpath:citrus-context.xml", 
                                    "classpath:com/consol/citrus/functions/citrus-function-ctx.xml",
                                    "classpath:com/consol/citrus/validation/citrus-validationmatcher-ctx.xml"})
@Listeners( { CitrusMethodInterceptor.class } )
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {
    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TestSuiteListeners testSuiteListener;
    
    @Autowired
    private TestContextFactoryBean testContextFactory;
    
    @Autowired(required = false)
    private SequenceBeforeSuite beforeSuite;
    
    @Autowired(required = false)
    private SequenceAfterSuite afterSuite;
    
    /** Parameter values provided from external logic */
    private Object[][] citrusDataProviderParameters;

    /** Collection of test runners for annotated methods */
    private Map<String, List<TestRunner>> testRunners = new HashMap<String, List<TestRunner>>();

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            List<TestRunner> methodTestRunners = testRunners.get(method.getName());

            if (!CollectionUtils.isEmpty(methodTestRunners)) {
                try {
                    TestRunner testRunner = methodTestRunners.get(testResult.getMethod().getCurrentInvocationCount() % methodTestRunners.size());

                    if (citrusDataProviderParameters != null) {
                        handleTestParameters(testResult.getMethod(), testRunner.getTestCase(), testRunner.getTestContext(), citrusDataProviderParameters[testResult.getMethod().getCurrentInvocationCount() % citrusDataProviderParameters.length]);
                    }
                    testRunner.run();
                } catch (RuntimeException e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                } catch (Exception e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                }
            }

            super.run(new FakeExecutionCallBack(callBack.getParameters()), testResult);
        } else {
            super.run(callBack, testResult);
        }
    }

    /**
     * Creates test runners from @CitrusXmlTest annotated test methods and saves those to local member.
     * Test runners get executed later when actual method is called by TestNG. This way user can annotate
     * multiple methods in one single class each executing several Citrus XML tests.
     */
    @BeforeClass(alwaysRun = true)
    public void createTestRunners() {
        for (Method method : ReflectionUtils.getAllDeclaredMethods(this.getClass())) {
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

                List<TestRunner> methodTestRunners = new ArrayList<TestRunner>();
                testRunners.put(method.getName(), methodTestRunners);

                for (String testName : testNames) {
                    TestContext testContext = prepareTestContext(createTestContext());

                    methodTestRunners.add(createTestRunner(testName, testPackage, testContext));
                }

                String[] testPackages = citrusTestAnnotation.packageScan();
                for (String packageName : testPackages) {
                    try {
                        Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageName.replace('.', '/') + "/**/*Test.xml");

                        for (Resource fileResource : fileResources) {
                            TestContext testContext = prepareTestContext(createTestContext());

                            String filePath = fileResource.getFile().getParentFile().getCanonicalPath();
                            filePath = filePath.substring(filePath.indexOf(packageName.replace('.', '/')));

                            methodTestRunners.add(createTestRunner(fileResource.getFilename().substring(0, fileResource.getFilename().length() - ".xml".length()), filePath, testContext));
                        }
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageName + "'", e);
                    }
                }
            }
        }
    }

    /**
     * Creates new test runner which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test runner with custom test annotations set.
     * @param beanName
     * @param packageName
     * @param testContext
     * @return
     */
    protected TestRunner createTestRunner(String beanName, String packageName, TestContext testContext) {
        return new CitrusXmlTestRunner(beanName, packageName, applicationContext, testContext);
    }

    /**
     * Runs tasks before test suite.
     * @param testContext the test context.
     * @throws Exception on error.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        /*
         * Fix for problem with Spring's TestNG support.
         * In order to have access to applicationContext in BeforeSuite annotated methods.
         * Fixed with version 3.1.RC1
         */
        springTestContextPrepareTestInstance();

        Assert.notNull(applicationContext);

        if (beforeSuite != null) {
            try {
                beforeSuite.execute(createTestContext());
            } catch (Exception e) {
                org.testng.Assert.fail("Before suite failed with errors", e);
            }
        } else {
            testSuiteListener.onStart();
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Executes the test case.
     */
    protected void executeTest() {
        executeTest(null);
    }

    /**
     * Executes the test case.
     * @param testContext the test context.
     */
    protected void executeTest(ITestContext testContext) {
        TestContext ctx = prepareTestContext(createTestContext());

        TestCase testCase = getTestCase(ctx);
        if (citrusDataProviderParameters != null) {
            handleTestParameters(Reporter.getCurrentTestResult().getMethod(), testCase, ctx,
                    citrusDataProviderParameters[Reporter.getCurrentTestResult().getMethod().getCurrentInvocationCount()]);
        }

        testCase.execute(ctx);
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     *
     * @param method the testng method currently executed
     * @param testCase the constructed Citrus test.
     * @param ctx the Citrus test context.
     */
    private void handleTestParameters(ITestNGMethod method, TestCase testCase, TestContext ctx, Object[] parameterValues) {
        String[] parameterNames;

        CitrusParameters citrusParameters = method.getConstructorOrMethod().getMethod().getAnnotation(CitrusParameters.class);
        Parameters testNgParameters = method.getConstructorOrMethod().getMethod().getAnnotation(Parameters.class);
        if (citrusParameters != null) {
            parameterNames = citrusParameters.value();
        } else if (testNgParameters != null) {
            parameterNames = testNgParameters.value();
        } else {
            throw new CitrusRuntimeException("Missing parameters annotation, " +
                    "please provide parameter names with proper annotation when using data provider!");
        }

        if (parameterValues.length != parameterNames.length) {
            throw new CitrusRuntimeException("Parameter mismatch: " + parameterNames.length +
                    " parameter names defined with " + parameterValues.length + " parameter values available");
        }

        for (int i = 0; i < parameterValues.length; i++) {
            ctx.setVariable(parameterNames[i], parameterValues[i]);
        }

        testCase.setParameters(convertParameterValues(parameterValues));
    }

    /**
     * Converts object parameter array to string array for test case.
     * @param parameterValues
     * @return
     */
    protected String[] convertParameterValues(Object[] parameterValues) {
        String[] parameters = new String[parameterValues.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameters[i] = "'" + parameterValues[i] + "'";
        }

        return parameters;
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
     * Creates a new test context.
     * @return the new citrus test context.
     */
    protected TestContext createTestContext() {
        return (TestContext) testContextFactory.getObject();
    }

    /**
     * Gets the test case to execute.
     * @param context
     * @return
     */
    protected TestCase getTestCase(TestContext context) {
        // by default use this class name and package
        return getTestCase(context, this.getClass().getPackage().getName(), this.getClass().getSimpleName());
    }

    /**
     * Gets the test case from application context.
     * @param context
     * @param packageName
     * @param testName
     * @return the new test case.
     */
    protected TestCase getTestCase(TestContext context, String packageName, String testName) {
        ClassPathXmlApplicationContext ctx = createApplicationContext(context, packageName, testName);
        TestCase testCase = null;
        
        try {
            testCase = (TestCase) ctx.getBean(testName, TestCase.class);
            testCase.setPackageName(packageName);
        } catch (NoSuchBeanDefinitionException e) {
            throw context.handleError(testName, packageName, "Could not find test with name '" + testName + "'", e);
        }
        
        return testCase;
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext(TestContext context, String packageName, String testName) {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[] {
                            packageName.replace('.', '/') + "/" + testName + ".xml",
                            "com/consol/citrus/spring/internal-helper-ctx.xml"},
                    true, applicationContext);
        } catch (Exception e) {
            throw context.handleError(getClass().getSimpleName(), getClass().getPackage().getName(), "Failed to load test case", e);
        }
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        if (afterSuite != null) {
            try {
                afterSuite.execute(createTestContext());
            } catch (Exception e) {
                org.testng.Assert.fail("After suite failed with errors", e);
            }
        } else {
            testSuiteListener.onFinish();
            testSuiteListener.onFinishSuccess();
        }
    }
    
    /**
     * Default data provider automatically adding parameters 
     * as variables to test case.
     * @return
     */
    @DataProvider(name = "citrusDataProvider")
    protected Object[][] provideTestParameters() {
      citrusDataProviderParameters = getParameterValues();
      return citrusDataProviderParameters;
    }
    
    /**
     * Hook for subclasses to provide individual test parameters.
     * @return
     */
    protected Object[][] getParameterValues() {
        return new Object[][] { {} };
    }

    /**
     * Gets this classes test runners.
     * @return
     */
    public Map<String, List<TestRunner>> getTestRunners() {
        return testRunners;
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
