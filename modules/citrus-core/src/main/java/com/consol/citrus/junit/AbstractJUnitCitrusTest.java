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

package com.consol.citrus.junit;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.consol.citrus.*;
import com.consol.citrus.TestCaseMetaInfo.Status;
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
public abstract class AbstractJUnitCitrusTest extends AbstractJUnit4SpringContextTests {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractJUnitCitrusTest.class);
    
    /** Test listeners */
    @Autowired
    private TestListeners testListener;
    
    /**
     * Run tasks before each test case.
     */
    @Before
    public void beforeTest() {
        TestSuite suite= getTestSuite();
        
        suite.beforeTest();
    }
    
    /**
     * Execute the test case.
     */
    protected void executeTest() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {
                        this.getClass().getPackage().getName()
                                .replace('.', '/')
                                + "/"
                                + this.getClass().getSimpleName()
                                + ".xml",
                        "com/consol/citrus/spring/internal-helper-ctx.xml" },
                true, applicationContext);
        
        TestCase testCase = null;
        try {
            testCase = (TestCase)ctx.getBean(this.getClass().getSimpleName(), TestCase.class);
        } catch (NoSuchBeanDefinitionException e) {
            org.testng.Assert.fail("Could not find test with name '" + this.getClass().getSimpleName() + "'", e);
        }
        
        if(!testCase.getMetaInfo().getStatus().equals(Status.DISABLED)) {
            testListener.onTestStart(testCase);
            
            try {
                testCase.execute();
                testCase.finish();
                
                testListener.onTestSuccess(testCase);
            } catch (Exception e) {
                testListener.onTestFailure(testCase, e);
                
                throw new TestCaseFailedException(e);
            } finally {
                testListener.onTestFinish(testCase);
            }
        } else {
            testListener.onTestSkipped(testCase);
        }
    }
    
    /**
     * Get the test suite instance by its type from 
     * application context. If none is found default test suite
     * is used instead.
     * 
     * @return
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
