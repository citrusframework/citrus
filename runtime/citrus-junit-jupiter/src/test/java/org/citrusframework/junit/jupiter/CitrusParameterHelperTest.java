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
import java.util.Map;
import java.util.function.Consumer;

import org.citrusframework.TestCase;
import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CitrusParameterHelperTest {

    private final RecordingTestLoader testLoader = new RecordingTestLoader();

    @Test
    public void shouldNameArgumentsFromAnnotation() throws Exception {
        Method method = getClass().getDeclaredMethod("annotatedTestMethod", TestContext.class, String.class, String.class);

        CitrusParameterHelper.injectTestParameters(testLoader, method,
                List.of(Mockito.mock(TestContext.class), "Hello", "Citrus"));

        Assertions.assertEquals(Map.of("greeting", "Hello", "user", "Citrus"), applyConfigurers());
    }

    @CitrusParameters({ "greeting", "user" })
    void annotatedTestMethod(@CitrusResource TestContext context, String greeting, String user) {
        // fixture method - never invoked
    }

    @Test
    public void shouldNameArgumentsFromParameterNamesAndSkipResources() {
        Parameter resource = Mockito.mock(Parameter.class);
        Mockito.when(resource.isNamePresent()).thenReturn(true);
        Mockito.when(resource.getName()).thenReturn("context");
        Mockito.when(resource.isAnnotationPresent(CitrusResource.class)).thenReturn(true);

        Parameter greeting = Mockito.mock(Parameter.class);
        Mockito.when(greeting.isNamePresent()).thenReturn(true);
        Mockito.when(greeting.getName()).thenReturn("greeting");

        Parameter user = Mockito.mock(Parameter.class);
        Mockito.when(user.isNamePresent()).thenReturn(true);
        Mockito.when(user.getName()).thenReturn("user");

        CitrusParameterHelper.injectTestParameters(testLoader, "parameterNamesTestMethod", null,
                new Parameter[]{ resource, greeting, user }, List.of(new Object(), "Hello", "Citrus"));

        Assertions.assertEquals(Map.of("greeting", "Hello", "user", "Citrus"), applyConfigurers());
    }

    @Test
    public void shouldFailOnUnnamedArguments() {
        Parameter parameter = Mockito.mock(Parameter.class);
        Mockito.when(parameter.isNamePresent()).thenReturn(false);

        CitrusRuntimeException exception = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> CitrusParameterHelper.injectTestParameters(testLoader, "unnamedTestMethod", null,
                        new Parameter[]{ parameter }, List.of("Hello")));

        Assertions.assertEquals("Unable to name the arguments of unnamedTestMethod - add @CitrusParameters",
                exception.getMessage());
    }

    @Test
    public void shouldSkipEmptyArguments() throws Exception {
        Method method = getClass().getDeclaredMethod("annotatedTestMethod", TestContext.class, String.class, String.class);

        CitrusParameterHelper.injectTestParameters(testLoader, method, List.of());
        CitrusParameterHelper.injectTestParameters(testLoader, method, null);

        Assertions.assertTrue(testLoader.getConfigurers().isEmpty());
    }

    private Map<String, Object> applyConfigurers() {
        DefaultTestCase testCase = new DefaultTestCase();
        testLoader.getConfigurers().forEach(configurer -> configurer.accept(testCase));
        return testCase.getParameters();
    }

    private static class RecordingTestLoader implements TestLoader {

        private final List<Consumer<TestCase>> configurers = new ArrayList<>();

        List<Consumer<TestCase>> getConfigurers() {
            return configurers;
        }

        @Override
        public void load() {
        }

        @Override
        public void configureTestCase(Consumer<TestCase> configurer) {
            configurers.add(configurer);
        }

        @Override
        public void doWithTestCase(Consumer<TestCase> handler) {
        }

        @Override
        public void setTestClass(Class<?> testClass) {
        }

        @Override
        public void setTestName(String testName) {
        }

        @Override
        public void setPackageName(String packageName) {
        }

        @Override
        public TestCase getTestCase() {
            return null;
        }
    }
}
