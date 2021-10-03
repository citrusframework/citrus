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

package com.consol.citrus.common;

import java.io.File;

import com.consol.citrus.CitrusContext;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestCase;
import com.consol.citrus.util.FileUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Loads test case as Spring bean from XML application context file. Loader holds application context file
 * for test case and a parent application context. At runtime this class loads the Spring application context and gets
 * test case bean instance from context.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public class XmlTestLoader implements TestLoader, TestSourceAware {

    private TestCase testCase;
    private final Class<?> testClass;
    private final String testName;
    private final String packageName;
    private final CitrusContext citrusContext;
    private String source;

    /**
     * Default constructor with context file and parent application context field.
     * @param testClass
     * @param testName
     * @param packageName
     * @param citrusContext
     */
    public XmlTestLoader(Class<?> testClass, String testName, String packageName, CitrusContext citrusContext) {
        this.testClass = testClass;
        this.testName = testName;
        this.packageName = packageName;
        this.citrusContext = citrusContext;
    }

    @Override
    public TestCase load() {
        if (testCase == null) {
            ApplicationContext ctx = loadApplicationContext();

            try {
                testCase = ctx.getBean(testName, TestCase.class);

                if (testCase instanceof DefaultTestCase) {
                    testCase.setTestClass(testClass);
                    testCase.setPackageName(packageName);
                }
            } catch (NoSuchBeanDefinitionException e) {
                throw citrusContext.getTestContextFactory().getObject()
                        .handleError(testName, packageName, "Could not find test with name '" + testName + "'", e);
            }
        }

        return testCase;
    }

    /**
     * Create new Spring bean application context with test case XML file,
     * helper and parent context file.
     * @return
     */
    private ApplicationContext loadApplicationContext() {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[]{
                            getSource(),
                            "com/consol/citrus/spring/annotation-config-ctx.xml"},
                    true, getParentApplicationContext());
        } catch (Exception e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load test case", e);
        }
    }

    private ApplicationContext getParentApplicationContext() {
        if (citrusContext instanceof CitrusSpringContext) {
            return ((CitrusSpringContext) citrusContext).getApplicationContext();
        }

        return null;
    }

    /**
     * Gets custom Spring application context file for the XML test case. If not set creates default
     * context file path from testName and packageName.
     * @return
     */
    public String getSource() {
        if (StringUtils.hasText(source)) {
            return source;
        } else {
            return packageName.replace('.', File.separatorChar) + File.separator + testName + FileUtils.FILE_EXTENSION_XML;
        }
    }

    /**
     * Sets custom Spring application context file for XML test case.
     * @param source
     */
    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
