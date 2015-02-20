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

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContextFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Loads test case as Spring bean from XML application context file. Loader holds application context file
 * for test case and a parent application context. At runtime this class loads the Spring application context and gets
 * test case bean instance from context.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public class XmlTestLoader implements TestLoader {

    private TestCase testCase;
    private String testName;
    private String packageName;
    private ApplicationContext parentContext;
    private String contextFile;

    /**
     * Default constructor with context file and parent application context field.
     * @param testName
     * @param packageName
     * @param parentContext
     */
    public XmlTestLoader(String testName, String packageName, ApplicationContext parentContext) {
        this.testName = testName;
        this.packageName = packageName;
        this.parentContext = parentContext;
    }

    @Override
    public TestCase load() {
        if (testCase == null) {
            ApplicationContext ctx = loadApplicationContext();

            try {
                testCase = ctx.getBean(testName, TestCase.class);
                testCase.setPackageName(packageName);
            } catch (NoSuchBeanDefinitionException e) {
                throw parentContext.getBean(TestContextFactory.class).getObject()
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
                            getContextFile(),
                            "com/consol/citrus/spring/annotation-config-ctx.xml"},
                    true, parentContext);
        } catch (Exception e) {
            throw parentContext.getBean(TestContextFactory.class).getObject()
                    .handleError(testName, packageName, "Failed to load test case", e);
        }
    }

    /**
     * Gets custom Spring application context file for the XML test case. If not set creates default
     * context file path from testName and packageName.
     * @return
     */
    public String getContextFile() {
        if (StringUtils.hasText(contextFile)) {
            return contextFile;
        } else {
            return packageName.replace('.', File.separatorChar) + File.separator + testName + ".xml";
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
