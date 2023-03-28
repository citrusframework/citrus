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

package org.citrusframework.junit;

import java.lang.annotation.Annotation;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public final class JUnit4ParameterHelper {

    /**
     * Prevent instantiation of utility class.
     */
    private JUnit4ParameterHelper() {
        // prevent instantiation
    }

    /**
     * Resolves value for annotated method parameter.
     *
     * @param frameworkMethod
     * @param parameterType
     * @return
     */
    public static Object resolveAnnotatedResource(CitrusFrameworkMethod frameworkMethod, Class<?> parameterType, TestContext context) {
        Object storedBuilder = frameworkMethod.getAttribute(JUnit4Helper.BUILDER_ATTRIBUTE);
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
     * Resolves method arguments supporting TestNG data provider parameters as well as
     * {@link CitrusResource} annotated methods.
     *
     * @param frameworkMethod
     * @param context
     * @return
     */
    public static Object[] resolveParameter(CitrusFrameworkMethod frameworkMethod, TestContext context) {
        Object[] values = new Object[frameworkMethod.getMethod().getParameterTypes().length];
        Class<?>[] parameterTypes = frameworkMethod.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Annotation[] parameterAnnotations = frameworkMethod.getMethod().getParameterAnnotations()[i];
            Class<?> parameterType = parameterTypes[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof CitrusResource) {
                    values[i] = resolveAnnotatedResource(frameworkMethod, parameterType, context);
                }
            }
        }

        return values;
    }
}
