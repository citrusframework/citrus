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

package org.citrusframework.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.citrusframework.TestCase;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompositeTestLoaderTest {

    @Test
    public void runsAllChildrenInOrderAndHandlersOnce() {
        List<String> loadOrder = new ArrayList<>();
        FakeLoader first = new FakeLoader("first", loadOrder);
        FakeLoader second = new FakeLoader("second", loadOrder);
        CompositeTestLoader composite = new CompositeTestLoader(List.of("first", "second"), List.of(first, second));

        List<TestCase> handlerCases = new ArrayList<>();
        composite.doWithTestCase(handlerCases::add);

        composite.load();

        Assertions.assertEquals(List.of("first", "second"), loadOrder);
        Assertions.assertEquals(1, handlerCases.size());
        Assertions.assertSame(second.getTestCase(), handlerCases.get(0));
        Assertions.assertSame(second.getTestCase(), composite.getTestCase());
        Assertions.assertEquals(List.of(first.getTestCase(), second.getTestCase()), composite.getTestCases());
    }

    @Test
    public void forwardsConfigurationToChildren() {
        List<String> loadOrder = new ArrayList<>();
        FakeLoader first = new FakeLoader("first", loadOrder);
        FakeLoader second = new FakeLoader("second", loadOrder);
        CompositeTestLoader composite = new CompositeTestLoader(List.of("first", "second"), List.of(first, second));

        composite.setTestClass(CompositeTestLoaderTest.class);
        composite.setTestName("testName");
        composite.setPackageName("com.example");

        Assertions.assertEquals(CompositeTestLoaderTest.class, first.testClass);
        Assertions.assertEquals(CompositeTestLoaderTest.class, second.testClass);
        Assertions.assertEquals("testName", first.testName);
        Assertions.assertEquals("testName", second.testName);
        Assertions.assertEquals("com.example", first.packageName);
        Assertions.assertEquals("com.example", second.packageName);

        List<TestCase> configured = new ArrayList<>();
        composite.configureTestCase(configured::add);
        composite.load();

        Assertions.assertEquals(List.of(first.getTestCase(), second.getTestCase()), configured);
    }

    @Test
    public void failsWhenChildFailsAndNamesFailedSources() {
        List<String> loadOrder = new ArrayList<>();
        FakeLoader first = new FakeLoader("first", loadOrder);
        TestCaseFailedException failure = new TestCaseFailedException(
                new CitrusRuntimeException("second source failed"));
        FakeLoader second = new FakeLoader("second", loadOrder, failure);
        CompositeTestLoader composite = new CompositeTestLoader(List.of("first", "second"), List.of(first, second));

        List<TestCase> handlerCases = new ArrayList<>();
        composite.doWithTestCase(handlerCases::add);

        TestCaseFailedException exception = Assertions.assertThrows(TestCaseFailedException.class, composite::load);

        Assertions.assertEquals("1 of 2 test sources failed: second", exception.getMessage());
        Assertions.assertSame(failure, exception.getCause().getCause());
        Assertions.assertEquals(List.of("first", "second"), loadOrder);
        Assertions.assertSame(second.getTestCase(), composite.getTestCase());
        Assertions.assertTrue(handlerCases.isEmpty());
    }

    @Test
    public void namesAllFailedSources() {
        List<String> loadOrder = new ArrayList<>();
        FakeLoader first = new FakeLoader("first", loadOrder);
        FakeLoader second = new FakeLoader("second", loadOrder,
                new TestCaseFailedException(new CitrusRuntimeException("second source failed")));
        FakeLoader third = new FakeLoader("third", loadOrder,
                new TestCaseFailedException(new CitrusRuntimeException("third source failed")));
        CompositeTestLoader composite = new CompositeTestLoader(List.of("first", "second", "third"),
                List.of(first, second, third));

        TestCaseFailedException exception = Assertions.assertThrows(TestCaseFailedException.class, composite::load);

        Assertions.assertEquals("2 of 3 test sources failed: second, third", exception.getMessage());
        Assertions.assertSame(second.getTestCase(), composite.getTestCase());
    }

    private static class FakeLoader implements TestLoader {

        private final String name;
        private final List<String> loadOrder;
        private final TestCase testCase = new FakeTestCase();
        private final RuntimeException failure;

        private Class<?> testClass;
        private String testName;
        private String packageName;

        private FakeLoader(String name, List<String> loadOrder) {
            this(name, loadOrder, null);
        }

        private FakeLoader(String name, List<String> loadOrder, RuntimeException failure) {
            this.name = name;
            this.loadOrder = loadOrder;
            this.failure = failure;
        }

        @Override
        public void load() {
            loadOrder.add(name);
            if (failure != null) {
                throw failure;
            }
        }

        @Override
        public void configureTestCase(Consumer<TestCase> configurer) {
            configurer.accept(testCase);
        }

        @Override
        public void doWithTestCase(Consumer<TestCase> handler) {
            handler.accept(testCase);
        }

        @Override
        public void setTestClass(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public void setTestName(String testName) {
            this.testName = testName;
        }

        @Override
        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public TestCase getTestCase() {
            return testCase;
        }
    }

    /** Minimal test case instance used as marker for identity assertions. */
    private static class FakeTestCase extends org.citrusframework.base.DefaultTestCase {
    }
}
