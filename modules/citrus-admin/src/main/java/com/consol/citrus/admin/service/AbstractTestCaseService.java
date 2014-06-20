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
import com.consol.citrus.admin.executor.ApplicationContextHolder;
import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.spring.model.SpringBeans;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.model.testcase.core.Testcase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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

    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    /**
     * Gets test case details such as status, description, author.
     * @return
     */
    public TestCaseDetail getTestDetail(Project project, String packageName, String testName, TestCaseType type) {
        TestCaseDetail testCase = new TestCaseDetail();
        testCase.setName(testName);
        testCase.setPackageName(packageName);
        testCase.setType(type);

        Testcase testModel;
        if (type.equals(TestCaseType.XML)) {
            testModel = getXmlTestModel(project, packageName, testName);
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
        String methodName = null;
        String testClassName;

        int methodSeparatorIndex = testName.indexOf('.');
        if (methodSeparatorIndex > 0) {
            methodName = testName.substring(methodSeparatorIndex + 1);
            testClassName = testName.substring(0, methodSeparatorIndex);
        } else {
            testClassName = testName;
        }

        try {
            Class<?> testBuilderClass = Class.forName(packageName + "." + testClassName);

            if (!applicationContextHolder.isApplicationContextLoaded()) {
                applicationContextHolder.loadApplicationContext();
            }

            TestNGCitrusTestBuilder builder = (TestNGCitrusTestBuilder) testBuilderClass.getConstructor(new Class[]{}).newInstance();
            AutowireCapableBeanFactory beanFactory = applicationContextHolder.getApplicationContext().getAutowireCapableBeanFactory();
            beanFactory.autowireBeanProperties(builder, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
            beanFactory.initializeBean(builder, testBuilderClass.getName());

            for (Method method : ReflectionUtils.getAllDeclaredMethods(testBuilderClass)) {
                CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
                if (citrusTestAnnotation != null) {
                    if (StringUtils.hasText(methodName)) {
                        if (StringUtils.hasText(citrusTestAnnotation.name())) {
                            if (!citrusTestAnnotation.name().equals(methodName)) {
                                continue;
                            }
                        } else if (!method.getName().equals(methodName)) {
                           continue;
                        }
                    }

                    Testcase model = getJavaDslTest(builder, method);
                    model.setName(StringUtils.hasText(methodName) ? methodName : testClassName);
                    return model;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source as it is not part of classpath: " + packageName + "." + testClassName, e);
        } catch (Exception e) {
            throw new CitrusAdminRuntimeException("Failed to load Java source " + packageName + "." + testClassName, e);
        }

        Testcase testModel = new Testcase();
        testModel.setName(StringUtils.hasText(methodName) ? methodName : testClassName);
        return testModel;
    }

    private Testcase getJavaDslTest(TestNGCitrusTestBuilder builder, Method method) {
        builder.init();
        ReflectionUtils.invokeMethod(method, builder);

        TestCase testCase = builder.getTestCase(null);
        return new TestcaseModelConverter().convert(testCase);
    }

    /**
     * Get test case model from XML source code.
     * @param packageName
     * @param testName
     * @return
     */
    private Testcase getXmlTestModel(Project project, String packageName, String testName) {
        String xmlSource = getSourceCode(project, packageName, testName, TestCaseType.XML);

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
