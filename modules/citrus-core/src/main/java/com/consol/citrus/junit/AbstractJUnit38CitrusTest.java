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

package com.consol.citrus.junit;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.consol.citrus.*;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestListeners;

/**
 * Abstract base test implementation for test cases that rather use JUnit testing framework. Class also provides 
 * test listener support and loads the root application context files for Citrus.
 * 
 * @author Christoph Deppisch
 */
@TestExecutionListeners({TestSuiteAwareExecutionListener.class})
@ContextConfiguration(locations = {"classpath:com/consol/citrus/spring/root-application-ctx.xml", 
                                   "classpath:citrus-context.xml", 
                                   "classpath:com/consol/citrus/functions/citrus-function-ctx.xml"})
public abstract class AbstractJUnit38CitrusTest extends AbstractJUnit38SpringContextTests {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractJUnit38CitrusTest.class);
    
    /** Test listeners */
    @Autowired
    private TestListeners testListener;
    
    @Autowired
    private TestContextFactoryBean testContextFactory;
    
    /**
     * Run tasks before each test case.
     */
    protected void setUp() {
        TestSuite suite= getTestSuite();
        
        suite.beforeTest();
    }
    
    /**
     * Execute the test case.
     */
    protected void executeTest() {
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
     * Get the test suite instance by its type from 
     * application context. If none is found default test suite
     * is used instead.
     * 
     * @return the test suite
     */
    private TestSuite getTestSuite() {
        TestSuite suite = null;
        
        Map<?, ?> suites = applicationContext.getBeansOfType(TestSuite.class);

        if(suites.keySet().size() > 1) {
            for (Entry<?, ?> entry : suites.entrySet()) {
                if(!entry.getKey().toString().equals(CitrusConstants.DEFAULT_SUITE_NAME)) {
                    suite = (TestSuite)entry.getValue();
                }
            }
        } else {
            log.warn("Could not find custom test suite - using default test suite");
            
            suite = (TestSuite)applicationContext.getBean(CitrusConstants.DEFAULT_SUITE_NAME, TestSuite.class);
        }
        
        return suite;
    }
}
