/*
 * Copyright the original author or authors.
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

package org.citrusframework.junit.jupiter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestCase;
import org.citrusframework.TestParameterAware;
import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Helper injects the arguments of a parameterized test method as test variables into the loaded
 * test case. Parameter names come from the {@link CitrusParameters} annotation on the method, else
 * from the method's parameter names when the class is compiled with parameter names present.
 * Arguments that the extension itself resolves ({@link CitrusResource}) are not passed on.
 *
 * @since 5.3
 */
public final class CitrusParameterHelper {

    /**
     * Prevent instantiation of utility class.
     */
    private CitrusParameterHelper() {
        // prevent instantiation
    }

    /**
     * Registers a test case configurer that sets the given method arguments as test parameters on
     * the loaded test case. Skipped when no arguments are given.
     * @param testLoader the test loader loading the test case.
     * @param method the parameterized test method.
     * @param arguments the resolved arguments of the current invocation.
     */
    public static void injectTestParameters(TestLoader testLoader, Method method, List<Object> arguments) {
        if (testLoader == null || method == null || arguments == null || arguments.isEmpty()) {
            return;
        }

        injectTestParameters(testLoader, method.getName(), method.getAnnotation(CitrusParameters.class),
                method.getParameters(), arguments);
    }

    /**
     * Computes parameter names for all non resource arguments and registers a configurer that sets
     * those as test parameters on the loaded test case.
     */
    static void injectTestParameters(TestLoader testLoader, String methodName, CitrusParameters annotation,
            Parameter[] parameters, List<Object> arguments) {
        if (arguments.isEmpty()) {
            return;
        }

        List<String> parameterNames = new ArrayList<>();
        List<Object> parameterValues = new ArrayList<>();

        int annotationIndex = 0;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(CitrusResource.class)) {
                continue;
            }

            String parameterName = null;
            if (annotation != null && annotationIndex < annotation.value().length) {
                parameterName = annotation.value()[annotationIndex];
            } else if (parameter.isNamePresent()) {
                parameterName = parameter.getName();
            }

            if (parameterName == null) {
                throw new CitrusRuntimeException(
                        "Unable to name the arguments of %s - add @CitrusParameters".formatted(methodName));
            }

            parameterNames.add(parameterName);
            parameterValues.add(arguments.get(i));
            annotationIndex++;
        }

        if (parameterValues.isEmpty()) {
            return;
        }

        String[] names = parameterNames.toArray(new String[0]);
        Object[] values = parameterValues.toArray();
        testLoader.configureTestCase(testCase -> {
            if (testCase instanceof TestParameterAware testParameterAware) {
                testParameterAware.setParameters(names, values);
            }
        });
    }
}
