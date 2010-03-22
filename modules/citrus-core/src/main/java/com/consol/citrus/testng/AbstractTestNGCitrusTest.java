/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 *
 * This file is part of Citrus.
 *
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
        TestCase testCase = createTestCase();

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
     * Creates the new test case.
     *
     * @return the new test case.
     */
    protected TestCase createTestCase() {
        ClassPathXmlApplicationContext ctx = createApplicationContextForTestCase();
        TestCase testCase = null;
        try {
            testCase = (TestCase) ctx.getBean(this.getClass().getSimpleName(), TestCase.class);
            testCase.setPackageName(this.getClass().getPackage().getName());
        } catch (NoSuchBeanDefinitionException e) {
            org.testng.Assert.fail("Could not find test with name '" + this.getClass().getSimpleName() + "'", e);
        }
        return testCase;
    }

    protected ClassPathXmlApplicationContext createApplicationContextForTestCase() {
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
