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

package com.consol.citrus.admin.service;

import com.consol.citrus.TestCase;
import com.consol.citrus.admin.converter.TestcaseModelConverter;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.TestCaseDetail;
import com.consol.citrus.admin.model.TestCaseType;
import com.consol.citrus.admin.spring.model.SpringBeans;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.model.testcase.core.Testcase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Abstract test case service provides common implementations for filesystem and classpath service.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractTestCaseService implements TestCaseService {

    @Autowired
    @Qualifier("jaxbMarshaller")
    private Unmarshaller unmarshaller;

    /**
     * Gets test case details such as status, description, author.
     * @return
     */
    public TestCaseDetail getTestDetail(String packageName, String testName, TestCaseType type) {
        TestCaseDetail testCase = new TestCaseDetail();
        testCase.setName(testName);
        testCase.setPackageName(packageName);
        testCase.setType(type);

        Testcase testModel;
        if (type.equals(TestCaseType.XML)) {
            testModel = getXmlTestModel(packageName, testName);
        } else if (type.equals(TestCaseType.JAVA)) {
            testModel = getJavaTestModel(packageName, testName);
        } else {
            throw new CitrusAdminRuntimeException("Unsupported test case type: " + type);
        }

        testCase.setDetail(testModel);

        return testCase;
    }

    /**
     * Get test case model from Java source code.
     * @param packageName
     * @param testName
     * @return
     */
    private Testcase getJavaTestModel(String packageName, String testName) {
        try {
            Class<?> testBuilderClass = Class.forName(packageName + "." + testName);

            TestNGCitrusTestBuilder builder = (TestNGCitrusTestBuilder) testBuilderClass.getConstructor(new Class[]{}).newInstance();

            for (Method method : ReflectionUtils.getAllDeclaredMethods(testBuilderClass)) {
                if (method.getAnnotation(CitrusTest.class) != null) {
                    CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);

                    builder.init();
                    ReflectionUtils.invokeMethod(method, builder);

                    TestCase testCase = builder.getTestCase(null);
                    return new TestcaseModelConverter().convert(testCase);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source as it is not part of classpath: " + packageName + "." + testName, e);
        } catch (Exception e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source " + packageName + "." + testName, e);
        }

        Testcase testModel = new Testcase();
        testModel.setName(testName);
        return testModel;
    }

    /**
     * Get test case model from XML source code.
     * @param packageName
     * @param testName
     * @return
     */
    private Testcase getXmlTestModel(String packageName, String testName) {
        String xmlSource = getSourceCode(packageName, testName, TestCaseType.XML);

        if (!StringUtils.hasText(xmlSource)) {
            throw new CitrusAdminRuntimeException("Failed to get XML source code for test: " + packageName + "." + testName);
        }

        try {
            return ((SpringBeans) unmarshaller.unmarshal(new StringSource(xmlSource))).getTestcase();
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("Failed to unmarshal test case from Spring XML bean definition", e);
        }
    }
}
