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

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;

/**
 * Abstract base testng test for Citrus bean definition parser unit testing. Provides access to
 * an application context holding bean definitions parsed from Citrus bean definition parsers.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractBeanDefinitionParserBaseTest<T extends TestAction> extends AbstractBaseTest {

    /** Application context holding bean definitions parsed */
    protected ApplicationContext beanDefinitionContext;
    
    /** Navigate index for list of actions */
    private AtomicInteger actionIndex = new AtomicInteger(0);
    
    /**
     * Creates the application context with bean definitions parsed. By default searches
     * for SimpleClassName-context.xml application context file in test package and 
     * builds child application context.
     * 
     * @return
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextPrepareTestInstance")
    protected void parseBeanDefinitions() {
        beanDefinitionContext = createApplicationContext("context");
    }
    
    /**
     * Creates a new application context with specified child application context. Child application context
     * is named SimpleClassName-[suffix].xml and is located in test class package.
     * @param string
     * @return
     */
    protected ApplicationContext createApplicationContext(String suffix) {
        return new ClassPathXmlApplicationContext(
                new String[] {
                        this.getClass().getPackage().getName().replace('.', '/')
                                + "/" + getClass().getSimpleName() + "-" + suffix + ".xml"},
                true, applicationContext);
    }

    /**
     * Gets the actual test case object from Spring application context.
     * @return
     */
    protected TestCase getTestCase() {
        return beanDefinitionContext.getBean(getClass().getSimpleName(), TestCase.class);
    }
    
    /**
     * Gets the next test action in list of action in test case.
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T getNextTestActionFromTest() {
        return (T)getTestCase().getActions().get(actionIndex.getAndIncrement());
    }
    
    /**
     * Checks for test action to meet expected class and name.
     * @param actionClass the action class.
     * @param actionName the action name.
     */
    protected void assertActionClassAndName(Class<T> actionClass, String actionName) {
        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), actionClass);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), actionName);
    }
    
    /**
     * Asserts the action count in test case.
     * @param count the number of expected test actions in test case.
     */
    protected void assertActionCount(int count) {
        Assert.assertEquals(getTestCase().getActions().size(), count);
    }
}
