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

package com.consol.citrus.junit;

import com.consol.citrus.annotations.CitrusXmlTest;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusJUnit4Runner extends SpringJUnit4ClassRunner {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusJUnit4Runner.class);

    /**
     * Default constructor using class to run.
     * @param clazz the test class to be run
     */
    public CitrusJUnit4Runner(Class<?> clazz) throws InitializationError {
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
            interceptedMethods.add(method);

            if (getTestClass().getJavaClass().getClass().isInstance(AbstractJUnit4CitrusTest.class)) {
                CitrusXmlTest citrusXmlTestAnnotation = method.getMethod().getAnnotation(CitrusXmlTest.class);
                if (citrusXmlTestAnnotation != null) {
                    if (citrusXmlTestAnnotation.name().length > 1) {
                        for (int i = 1; i < citrusXmlTestAnnotation.name().length; i++) {
                            interceptedMethods.add(new FrameworkMethod(method.getMethod()));
                        }
                    }

                    String[] packagesToScan = citrusXmlTestAnnotation.packageScan();
                    for (String packageName : packagesToScan) {
                        try {
                            Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageName.replace('.', '/') + "/**/*Test.xml");
                            for (int i = 1; i < fileResources.length; i++) {
                                interceptedMethods.add(new FrameworkMethod(method.getMethod()));
                            }
                        } catch (IOException e) {
                            log.error("Unable to locate file resources for test package '" + packageName + "'", e);
                        }
                    }
                }
            }
        }

        return interceptedMethods;
    }

    /**
     * Special invoke method statement. Checks on {@link CitrusXmlTest} annotation present and invokes
     * run method on abstract Citrus JUnit4 test class.
     */
    private static class InvokeRunMethod extends InvokeMethod {
        private final FrameworkMethod frameworkMethod;
        private final Object testInstance;

        public InvokeRunMethod(FrameworkMethod frameworkMethod, Object testInstance) {
            super(frameworkMethod, testInstance);

            this.frameworkMethod = frameworkMethod;
            this.testInstance = testInstance;
        }

        @Override
        public void evaluate() throws Throwable {
            if (AbstractJUnit4CitrusTest.class.isAssignableFrom(testInstance.getClass()) && frameworkMethod.getAnnotation(CitrusXmlTest.class) != null) {
                ((AbstractJUnit4CitrusTest)testInstance).run(frameworkMethod);
            } else {
                super.evaluate();
            }
        }
    }
}
