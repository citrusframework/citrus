/*
 * Copyright 2006-2013 the original author or authors.
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
import com.consol.citrus.context.TestContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Test runner loads test case as Spring bean from application context. Runner holds application context file
 * for test case and a parent application context. At runtime runner loads Spring application context and gets
 * test case bean instance from context.
 *
 * @author Christoph Deppisch
 */
public class CitrusXmlTestRunner extends AbstractTestRunner {

    private TestCase testCase;
    private String beanName;
    private String packageName;
    private ApplicationContext parentContext;
    private String contextFile;

    /**
     * Default constructor with context file and parent application context field.
     * @param beanName
     * @param packageName
     * @param parentContext
     * @param testContext
     */
    public CitrusXmlTestRunner(String beanName, String packageName, ApplicationContext parentContext, TestContext testContext) {
        super(testContext);
        this.beanName = beanName;
        this.packageName = packageName;
        this.parentContext = parentContext;
    }

    @Override
    public TestCase getTestCase() {
        if (testCase == null) {
            loadTestCase();
        }

        return testCase;
    }

    private void loadTestCase() {
        ApplicationContext ctx = loadApplicationContext();

        try {
            testCase = ctx.getBean(beanName, TestCase.class);
            testCase.setPackageName(packageName);
        } catch (NoSuchBeanDefinitionException e) {
            throw getTestContext().handleError(beanName, packageName, "Could not find test with name '" + beanName + "'", e);
        }
    }

    private ApplicationContext loadApplicationContext() {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[]{
                            getContextFile(),
                            "com/consol/citrus/spring/internal-helper-ctx.xml"},
                    true, parentContext);
        } catch (Exception e) {
            throw getTestContext().handleError(beanName, packageName, "Failed to load test case", e);
        }
    }

    /**
     * Gets custom Spring application context file for the XML test case. If not set creates default
     * context file path from beanName and packageName.
     * @return
     */
    public String getContextFile() {
        if (StringUtils.hasText(contextFile)) {
            return contextFile;
        } else {
            return packageName.replace('.', '/') + "/" + beanName + ".xml";
        }
    }

    /**
     * Sets custom Spring application context file for XML test case.
     * @param contextFile
     */
    public void setContextFile(String contextFile) {
        this.contextFile = contextFile;
    }
}
