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

package org.citrusframework.api.common;

import java.util.List;
import java.util.function.Consumer;

import org.citrusframework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TestLoaderTest {

    @Test
    void shouldHaveNoTestCasesBeforeLoad() {
        Assertions.assertEquals(List.of(), new SimpleTestLoader().getTestCases());
    }

    @Test
    void shouldReturnLoadedTestCase() {
        SimpleTestLoader loader = new SimpleTestLoader();
        TestCase testCase = Mockito.mock(TestCase.class);
        loader.testCase = testCase;

        Assertions.assertEquals(List.of(testCase), loader.getTestCases());
    }

    /**
     * Minimal loader that only implements the abstract methods of the interface.
     */
    private static class SimpleTestLoader implements TestLoader {

        private TestCase testCase;

        @Override
        public void load() {
        }

        @Override
        public void configureTestCase(Consumer<TestCase> handler) {
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
            return testCase;
        }
    }
}
