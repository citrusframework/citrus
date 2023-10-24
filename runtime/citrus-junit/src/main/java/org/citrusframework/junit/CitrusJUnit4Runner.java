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

package org.citrusframework.junit;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.util.StringUtils;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit runner reads Citrus test annotation for XML test cases and prepares test execution within proper Citrus
 * test context boundaries. Supports package scan as well as multiple test method annotations within one single class.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusJUnit4Runner extends BlockJUnit4ClassRunner {

    /**
     * Default constructor using class to run.
     * @param clazz the test class to be run
     */
    public CitrusJUnit4Runner(Class<?> clazz) throws InitializationError {
        super(clazz);
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
            if (method.getMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = method.getMethod().getAnnotation(CitrusTest.class);

                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), citrusTestAnnotation.name(),
                            method.getMethod().getDeclaringClass().getPackage().getName()));
                } else {
                    interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), method.getDeclaringClass().getSimpleName() + "." + method.getName(),
                            method.getMethod().getDeclaringClass().getPackage().getName()));
                }
            } else if (method.getMethod().getAnnotation(CitrusTestSource.class) != null) {
                CitrusTestSource citrusTestAnnotation = method.getMethod().getAnnotation(CitrusTestSource.class);
                interceptedMethods.addAll(JUnit4Helper.findInterceptedMethods(method, citrusTestAnnotation.type(),
                        citrusTestAnnotation.name(), citrusTestAnnotation.packageName(),
                        citrusTestAnnotation.packageScan(), citrusTestAnnotation.sources()));
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
     * Special invoke method statement. Checks on {@link CitrusFrameworkMethod.Runner} instance and invokes
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
