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

package org.citrusframework.testng.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citrusframework.CitrusSettings;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.internal.MethodInstance;

/**
 * TestNG method interceptor duplicates method instances for each package scan test and test name in
 * Citrus annotated test method. So TestNG test is executed multiple times respectively for each Citrus test
 * constructed by method annotation.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestNGCitrusSpringMethodInterceptor implements IMethodInterceptor {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TestNGCitrusSpringMethodInterceptor.class);

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> interceptedMethods = new ArrayList<>();

        for (IMethodInstance method : methods) {
            boolean baseMethodAdded = false;
            if (method.getInstance() instanceof TestNGCitrusSpringSupport) {
                if (method.getMethod().getConstructorOrMethod().getMethod().getAnnotation(CitrusTestSource.class) != null) {
                    CitrusTestSource citrusTestAnnotation = method.getMethod().getConstructorOrMethod().getMethod().getAnnotation(CitrusTestSource.class);
                    if (citrusTestAnnotation.name().length > 1) {
                        for (int i = 0; i < citrusTestAnnotation.name().length; i++) {
                            if (i == 0) {
                                baseMethodAdded = true;
                                interceptedMethods.add(method);
                            } else {
                                interceptedMethods.add(new MethodInstance(method.getMethod()));
                            }
                        }
                    }

                    String[] packagesToScan = citrusTestAnnotation.packageScan();
                    for (String packageName : packagesToScan) {
                        try {
                            for (String fileNamePattern : CitrusSettings.getTestFileNamePattern(citrusTestAnnotation.type())) {
                                Set<Path> fileResources = new ClasspathResourceResolver().getResources(packageName.replace('.', File.separatorChar), fileNamePattern);
                                for (int i = 0; i < fileResources.size(); i++) {
                                    if (i == 0 && !baseMethodAdded) {
                                        baseMethodAdded = true;
                                        interceptedMethods.add(method);
                                    } else {
                                        interceptedMethods.add(new MethodInstance(method.getMethod()));
                                    }
                                }
                            }
                        } catch (IOException e) {
                            logger.error("Unable to locate file resources for test package '" + packageName + "'", e);
                        }
                    }
                }
            }

            if (!baseMethodAdded) {
                interceptedMethods.add(method);
            }
        }

        return interceptedMethods;
    }
}
