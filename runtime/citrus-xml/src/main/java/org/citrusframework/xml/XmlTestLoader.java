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

package org.citrusframework.xml;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

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

    public static final String TEST_NS = "http://citrusframework.org/schema/xml/testcase";
    private static final Pattern NAMESPACE_IS_SET = Pattern.compile(".*<(\\w+:)?test .*xmlns(:\\w+)?=\\s*\".*>.*", Pattern.DOTALL);

    /**
     * Default constructor.
     */
    public XmlTestLoader() {
        try {
            jaxbContext = JAXBContext.newInstance("org.citrusframework.xml");
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to create XMLTestLoader instance", e);
        }
    }

    /**
     * Constructor with context file and parent application context field.
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
                                    .unmarshal(new StringSource(applyNamespace(FileUtils.readToString(xmlSource))), XmlTestCase.class)
                                    .getValue()
                                    .getTestCase();
            if (runner instanceof DefaultTestCaseRunner) {
                ((DefaultTestCaseRunner) runner).setTestCase(testCase);
            }

            testCase.getActionBuilders().stream()
                    .filter(action -> ReferenceResolverAware.class.isAssignableFrom(action.getClass()))
                    .map(ReferenceResolverAware.class::cast)
                    .forEach(action -> action.setReferenceResolver(context.getReferenceResolver()));

            configurer.forEach(handler -> handler.accept(testCase));
            citrus.run(testCase, context);
            handler.forEach(handler -> handler.accept(testCase));
        } catch (JAXBException | IOException e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load XML test with name '" + testName + "'", e);
        }
    }

    /**
     * Automatically applies Citrus test namespace if non is set on the root element.
     */
    public static String applyNamespace(String xmlSource) {
        if (NAMESPACE_IS_SET.matcher(xmlSource).matches()) {
            return xmlSource;
        }

        return xmlSource.replace("<test ", String.format("<test xmlns=\"%s\" ", TEST_NS));
    }

    /**
     * Gets custom Spring application context file for the XML test case. If not set creates default
     * context file path from testName and packageName.
     */
    public String getSource() {
        if (StringUtils.hasText(source)) {
            return source;
        } else {
            String path = packageName.replace('.', '/');
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_XML) ? testName : testName + FileUtils.FILE_EXTENSION_XML;
            return Resources.CLASSPATH_RESOURCE_PREFIX + path + "/" + fileName;
        }
    }

    /**
     * Sets custom Spring application context file for XML test case.
     */
    @Override
    public void setSource(String source) {
        this.source = source;
    }

    public XmlTestLoader source(String source) {
        setSource(source);
        return this;
    }
}
