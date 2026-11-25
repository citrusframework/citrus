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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.citrusframework.CitrusContext;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestParameterAware;
import org.citrusframework.TestResult;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultMultiTestRunTest {

    private CitrusContext citrusContext;
    private TestContext fileContext;
    private List<String> steps;

    @BeforeEach
    void setup() {
        citrusContext = CitrusContext.create();
        fileContext = citrusContext.createTestContext();
        steps = Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    void shouldRunHooksInOrder() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(record("beforeAll")))
                .beforeEach(List.of(record("beforeEach")))
                .afterEach(List.of(record("afterEach")))
                .afterAll(List.of(record("afterAll")))
                .test(test("a", cleanup("cleanup-a"), record("a")))
                .test(test("b", record("b")))
                .build());

        runAll(run);

        Assertions.assertEquals(List.of("beforeAll", "beforeEach", "a", "finally-a", "afterEach", "cleanup-a",
                "beforeEach", "b", "finally-b", "afterEach", "afterAll"), steps);
    }

    @Test
    void shouldRunEachHookOncePerScope() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(record("beforeAll")))
                .beforeEach(List.of(record("beforeEach")))
                .afterEach(List.of(record("afterEach")))
                .afterAll(List.of(record("afterAll")))
                .test(test("a"))
                .test(test("b"))
                .test(test("c"))
                .build());

        runAll(run);

        Assertions.assertEquals(1, Collections.frequency(steps, "beforeAll"));
        Assertions.assertEquals(3, Collections.frequency(steps, "beforeEach"));
        Assertions.assertEquals(3, Collections.frequency(steps, "afterEach"));
        Assertions.assertEquals(1, Collections.frequency(steps, "afterAll"));
        Assertions.assertEquals(List.of("a", "b", "c"),
                citrusContext.getTestResults().asList().stream().map(TestResult::getTestName).toList());
        Assertions.assertTrue(citrusContext.getTestResults().asList().stream().allMatch(TestResult::isSuccess));
    }

    @Test
    void shouldLetBeforeEachSeeTestVariables() {
        DefaultMultiTestRun run = run(() -> {
            DefaultTestCase test = test("a");
            test.getVariableDefinitions().put("orderId", "42");
            return file().beforeEach(List.of(recordVariable("beforeEach", "orderId"))).test(test).build();
        });

        runAll(run);

        Assertions.assertEquals(List.of("beforeEach=42", "finally-a"), steps);
    }

    @Test
    void shouldShareStateFromBeforeAll() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(setVariable("brokerUrl", "tcp://localhost:61616")))
                .afterAll(List.of(recordVariable("afterAll", "brokerUrl"), recordVariableKnown("afterAll", "orderId")))
                .test(test("a", setVariable("orderId", "1"), recordVariable("a", "brokerUrl")))
                .test(test("b", recordVariable("b", "brokerUrl"), recordVariableKnown("b", "orderId")))
                .build());

        runAll(run);

        Assertions.assertTrue(steps.contains("a=tcp://localhost:61616"));
        Assertions.assertTrue(steps.contains("b=tcp://localhost:61616"));
        Assertions.assertTrue(steps.contains("b:orderId=false"));
        Assertions.assertTrue(steps.contains("afterAll=tcp://localhost:61616"));
        Assertions.assertTrue(steps.contains("afterAll:orderId=false"));
    }

    @Test
    void shouldOverrideSharedVariablePerTest() {
        DefaultMultiTestRun run = run(() -> {
            DefaultTestCase b = test("b", recordVariable("b", "user"));
            b.getVariableDefinitions().put("alias", "bob");
            b.getVariableDefinitions().put("user", "${alias}");
            return file().beforeAll(List.of(setVariable("user", "alice")))
                    .test(test("a", recordVariable("a", "user")))
                    .test(b)
                    .test(test("c", recordVariable("c", "user")))
                    .build();
        });

        runAll(run);

        Assertions.assertEquals(List.of("a=alice", "finally-a", "b=bob", "finally-b", "c=alice", "finally-c"), steps);
    }

    @Test
    void shouldResolveInheritedExpressionsAsVariableDefinitions() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(setVariable("user", "alice"), setVariable("greeting", "Hello ${user}")))
                .afterAll(List.of(recordVariable("afterAll", "greeting")))
                .test(test("a", recordVariable("a", "greeting")))
                .build());

        runAll(run);

        Assertions.assertEquals(List.of("a=Hello alice", "finally-a", "afterAll=Hello ${user}"), steps);
    }

    @Test
    void shouldPassHostArgumentsToEveryPart() {
        List<String> configured = Collections.synchronizedList(new ArrayList<>());
        Consumer<TestCase> parameters = testCase -> ((TestParameterAware) testCase)
                .setParameters(new String[] { "env" }, new Object[] { "dev" });
        Consumer<TestCase> groups = testCase -> configured.add(testCase.getName());

        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(recordVariable("beforeAll", "env")))
                .afterAll(List.of(recordVariable("afterAll", "env")))
                .test(test("a", recordVariable("a", "env")))
                .test(test("b", recordVariable("b", "env")))
                .build(), List.of(parameters, groups));

        runAll(run);

        Assertions.assertEquals(List.of("beforeAll=dev", "a=dev", "finally-a", "b=dev", "finally-b", "afterAll=dev"), steps);
        Assertions.assertEquals(List.of("orders", "a", "b"), configured);
        Assertions.assertTrue(run.getTestCases().stream()
                .allMatch(testCase -> "dev".equals(((TestParameterAware) testCase).getParameters().get("env"))));
    }

    @Test
    void shouldLetBeforeAllVariablesWinOverHostArguments() {
        Consumer<TestCase> parameters = testCase -> ((TestParameterAware) testCase)
                .setParameters(new String[] { "env" }, new Object[] { "dev" });

        DefaultMultiTestRun run = run(() -> {
            DefaultTestCase b = test("b", recordVariable("b", "env"));
            b.getVariableDefinitions().put("env", "own");
            return file().beforeAll(List.of(setVariable("env", "local"), recordVariable("beforeAll", "env")))
                    .test(test("a", recordVariable("a", "env")))
                    .test(b)
                    .build();
        }, List.of(parameters));

        runAll(run);

        Assertions.assertEquals(List.of("beforeAll=local", "a=local", "finally-a", "b=own", "finally-b"), steps);
    }

    @Test
    void shouldReportTestIdentity() {
        DefaultMultiTestRun run = run(() -> file().test(test("a")).test(test("b")).build());

        runAll(run);

        for (TestCase testCase : run.getTestCases()) {
            Assertions.assertEquals(DefaultMultiTestRunTest.class, testCase.getTestClass());
            Assertions.assertEquals("org.example", testCase.getPackageName());
        }
    }

    @Test
    void shouldRunFileCleanupsAfterAfterAll() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(cleanup("fileCleanup")))
                .afterAll(List.of(record("afterAll")))
                .test(test("a", record("a")))
                .test(test("b", record("b")))
                .build());

        runAll(run);

        Assertions.assertEquals(List.of("a", "finally-a", "b", "finally-b", "afterAll", "fileCleanup"), steps);
    }

    @Test
    void shouldParseOncePerTestAndNeverBuildTwice() {
        AtomicInteger parses = new AtomicInteger();
        DefaultMultiTestRun run = run(() -> {
            parses.incrementAndGet();
            return file()
                    .beforeEach(List.of(new OnceBuilder(record("beforeEach"))))
                    .afterEach(List.of(new OnceBuilder(record("afterEach"))))
                    .test(test("a"))
                    .test(test("b"))
                    .test(test("c"))
                    .build();
        });

        runAll(run);

        Assertions.assertEquals(4, parses.get());
        Assertions.assertEquals(3, Collections.frequency(steps, "beforeEach"));
    }

    @Test
    void shouldRunOneTestAtATime() throws InterruptedException {
        DefaultMultiTestRun run = run(() -> file()
                .test(test("a", slow("a")))
                .test(test("b", slow("b")))
                .build());

        CountDownLatch done = new CountDownLatch(2);
        new Thread(() -> { run.runTest(0); done.countDown(); }).start();
        new Thread(() -> { run.runTest(1); done.countDown(); }).start();
        Assertions.assertTrue(done.await(10, TimeUnit.SECONDS));

        List<String> recorded = steps.stream().filter(step -> !step.startsWith("finally")).toList();
        Assertions.assertTrue(recorded.equals(List.of("start-a", "end-a", "start-b", "end-b"))
                || recorded.equals(List.of("start-b", "end-b", "start-a", "end-a")), recorded.toString());
    }

    @Test
    void shouldFailEveryTestWhenBeforeAllFails() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(failing("beforeAll")))
                .beforeEach(List.of(record("beforeEach")))
                .afterEach(List.of(record("afterEach")))
                .afterAll(List.of(record("afterAll")))
                .test(test("a", record("a")))
                .test(test("b", record("b")))
                .build());

        for (int i = 0; i < 2; i++) {
            int index = i;
            TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, () -> run.runTest(index));
            Assertions.assertEquals("beforeAll of 'orders' failed", error.getMessage());
            Assertions.assertEquals("beforeAll failed", error.getCause().getCause().getMessage());
        }
        run.close();

        Assertions.assertEquals(List.of("beforeAll", "afterAll"), steps);
        Assertions.assertEquals(2, citrusContext.getTestResults().getFailed());
    }

    @Test
    void shouldFailOnlyTheTestWhoseBeforeEachFails() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeEach(List.of(failWhenBroken()))
                .afterEach(List.of(record("afterEach")))
                .test(test("a", "ok"))
                .test(test("b", "broken"))
                .test(test("c", "ok"))
                .build());

        List<String> failed = runAll(run);

        Assertions.assertEquals(List.of("b"), failed);
        Assertions.assertEquals(List.of("a", "finally-a", "afterEach", "finally-b", "afterEach", "c", "finally-c", "afterEach"), steps);
    }

    @Test
    void shouldFailOnlyTheTestWhoseAfterEachFails() {
        DefaultMultiTestRun run = run(() -> file()
                .afterEach(List.of(failWhenBroken()))
                .test(test("a", "ok"))
                .test(test("b", "broken"))
                .test(test("c", "ok"))
                .build());

        List<String> failed = runAll(run);

        Assertions.assertEquals(List.of("b"), failed);
        Assertions.assertTrue(run.getTestCases().get(0).getTestResult().isSuccess());
        Assertions.assertTrue(run.getTestCases().get(2).getTestResult().isSuccess());
    }

    @Test
    void shouldReportFailingAfterAllWithoutChangingTestResults() {
        DefaultMultiTestRun run = run(() -> file()
                .afterAll(List.of(failing("afterAll")))
                .test(test("a"))
                .test(test("b"))
                .build());

        run.runTest(0);
        run.runTest(1);
        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class, run::close);

        Assertions.assertEquals("afterAll of 'orders' failed", error.getMessage());
        Assertions.assertEquals(List.of("a", "b", "orders.afterAll"), run.getTestCases().stream().map(TestCase::getName).toList());
        Assertions.assertTrue(run.getTestCases().get(0).getTestResult().isSuccess());
        Assertions.assertTrue(run.getTestCases().get(1).getTestResult().isSuccess());
        TestCase afterAll = run.getTestCases().get(2);
        Assertions.assertTrue(afterAll.getTestResult().isFailed());
        Assertions.assertEquals(DefaultMultiTestRunTest.class, afterAll.getTestClass());
        Assertions.assertEquals(1, citrusContext.getTestResults().getFailed());
        Assertions.assertEquals(2, citrusContext.getTestResults().getSuccess());

        run.close();
        Assertions.assertEquals(3, run.getTestCases().size());
    }

    @Test
    void shouldKeepFirstCleanupFailureAsCause() {
        DefaultMultiTestRun run = run(() -> file()
                .beforeAll(List.of(failingCleanup("first"), failingCleanup("second")))
                .test(test("a"))
                .build());

        run.runTest(0);
        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class, run::close);

        Assertions.assertEquals("first failed", error.getCause().getMessage());
        Assertions.assertEquals(1, error.getSuppressed().length);
        Assertions.assertEquals("second failed", error.getSuppressed()[0].getMessage());
    }

    @Test
    void shouldSkipDisabledTestWithoutHooks() {
        DefaultMultiTestRun run = run(() -> {
            DefaultTestCase b = test("b", record("b"));
            b.getMetaInfo().setStatus(TestCaseMetaInfo.Status.DISABLED);
            return file()
                    .beforeEach(List.of(record("beforeEach")))
                    .afterEach(List.of(record("afterEach")))
                    .test(test("a", record("a")))
                    .test(b)
                    .build();
        });

        runAll(run);

        Assertions.assertEquals(List.of("beforeEach", "a", "finally-a", "afterEach"), steps);
        Assertions.assertTrue(run.getTestCases().get(1).getTestResult().isSkipped());
    }

    @Test
    void shouldNotRunFileHooksWhenAllTestsAreDisabled() {
        DefaultMultiTestRun run = run(() -> {
            DefaultTestCase a = test("a", record("a"));
            a.getMetaInfo().setStatus(TestCaseMetaInfo.Status.DISABLED);
            DefaultTestCase b = test("b", record("b"));
            b.getMetaInfo().setStatus(TestCaseMetaInfo.Status.DISABLED);
            return file().beforeAll(List.of(setVariable("user", "alice"), record("beforeAll")))
                    .afterAll(List.of(record("afterAll")))
                    .test(a)
                    .test(b)
                    .build();
        });

        runAll(run);

        Assertions.assertEquals(List.of(), steps);
        Assertions.assertFalse(fileContext.getVariables().containsKey("user"));
    }

    private DefaultMultiTestRun run(Supplier<MultiTestDefinition> parser) {
        return run(parser, List.of());
    }

    private DefaultMultiTestRun run(Supplier<MultiTestDefinition> parser, List<Consumer<TestCase>> configurers) {
        MultiTestDefinition first = parser.get();
        first.validate(first.getName());
        return new DefaultMultiTestRun(first, parser, fileContext, citrusContext,
                DefaultMultiTestRunTest.class, "org.example", configurers);
    }

    /**
     * Runs every test like a host running all tests of the file in one host test.
     * @return the names of the failed tests.
     */
    private static List<String> runAll(DefaultMultiTestRun run) {
        List<String> failed = new ArrayList<>();
        for (int i = 0; i < run.getTestNames().size(); i++) {
            try {
                run.runTest(i);
            } catch (TestCaseFailedException e) {
                failed.add(run.getTestNames().get(i));
            }
        }
        run.close();
        return failed;
    }

    private static MultiTestDefinition.Builder file() {
        return new MultiTestDefinition.Builder().name("orders");
    }

    private DefaultTestCase test(String name, TestActionBuilder<?>... actions) {
        DefaultTestCase test = new DefaultTestCase();
        test.setName(name);
        for (TestActionBuilder<?> action : actions) {
            test.getActionBuilders().add(action);
        }
        test.addFinalAction(record("finally-" + name));
        return test;
    }

    private DefaultTestCase test(String name, String mode) {
        DefaultTestCase test = test(name, record(name));
        test.getVariableDefinitions().put("mode", mode);
        test.getActionBuilders().clear();
        test.getActionBuilders().add(record(name));
        return test;
    }

    private TestActionBuilder<?> record(String step) {
        return () -> context -> steps.add(step);
    }

    private TestActionBuilder<?> recordVariable(String label, String variable) {
        return () -> context -> steps.add(label + "=" + context.getVariable(variable));
    }

    private TestActionBuilder<?> recordVariableKnown(String label, String variable) {
        return () -> context -> steps.add(label + ":" + variable + "=" + context.getVariables().containsKey(variable));
    }

    private static TestActionBuilder<?> setVariable(String name, String value) {
        return () -> context -> context.setVariable(name, value);
    }

    private TestActionBuilder<?> cleanup(String step) {
        return () -> context -> context.doFinally(record(step));
    }

    private static TestActionBuilder<?> failingCleanup(String label) {
        return () -> context -> context.doFinally(() -> ctx -> {
            throw new CitrusRuntimeException(label + " failed");
        });
    }

    private TestActionBuilder<?> failing(String step) {
        return () -> context -> {
            steps.add(step);
            throw new CitrusRuntimeException(step + " failed");
        };
    }

    private static TestActionBuilder<?> failWhenBroken() {
        return () -> context -> {
            if ("broken".equals(context.getVariable("mode"))) {
                throw new CitrusRuntimeException("broken");
            }
        };
    }

    private TestActionBuilder<?> slow(String name) {
        return () -> context -> {
            steps.add("start-" + name);
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            steps.add("end-" + name);
        };
    }

    /**
     * Builder that fails when it is built a second time.
     */
    private static final class OnceBuilder implements TestActionBuilder<TestAction> {

        private final TestActionBuilder<?> delegate;
        private final AtomicInteger builds = new AtomicInteger();

        private OnceBuilder(TestActionBuilder<?> delegate) {
            this.delegate = delegate;
        }

        @Override
        public TestAction build() {
            if (builds.incrementAndGet() > 1) {
                throw new IllegalStateException("Builder built twice");
            }
            return delegate.build();
        }
    }
}
