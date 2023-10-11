/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.testng;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestParameterAware;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public final class TestNGParameterHelper {

    /**
     * Prevent instantiation of utility class.
     */
    private TestNGParameterHelper() {
        // prevent instantiation
    }

    /**
     * Resolves method arguments supporting TestNG data provider parameters as well as
     * {@link CitrusResource} annotated methods.
     *
     * @param testResult
     * @param method
     * @param context
     * @param invocationCount
     * @return
     */
    public static Object[] resolveParameter(Object target, ITestResult testResult, final Method method, TestContext context, int invocationCount) {
        Object[] dataProviderParams = null;
        if (method.getAnnotation(Test.class) != null &&
                StringUtils.hasText(method.getAnnotation(Test.class).dataProvider())) {
            final Method[] dataProvider = new Method[1];
            ReflectionHelper.doWithMethods(method.getDeclaringClass(), current -> {
                if (current.getAnnotation(DataProvider.class) == null) {
                    return;
                }

                if (StringUtils.hasText(current.getAnnotation(DataProvider.class).name()) &&
                        current.getAnnotation(DataProvider.class).name().equals(method.getAnnotation(Test.class).dataProvider())) {
                    dataProvider[0] = current;
                } else if (current.getName().equals(method.getAnnotation(Test.class).dataProvider())) {
                    dataProvider[0] = current;
                }

            });

            if (dataProvider[0] == null) {
                throw new CitrusRuntimeException("Unable to find data provider: " + method.getAnnotation(Test.class).dataProvider());
            }

            Object[][] parameters = (Object[][]) ReflectionHelper.invokeMethod(dataProvider[0], target,
                    resolveParameter(target, testResult, dataProvider[0], context, -1));
            if (parameters != null) {
                dataProviderParams = parameters[invocationCount % parameters.length];
            }
        }

        Object[] values = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
            Class<?> parameterType = parameterTypes[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof CitrusResource) {
                    values[i] = resolveAnnotatedResource(testResult, parameterType, context);
                }
            }

            if (parameterType.equals(ITestResult.class)) {
                values[i] = testResult;
            } else if (parameterType.equals(ITestContext.class)) {
                values[i] = testResult.getTestContext();
            } else if (values[i] == null && dataProviderParams != null && i < dataProviderParams.length) {
                values[i] = dataProviderParams[i];
            }
        }

        return values;
    }

    /**
     * Resolves value for annotated method parameter.
     *
     * @param testResult
     * @param parameterType
     * @return
     */
    private static Object resolveAnnotatedResource(ITestResult testResult, Class<?> parameterType, TestContext context) {
        Object storedBuilder = testResult.getAttribute(TestNGHelper.BUILDER_ATTRIBUTE);
        if (TestCaseRunner.class.isAssignableFrom(parameterType)) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        } else if (TestContext.class.isAssignableFrom(parameterType)) {
            return context;
        } else {
            throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + parameterType);
        }
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     *
     * @param method the method currently executed
     * @param testCase the constructed Citrus test.
     */
    public static void injectTestParameters(Method method, TestCase testCase, Object[] parameterValues) {
        if (testCase instanceof TestParameterAware) {
            ((TestParameterAware) testCase).setParameters(getParameterNames(method), parameterValues);
        }
    }

    /**
     * Read parameter names form method annotation.
     * @param method
     * @return
     */
    private static String[] getParameterNames(Method method) {
        String[] parameterNames;
        CitrusParameters citrusParameters = method.getAnnotation(CitrusParameters.class);
        Parameters testNgParameters = method.getAnnotation(Parameters.class);
        if (citrusParameters != null) {
            parameterNames = citrusParameters.value();
        } else if (testNgParameters != null) {
            parameterNames = testNgParameters.value();
        } else {
            List<String> methodParameterNames = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                methodParameterNames.add(parameter.getName());
            }
            parameterNames = methodParameterNames.toArray(new String[0]);
        }

        return parameterNames;
    }
}
