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

package org.citrusframework.common;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.citrusframework.CitrusSpringContext;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
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
public class SpringXmlTestLoader extends DefaultTestLoader implements TestSourceAware {

    private String source;

    @Override
    protected void doLoad() {
        ApplicationContext ctx = loadApplicationContext();

        try {
            testCase = ctx.getBean(testName, TestCase.class);
            if (runner instanceof DefaultTestCaseRunner) {
                ((DefaultTestCaseRunner) runner).setTestCase(testCase);
            }

            configurer.forEach(handler -> handler.accept(testCase));
            citrus.run(testCase, context);
            handler.forEach(handler -> handler.accept(testCase));
        } catch (NoSuchBeanDefinitionException e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load Spring XML test with name '" + testName + "'", e);
        }
    }

    /**
     * Create new Spring bean application context with test case XML file,
     * helper and parent context file.
     */
    private ApplicationContext loadApplicationContext() {
        try {
            configureCustomParsers();

            return new ClassPathXmlApplicationContext(
                    new String[]{
                            getSource(),
                            "org/citrusframework/spring/annotation-config-ctx.xml"},
                    true, getParentApplicationContext());
        } catch (Exception e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load test case", e);
        }
    }

    /**
     * Configures the CitrusNamespaceParserRegistry with custom parsers
     */
    private void configureCustomParsers() {
        SpringXmlTestLoaderConfiguration loaderConfiguration = testClass.getAnnotation(SpringXmlTestLoaderConfiguration.class);
        if (loaderConfiguration != null) {
            for (BeanDefinitionParserConfiguration beanDefinitionParserConfiguration : loaderConfiguration.parserConfigurations()) {
                Class<? extends BeanDefinitionParser> parserClass = beanDefinitionParserConfiguration.parser();
                try {
                    if (parserClass != null) {
                        BeanDefinitionParser parserOverride = parserClass.getDeclaredConstructor().newInstance();
                        CitrusNamespaceParserRegistry.registerParser(beanDefinitionParserConfiguration.name(), parserOverride);
                    }
                } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new CitrusRuntimeException(String.format("Could not install custom BeanDefinitionParser '%s'", parserClass), e);
                }
            }
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
     */
    public String getSource() {
        if (StringUtils.hasText(source)) {
            return source;
        } else {
            return packageName.replace('.', File.separatorChar) +
                    File.separator + testName + FileUtils.FILE_EXTENSION_XML;
        }
    }

    /**
     * Sets custom Spring application context file for XML test case.
     */
    @Override
    public void setSource(String source) {
        this.source = source;
    }

    public SpringXmlTestLoader source(String source) {
        setSource(source);
        return this;
    }
}
