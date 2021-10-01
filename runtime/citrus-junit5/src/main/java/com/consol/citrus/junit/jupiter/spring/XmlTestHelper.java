/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.junit.jupiter.spring;

import java.io.File;
import java.lang.reflect.Method;

import com.consol.citrus.TestCase;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.junit.jupiter.CitrusExtension;
import com.consol.citrus.junit.jupiter.CitrusExtensionHelper;
import com.consol.citrus.util.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class XmlTestHelper {

    /**
     * Private constructor prevents instantiation of utility class.
     */
    private XmlTestHelper() {
        // prevent instantiation
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param extensionContext
     * @return
     */
    public static TestLoader createTestLoader(ExtensionContext extensionContext) {
        Method method = extensionContext.getRequiredTestMethod();
        String testName = extensionContext.getRequiredTestClass().getSimpleName();
        String packageName = method.getDeclaringClass().getPackage().getName();
        String source = "";

        if (method.getAnnotation(CitrusXmlTest.class) != null) {
            CitrusXmlTest citrusXmlTestAnnotation = method.getAnnotation(CitrusXmlTest.class);
            String[] packagesToScan = citrusXmlTestAnnotation.packageScan();

            if (StringUtils.hasText(citrusXmlTestAnnotation.packageName())) {
                packageName = citrusXmlTestAnnotation.packageName();
            }

            if (citrusXmlTestAnnotation.name().length > 0) {
                testName = citrusXmlTestAnnotation.name()[0];
            } else if (packagesToScan.length == 0 && citrusXmlTestAnnotation.sources().length == 0) {
                testName = method.getName();
            }

            if (citrusXmlTestAnnotation.sources().length > 0) {
                source = citrusXmlTestAnnotation.sources()[0];

                Resource file = FileUtils.getFileResource(source);
                testName = FileUtils.getBaseName(file.getFilename());
                packageName = source.substring(0, source.lastIndexOf(File.pathSeparator));
            }
        }

        XmlTestLoader testLoader = new XmlTestLoader(extensionContext.getRequiredTestClass(), testName,
                packageName, CitrusExtensionHelper.getCitrus(extensionContext).getCitrusContext());
        testLoader.setSource(source);

        return testLoader;
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    public static TestCase getXmlTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE)
                .getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName(),
                        key -> createTestLoader(extensionContext).load(), TestCase.class);
    }
}
