/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.junit.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.junit.CitrusFrameworkMethod;
import com.consol.citrus.junit.TestSuiteExecutionListener;
import com.consol.citrus.util.FileUtils;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

/**
 * JUnit runner reads Citrus test annotation for XML test cases and prepares test execution within proper Citrus
 * test context boundaries. Supports package scan as well as multiple test method annotations within one single class.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusSpringJUnit4Runner extends SpringJUnit4ClassRunner {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusSpringJUnit4Runner.class);

    /**
     * Default constructor using class to run.
     * @param clazz the test class to be run
     */
    public CitrusSpringJUnit4Runner(Class<?> clazz) throws InitializationError {
        super(clazz);
        getTestContextManager().registerTestExecutionListeners(new TestSuiteExecutionListener());
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object testInstance) {
        return new InvokeRunMethod(frameworkMethod, testInstance);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> methods = super.getChildren();
        List<FrameworkMethod> interceptedMethods = new ArrayList<>();

        for (FrameworkMethod method : methods) {
            if (method.getMethod().getAnnotation(CitrusXmlTest.class) != null) {
                CitrusXmlTest citrusXmlTestAnnotation = method.getMethod().getAnnotation(CitrusXmlTest.class);
                String[] packagesToScan = citrusXmlTestAnnotation.packageScan();

                String packageName = method.getMethod().getDeclaringClass().getPackage().getName();
                if (StringUtils.hasText(citrusXmlTestAnnotation.packageName())) {
                    packageName = citrusXmlTestAnnotation.packageName();
                }

                if (citrusXmlTestAnnotation.name().length > 0) {
                    for (String name : citrusXmlTestAnnotation.name()) {
                        interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), name, packageName));
                    }
                } else if (packagesToScan.length == 0 && citrusXmlTestAnnotation.sources().length == 0) {
                    interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), method.getName(), packageName));
                }

                for (String source : citrusXmlTestAnnotation.sources()) {
                    Resource file = FileUtils.getFileResource(source);
                    CitrusFrameworkMethod frameworkMethod = new CitrusFrameworkMethod(method.getMethod(), FileUtils.getBaseName(file.getFilename()),
                            source.substring(0, source.lastIndexOf(File.pathSeparator)));
                    frameworkMethod.setSource(source);
                    interceptedMethods.add(frameworkMethod);
                }

                for (String packageScan : packagesToScan) {
                    try {
                        for (String fileNamePattern : CitrusSettings.getXmlTestFileNamePattern()) {
                            Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageScan.replace('.', File.separatorChar) + fileNamePattern);
                            for (Resource fileResource : fileResources) {
                                String filePath = fileResource.getFile().getParentFile().getCanonicalPath();

                                if (packageScan.startsWith("file:")) {
                                    filePath = "file:" + filePath;
                                }

                                filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                                interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(),
                                        FileUtils.getBaseName(fileResource.getFilename()),
                                        filePath));
                            }
                        }
                    } catch (RuntimeException | IOException e) {
                        LOG.error("Unable to locate file resources for test package '" + packageScan + "'", e);
                    }
                }
            } else if (method.getMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = method.getMethod().getAnnotation(CitrusTest.class);

                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), citrusTestAnnotation.name(),
                            method.getMethod().getDeclaringClass().getPackage().getName()));
                } else {
                    interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), method.getDeclaringClass().getSimpleName() + "." + method.getName(),
                            method.getMethod().getDeclaringClass().getPackage().getName()));
                }
            } else {
                interceptedMethods.add(method);
            }
        }

        return interceptedMethods;
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Test.class);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoid(false, errors);
        }
    }

    /**
     * Special invoke method statement. Checks on {@link CitrusTest} or {@link CitrusXmlTest} annotation present and invokes
     * run method on abstract Citrus JUnit4 test class.
     */
    private static class InvokeRunMethod extends InvokeMethod {
        private final FrameworkMethod frameworkMethod;
        private final Object testInstance;

        /**
         * Constructor using framework method and test instance as object.
         * @param frameworkMethod
         * @param testInstance
         */
        public InvokeRunMethod(FrameworkMethod frameworkMethod, Object testInstance) {
            super(frameworkMethod, testInstance);

            this.frameworkMethod = frameworkMethod;
            this.testInstance = testInstance;
        }

        @Override
        public void evaluate() throws Throwable {
            if (CitrusFrameworkMethod.Runner.class.isAssignableFrom(testInstance.getClass()) &&
                    frameworkMethod instanceof CitrusFrameworkMethod) {
                ((CitrusFrameworkMethod.Runner)testInstance).run((CitrusFrameworkMethod) frameworkMethod);
            } else {
                super.evaluate();
            }
        }
    }
}
