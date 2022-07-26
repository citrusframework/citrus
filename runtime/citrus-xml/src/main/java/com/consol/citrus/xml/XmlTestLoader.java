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

package com.consol.citrus.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.common.DefaultTestLoader;
import com.consol.citrus.common.TestSourceAware;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Loads test case as Spring bean from XML application context file. Loader holds application context file
 * for test case and a parent application context. At runtime this class loads the Spring application context and gets
 * test case bean instance from context.
 *
 * @author Christoph Deppisch
 */
public class XmlTestLoader extends DefaultTestLoader implements TestSourceAware {

    private String source;

    private final JAXBContext jaxbContext;

    /**
     * Default constructor.
     */
    public XmlTestLoader() {
        try {
            jaxbContext = JAXBContext.newInstance("com.consol.citrus.xml");
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to create XMLTestLoader instance", e);
        }
    }

    /**
     * Constructor with context file and parent application context field.
     * @param testClass
     * @param testName
     * @param packageName
     */
    public XmlTestLoader(Class<?> testClass, String testName, String packageName) {
        this();

        this.testClass = testClass;
        this.testName = testName;
        this.packageName = packageName;
    }

    @Override
    public void doLoad() {
        Resource xmlSource = FileUtils.getFileResource(getSource());

        try {
            testCase = jaxbContext.createUnmarshaller()
                                    .unmarshal(new StringSource(FileUtils.readToString(xmlSource)), XmlTestCase.class)
                                    .getValue()
                                    .getTestCase();
            if (runner instanceof DefaultTestCaseRunner) {
                ((DefaultTestCaseRunner) runner).setTestCase(testCase);
            }

            configurer.forEach(handler -> handler.accept(testCase));
            citrus.run(testCase, context);
            handler.forEach(handler -> handler.accept(testCase));
        } catch (JAXBException | IOException e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load XML test with name '" + testName + "'", e);
        }
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
            return ResourceUtils.CLASSPATH_URL_PREFIX + packageName.replace('.', File.separatorChar) +
                    File.separator + testName + FileUtils.FILE_EXTENSION_XML;
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
