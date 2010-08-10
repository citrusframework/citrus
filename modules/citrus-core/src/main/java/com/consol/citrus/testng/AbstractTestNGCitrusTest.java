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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import com.consol.citrus.*;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestListeners;

/**
 * Abstract base test implementation for testng test cases. Providing test listener support and
 * loading basic application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(locations = {"classpath:com/consol/citrus/spring/root-application-ctx.xml",
                                   "classpath:citrus-context.xml",
                                   "classpath:com/consol/citrus/functions/citrus-function-ctx.xml"})
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {
    /**
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Test listeners */
    @Autowired
    private TestListeners testListener;

    @Autowired
    private TestContextFactoryBean testContextFactory;

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

        TestSuite suite= getTestSuite(testContext.getSuite().getName());

        if(!suite.beforeSuite()) {
            org.testng.Assert.fail("Before suite failed with errors");
        }
    }

    /**
     * Runs tasks before tests.
     * @param testContext the test context.
     */
    @BeforeClass(dependsOnMethods = "springTestContextPrepareTestInstance")
    public void beforeTest(ITestContext testContext) {
        TestSuite suite = getTestSuite(testContext.getSuite().getName());
        suite.beforeTest();
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
        TestCase testCase = getTestCase();

        if(!testCase.getMetaInfo().getStatus().equals(Status.DISABLED)) {
            testListener.onTestStart(testCase);

            try {
                testCase.execute(prepareTestContext(createTestContext()));
                testListener.onTestSuccess(testCase);
            } catch (Exception e) {
                testListener.onTestFailure(testCase, e);

                throw new TestCaseFailedException(e);
            } finally {
                testListener.onTestFinish(testCase);
                testCase.finish();
            }
        } else {
            testListener.onTestSkipped(testCase);
        }
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
     * @throws Exception on error.
     */
    protected TestContext createTestContext() throws Exception {
        return (TestContext)testContextFactory.getObject();
    }

    /**
     * Gets the test case from application context.
     * @return the new test case.
     */
    protected TestCase getTestCase() {
        ClassPathXmlApplicationContext ctx = createApplicationContext();
        TestCase testCase = null;
        try {
            testCase = (TestCase) ctx.getBean(this.getClass().getSimpleName(), TestCase.class);
            testCase.setPackageName(this.getClass().getPackage().getName());
        } catch (NoSuchBeanDefinitionException e) {
            org.testng.Assert.fail("Could not find test with name '" + this.getClass().getSimpleName() + "'", e);
        }
        return testCase;
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(
                new String[] {
                        this.getClass().getPackage().getName()
                                .replace('.', '/')
                                + "/"
                                + getClass().getSimpleName()
                                + ".xml",
                                "com/consol/citrus/spring/internal-helper-ctx.xml"},
                true, applicationContext);
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        TestSuite suite= getTestSuite(testContext.getSuite().getName());

        if(!suite.afterSuite()) {
            org.testng.Assert.fail("After suite failed with errors");
        }
    }

    /**
     * Gets the test suite instance by its name from application context.
     * @param name the name.
     * @return the test suite.
     */
    private TestSuite getTestSuite(String name) {
        if(name.endsWith(" by packages")) {
            name = name.substring(0, name.length() - " by packages".length());
        }

        TestSuite suite;
        try {
            suite = (TestSuite)applicationContext.getBean(name, TestSuite.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("Could not find test suite with name '" + name + "' using default test suite");

            suite = (TestSuite)applicationContext.getBean(CitrusConstants.DEFAULT_SUITE_NAME, TestSuite.class);
        }

        return suite;
    }
}
