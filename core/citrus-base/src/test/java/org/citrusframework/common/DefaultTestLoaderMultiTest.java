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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.citrusframework.CitrusContext;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultTestLoaderMultiTest {

    private CitrusContext citrusContext;
    private List<String> steps;

    @BeforeEach
    void setup() {
        citrusContext = CitrusContext.create();
        steps = Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    void shouldRunAllTestsAsOneLoad() {
        MultiLoader loader = loader(() -> file(test("a"), test("b"), test("c")));
        List<TestCase> handled = new ArrayList<>();
        loader.doWithTestCase(handled::add);

        loader.load();

        Assertions.assertEquals(List.of("a", "b", "c"), steps);
        Assertions.assertEquals(List.of("a", "b", "c"), loader.getTestCases().stream().map(TestCase::getName).toList());
        Assertions.assertEquals("c", loader.getTestCase().getName());
        Assertions.assertEquals(1, handled.size());
        Assertions.assertSame(loader.getTestCase(), handled.get(0));
        Assertions.assertEquals(3, citrusContext.getTestResults().getSize());
        Assertions.assertEquals(3, citrusContext.getTestResults().getSuccess());
    }

    @Test
    void shouldFailNamingFailedTests() {
        MultiLoader loader = loader(() -> file(test("a"), failingTest("b"), failingTest("c"), test("d")));
        List<TestCase> handled = new ArrayList<>();
        loader.doWithTestCase(handled::add);

        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals("2 of 4 tests in 'orders' failed: b, c", error.getMessage());
        Assertions.assertEquals("b failed", rootCause(error).getMessage());
        Assertions.assertEquals("b", loader.getTestCase().getName());
        Assertions.assertTrue(handled.isEmpty());
        Assertions.assertEquals(List.of("a", "b", "c", "d"), steps);
        Assertions.assertEquals(4, citrusContext.getTestResults().getSize());
        Assertions.assertEquals(2, citrusContext.getTestResults().getFailed());
        Assertions.assertTrue(loader.getTestCases().get(0).getTestResult().isSuccess());
        Assertions.assertTrue(loader.getTestCases().get(3).getTestResult().isSuccess());
    }

    @Test
    void shouldFailWhenOnlyAfterAllFails() {
        MultiLoader loader = loader(() -> new MultiTestDefinition.Builder()
                .name("orders")
                .afterAll(List.of(() -> context -> {
                    throw new CitrusRuntimeException("cleanup broken");
                }))
                .test(test("a"))
                .test(test("b"))
                .build());

        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals("1 of 2 tests in 'orders' failed: orders.afterAll", error.getMessage());
        Assertions.assertEquals("orders.afterAll", loader.getTestCase().getName());
        Assertions.assertTrue(loader.getTestCases().get(0).getTestResult().isSuccess());
        Assertions.assertTrue(loader.getTestCases().get(1).getTestResult().isSuccess());
        Assertions.assertEquals(3, citrusContext.getTestResults().getSize());
    }

    @Test
    void shouldApplyConfigurersToSharedDeclarationsAndEveryTest() {
        MultiLoader loader = loader(() -> file(test("a"), test("b")));
        List<String> configured = new ArrayList<>();
        loader.configureTestCase(testCase -> configured.add(testCase.getName()));

        loader.load();

        Assertions.assertEquals(List.of("orders", "a", "b"), configured);
    }

    @Test
    void shouldContributeAllTestsToComposite() {
        DefaultTestLoader single = new DefaultTestLoader();
        single.setCitrusContext(citrusContext);
        single.setTestName("single");
        MultiLoader multi = loader(() -> file(test("a"), failingTest("b")));

        CompositeTestLoader composite = new CompositeTestLoader(List.of("single", "orders"), List.of(single, multi));
        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, composite::load);

        Assertions.assertEquals("1 of 2 test sources failed: orders", error.getMessage());
        Assertions.assertEquals(List.of("single", "a", "b"), composite.getTestCases().stream().map(TestCase::getName).toList());
        Assertions.assertEquals(3, citrusContext.getTestResults().getSize());
    }

    private MultiLoader loader(Supplier<MultiTestDefinition> parser) {
        MultiLoader loader = new MultiLoader(parser);
        loader.setCitrusContext(citrusContext);
        loader.setTestClass(DefaultTestLoaderMultiTest.class);
        loader.setTestName("orders");
        loader.setPackageName("org.example");
        return loader;
    }

    private static MultiTestDefinition file(DefaultTestCase... tests) {
        MultiTestDefinition.Builder builder = new MultiTestDefinition.Builder().name("orders");
        for (DefaultTestCase test : tests) {
            builder.test(test);
        }
        return builder.build();
    }

    private DefaultTestCase test(String name) {
        DefaultTestCase test = new DefaultTestCase();
        test.setName(name);
        test.getActionBuilders().add((TestActionBuilder<?>) () -> context -> steps.add(name));
        return test;
    }

    private DefaultTestCase failingTest(String name) {
        DefaultTestCase test = new DefaultTestCase();
        test.setName(name);
        test.getActionBuilders().add((TestActionBuilder<?>) () -> context -> {
            steps.add(name);
            throw new CitrusRuntimeException(name + " failed");
        });
        return test;
    }

    private static Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Loader that runs a multi-test source built in Java.
     */
    private static final class MultiLoader extends DefaultTestLoader implements TestLoader {

        private final Supplier<MultiTestDefinition> parser;

        private MultiLoader(Supplier<MultiTestDefinition> parser) {
            this.parser = parser;
        }

        @Override
        protected void doLoad() {
            MultiTestDefinition first = parser.get();
            first.validate(first.getName());
            runMultiTest(createMultiTestRun(first, parser));
        }
    }
}
