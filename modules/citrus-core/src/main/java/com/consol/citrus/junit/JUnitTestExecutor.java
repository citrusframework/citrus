/*
 * Copyright 2006-2011 the original author or authors.
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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestListeners;

/**
 * @author Christoph Deppisch
 */
public class JUnitTestExecutor {
    
    /** Parent application context */
    private ApplicationContext applicationContext;
    
    /** Class to execute as JUnit test */
    private Class<?> testClass;

    /**
     * Default constructor using fields.
     */
    public JUnitTestExecutor(ApplicationContext applicationContext, Class<?> testClass) {
        this.applicationContext = applicationContext;
        this.testClass = testClass;
    }
    
    /**
     * @param beforeTest
     */
    public void beforeTest(SequenceBeforeTest beforeTest, TestContext context) {
        if (beforeTest != null) {
            try {
                beforeTest.execute(context);
            } catch (Exception e) {
                throw new CitrusRuntimeException("Before test failed with errors", e);
            }
        }
    }

    /**
     * @param context
     */
    public void executeTest(TestContext context) {
        getTestCase(context).execute(context);
    }
    
    /**
     * Gets the test case from application context.
     * @return the new test case.
     */
    protected TestCase getTestCase(TestContext context) {
        ClassPathXmlApplicationContext ctx = createApplicationContext(context);
        TestCase testCase = null;
        try {
            testCase = (TestCase) ctx.getBean(testClass.getSimpleName(), TestCase.class);
            testCase.setPackageName(testClass.getPackage().getName());
        } catch (NoSuchBeanDefinitionException e) {
            throw context.handleError(getClass().getSimpleName(), getClass().getPackage().getName(), "Could not find test with name '" + testClass.getSimpleName() + "'", e);
        }
        return testCase;
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext(TestContext context) {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[] {
                            testClass.getPackage().getName().replace('.', '/')
                                    + "/" + testClass.getSimpleName() + ".xml",
                                    "com/consol/citrus/spring/internal-helper-ctx.xml"},
                    true, applicationContext);
        } catch (Exception e) {
            throw context.handleError(getClass().getSimpleName(), getClass().getPackage().getName(), "Failed to load test case", e);
        }
    }

}
