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

package org.citrusframework.junit.jupiter.regression;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.container.SequenceAfterSuite;
import org.citrusframework.container.SequenceBeforeSuite;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verifies method-level and class-level JUnit Jupiter parallel execution with Citrus extensions.
 */
@Isolated
class ParallelMethodExecutionTest {

    @BeforeEach
    void resetBefore() {
        CitrusExtension.reset();
        CitrusInstanceManager.reset();
    }

    @AfterEach
    void resetAfter() {
        CitrusExtension.reset();
        CitrusInstanceManager.reset();
    }

    @Test
    void shouldExecuteContainerBehaviorsAndOverloadedMethodsInParallel() {
        ConcurrentBehaviorAndOverloadSample.reset();

        TestExecutionSummary summary = executeConcurrently(ConcurrentBehaviorAndOverloadSample.class);

        Assertions.assertEquals(4, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(4, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
        Assertions.assertEquals(0, summary.getTestsFailedCount());
        Assertions.assertTrue(ConcurrentBehaviorAndOverloadSample.THREADS.size() >= 2,
                "Expected methods to execute on multiple threads, got: " + ConcurrentBehaviorAndOverloadSample.THREADS);
    }

    @Test
    void shouldIsolateConcurrentParameterizedAndRepeatedInvocations() {
        ConcurrentTemplateInvocationsSample.reset();

        TestExecutionSummary summary = executeConcurrently(ConcurrentTemplateInvocationsSample.class);

        Assertions.assertEquals(8, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(8, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
        Assertions.assertEquals(8, ConcurrentTemplateInvocationsSample.EXECUTED_ROWS.get(),
                "Every template invocation must execute its own TestCase without short-circuiting");
    }

    @Test
    void shouldParseBindToRegistryOnceAndBlockConcurrentMethodsUntilReady() {
        ConcurrentBindToRegistrySample.reset();

        TestExecutionSummary summary = executeConcurrently(ConcurrentBindToRegistrySample.class);

        Assertions.assertEquals(4, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(4, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
        Assertions.assertEquals(1, ConcurrentBindToRegistrySample.BIND_CALLS.get(),
                "@BindToRegistry factory method must only be invoked once per class");
    }

    @Test
    void shouldCompleteBeforeSuiteBeforeAnyConcurrentTestMethodStarts() {
        SuiteBarrierClassOneSample.reset();

        Citrus citrus = Citrus.newInstance();
        citrus.getCitrusContext().addComponent("beforeSuiteBarrier", new SequenceBeforeSuite.Builder()
                .actions(ctx -> {
                    try {
                        Thread.sleep(80L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    SuiteBarrierClassOneSample.SUITE_READY.set(true);
                })
                .build());
        citrus.getCitrusContext().addComponent("afterSuiteCounter", new SequenceAfterSuite.Builder()
                .actions(ctx -> SuiteBarrierClassOneSample.AFTER_SUITE_COUNT.incrementAndGet())
                .build());

        TestExecutionSummary summary = executeConcurrently(
                SuiteBarrierClassOneSample.class,
                SuiteBarrierClassTwoSample.class);

        Assertions.assertEquals(4, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(4, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
        Assertions.assertEquals(1, SuiteBarrierClassOneSample.AFTER_SUITE_COUNT.get(),
                "afterSuite must execute once when root test plan finishes");
    }

    @Test
    void shouldIsolateParallelSpringContextsAndNonSpringTests() {
        ParallelSpringContextASample.reset();

        TestExecutionSummary summary = executeConcurrently(
                ParallelSpringContextASample.class,
                ParallelSpringContextBSample.class,
                ParallelNonSpringAlongsideSpringSample.class);

        Assertions.assertEquals(6, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(6, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
    }

    @Test
    void shouldSupportPerClassLifecycleWithParameterInjectionInParallel() {
        PerClassParameterInjectionSample.reset();

        TestExecutionSummary summary = executeConcurrently(PerClassParameterInjectionSample.class);

        Assertions.assertEquals(3, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(3, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
    }

    @Test
    void shouldSupportPerClassLifecycleWithFieldInjectionWhenSameThread() {
        TestExecutionSummary summary = executeConcurrently(PerClassSameThreadFieldInjectionSample.class);

        Assertions.assertEquals(2, summary.getTestsFoundCount(), "tests found");
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), () -> "failures: " + formatFailures(summary));
    }

    @Test
    void shouldRejectConcurrentMutationOfPerClassResourceField() {
        PerClassConcurrentUnsafeFieldSample.reset();

        TestExecutionSummary summary = executeConcurrently(PerClassConcurrentUnsafeFieldSample.class);

        Assertions.assertEquals(2, summary.getTestsFoundCount(), "tests found");
        Assertions.assertTrue(summary.getTestsFailedCount() >= 1,
                "Expected fast failure when @TestInstance(PER_CLASS) shares per-test @CitrusResource fields concurrently");
        Assertions.assertTrue(summary.getFailures().stream()
                        .anyMatch(f -> f.getException() instanceof CitrusRuntimeException
                                && f.getException().getMessage().contains("PER_CLASS")),
                () -> "Expected CitrusRuntimeException mentioning PER_CLASS, got: " + formatFailures(summary));
    }

    private static TestExecutionSummary executeConcurrently(Class<?>... sampleClasses) {
        DiscoverySelectors[] unused = null;
        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request()
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "8");

        for (Class<?> sampleClass : sampleClasses) {
            builder.selectors(DiscoverySelectors.selectClass(sampleClass));
        }

        LauncherDiscoveryRequest request = builder.build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.execute(request, listener);
        return listener.getSummary();
    }

    private static String formatFailures(TestExecutionSummary summary) {
        StringBuilder sb = new StringBuilder();
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            sb.append(failure.getTestIdentifier().getDisplayName())
                    .append(": ")
                    .append(failure.getException())
                    .append("; ");
        }
        return sb.toString();
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    static class ConcurrentBehaviorAndOverloadSample implements TestActionSupport {

        static final Set<String> THREADS = ConcurrentHashMap.newKeySet();
        static volatile CyclicBarrier barrier = new CyclicBarrier(4);

        static void reset() {
            THREADS.clear();
            barrier = new CyclicBarrier(4);
        }

        @Test
        @CitrusTest
        void shouldApplyInContainer(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            runner.run(sequential().actions(
                    createVariable("localKey", "inContainerWithRunner"),
                    action(ctx -> awaitBarrier()),
                    apply().behavior(new VerifyLocalVariableBehavior("inContainerWithRunner")).on(runner),
                    runner.applyBehavior(new VerifyLocalVariableBehavior("inContainerWithRunner"))
            ));
            Assertions.assertEquals("inContainerWithRunner", context.getVariable("localKey"));
        }

        @Test
        @CitrusTest
        void shouldApplyInContainerWithoutRunner(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            runner.run(sequential().actions(
                    createVariable("localKey", "inContainerWithoutRunner"),
                    action(ctx -> awaitBarrier()),
                    apply(new VerifyLocalVariableBehavior("inContainerWithoutRunner")),
                    apply().behavior(new VerifyLocalVariableBehavior("inContainerWithoutRunner"))
            ));
            Assertions.assertEquals("inContainerWithoutRunner", context.getVariable("localKey"));
        }

        @Test
        @CitrusTest
        void overloaded(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            runner.run(sequential().actions(
                    createVariable("localKey", "overloadedTwoArgs"),
                    action(ctx -> awaitBarrier()),
                    echo("${localKey}")
            ));
            Assertions.assertEquals("overloadedTwoArgs", context.getVariable("localKey"));
        }

        @Test
        @CitrusTest
        void overloaded(TestInfo testInfo, @CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            Assertions.assertNotNull(testInfo);
            runner.run(sequential().actions(
                    createVariable("localKey", "overloadedThreeArgs"),
                    action(ctx -> awaitBarrier()),
                    echo("${localKey}")
            ));
            Assertions.assertEquals("overloadedThreeArgs", context.getVariable("localKey"));
        }

        private static void awaitBarrier() {
            THREADS.add(Thread.currentThread().getName());
            try {
                barrier.await(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class VerifyLocalVariableBehavior implements TestBehavior, TestActionSupport {
        private final String expected;

        VerifyLocalVariableBehavior(String expected) {
            this.expected = expected;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertEquals(expected, ctx.getVariable("localKey")));
            runner.run(echo("Verified ${localKey}"));
        }
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    static class ConcurrentTemplateInvocationsSample implements TestActionSupport {

        static final AtomicInteger EXECUTED_ROWS = new AtomicInteger();
        static volatile CyclicBarrier paramBarrier = new CyclicBarrier(4);
        static volatile CyclicBarrier repeatBarrier = new CyclicBarrier(4);

        static void reset() {
            EXECUTED_ROWS.set(0);
            paramBarrier = new CyclicBarrier(4);
            repeatBarrier = new CyclicBarrier(4);
        }

        @ParameterizedTest
        @ValueSource(strings = {"alpha", "beta", "gamma", "delta"})
        @CitrusTest
        void parameterizedRow(String row, @CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            runner.run(sequential().actions(
                    createVariable("sharedVarName", row),
                    action(ctx -> {
                        EXECUTED_ROWS.incrementAndGet();
                        await(paramBarrier);
                    }),
                    apply(new ExpectedVarBehavior(row))
            ));
            Assertions.assertEquals(row, context.getVariable("sharedVarName"));
        }

        @RepeatedTest(4)
        @CitrusTest
        void repeatedRow(org.junit.jupiter.api.RepetitionInfo repetitionInfo,
                         @CitrusResource TestActionRunner runner,
                         @CitrusResource TestContext context) {
            String expected = "rep-" + repetitionInfo.getCurrentRepetition();
            runner.run(sequential().actions(
                    createVariable("sharedVarName", expected),
                    action(ctx -> {
                        EXECUTED_ROWS.incrementAndGet();
                        await(repeatBarrier);
                    }),
                    apply(new ExpectedVarBehavior(expected))
            ));
            Assertions.assertEquals(expected, context.getVariable("sharedVarName"));
        }

        private static void await(CyclicBarrier barrier) {
            try {
                barrier.await(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ExpectedVarBehavior implements TestBehavior, TestActionSupport {
        private final String expected;

        ExpectedVarBehavior(String expected) {
            this.expected = expected;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertEquals(expected, ctx.getVariable("sharedVarName")));
        }
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    public static class ConcurrentBindToRegistrySample implements TestActionSupport {

        static final AtomicInteger BIND_CALLS = new AtomicInteger();

        static void reset() {
            BIND_CALLS.set(0);
        }

        @BindToRegistry(name = "sharedConfigGreeting")
        public String sharedConfigGreeting() throws InterruptedException {
            BIND_CALLS.incrementAndGet();
            Thread.sleep(75L);
            return "hello-from-registry";
        }

        @Test
        @CitrusTest
        void m1(@CitrusResource TestActionRunner runner) {
            assertBoundBean(runner);
        }

        @Test
        @CitrusTest
        void m2(@CitrusResource TestActionRunner runner) {
            assertBoundBean(runner);
        }

        @Test
        @CitrusTest
        void m3(@CitrusResource TestActionRunner runner) {
            assertBoundBean(runner);
        }

        @Test
        @CitrusTest
        void m4(@CitrusResource TestActionRunner runner) {
            assertBoundBean(runner);
        }

        private void assertBoundBean(TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertEquals(
                    "hello-from-registry",
                    ctx.getReferenceResolver().resolve("sharedConfigGreeting", String.class)));
        }
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    static class SuiteBarrierClassOneSample implements TestActionSupport {

        static final AtomicBoolean SUITE_READY = new AtomicBoolean(false);
        static final AtomicInteger AFTER_SUITE_COUNT = new AtomicInteger();

        static void reset() {
            SUITE_READY.set(false);
            AFTER_SUITE_COUNT.set(0);
        }

        @Test
        @CitrusTest
        void first(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertTrue(SUITE_READY.get(), "beforeSuite must finish before test starts"));
        }

        @Test
        @CitrusTest
        void second(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertTrue(SUITE_READY.get(), "beforeSuite must finish before test starts"));
        }
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    static class SuiteBarrierClassTwoSample implements TestActionSupport {

        @Test
        @CitrusTest
        void first(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertTrue(SuiteBarrierClassOneSample.SUITE_READY.get(),
                    "beforeSuite must finish before second class test starts"));
        }

        @Test
        @CitrusTest
        void second(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertTrue(SuiteBarrierClassOneSample.SUITE_READY.get(),
                    "beforeSuite must finish before second class test starts"));
        }
    }

    @CitrusSpringSupport
    @Execution(ExecutionMode.CONCURRENT)
    @ContextConfiguration(classes = {CitrusSpringConfig.class, ParallelSpringContextASample.ConfigA.class})
    static class ParallelSpringContextASample implements TestActionSupport {

        static volatile CyclicBarrier springBarrier = new CyclicBarrier(6);

        static void reset() {
            springBarrier = new CyclicBarrier(6);
        }

        @Autowired
        @Qualifier("tenantName")
        private String tenantName;

        @Test
        @CitrusTest
        void tenantAMethod1(@CitrusResource TestActionRunner runner) {
            verifyTenantA(runner);
        }

        @Test
        @CitrusTest
        void tenantAMethod2(@CitrusResource TestActionRunner runner) {
            verifyTenantA(runner);
        }

        private void verifyTenantA(TestActionRunner runner) {
            Assertions.assertEquals("tenant-A", tenantName);
            runner.run(sequential().actions(
                    ctx -> awaitSpringBarrier(),
                    ctx -> {
                        Assertions.assertEquals("tenant-A",
                                ctx.getReferenceResolver().resolve("tenantName", String.class));
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantB"));
                    }
            ));
        }

        static void awaitSpringBarrier() {
            try {
                springBarrier.await(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Configuration
        static class ConfigA {
            @Bean("tenantName")
            public String tenantName() {
                return "tenant-A";
            }

            @Bean("onlyInTenantA")
            public String onlyInTenantA() {
                return "A";
            }
        }
    }

    @CitrusSpringSupport
    @Execution(ExecutionMode.CONCURRENT)
    @ContextConfiguration(classes = {CitrusSpringConfig.class, ParallelSpringContextBSample.ConfigB.class})
    static class ParallelSpringContextBSample implements TestActionSupport {

        @Autowired
        @Qualifier("tenantName")
        private String tenantName;

        @Test
        @CitrusTest
        void tenantBMethod1(@CitrusResource TestActionRunner runner) {
            verifyTenantB(runner);
        }

        @Test
        @CitrusTest
        void tenantBMethod2(@CitrusResource TestActionRunner runner) {
            verifyTenantB(runner);
        }

        private void verifyTenantB(TestActionRunner runner) {
            Assertions.assertEquals("tenant-B", tenantName);
            runner.run(sequential().actions(
                    ctx -> ParallelSpringContextASample.awaitSpringBarrier(),
                    ctx -> {
                        Assertions.assertEquals("tenant-B",
                                ctx.getReferenceResolver().resolve("tenantName", String.class));
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantA"));
                    }
            ));
        }

        @Configuration
        static class ConfigB {
            @Bean("tenantName")
            public String tenantName() {
                return "tenant-B";
            }

            @Bean("onlyInTenantB")
            public String onlyInTenantB() {
                return "B";
            }
        }
    }

    @CitrusSupport
    @Execution(ExecutionMode.CONCURRENT)
    static class ParallelNonSpringAlongsideSpringSample implements TestActionSupport {

        @Test
        @CitrusTest
        void nonSpring1(@CitrusResource TestActionRunner runner) {
            runner.run(sequential().actions(
                    ctx -> ParallelSpringContextASample.awaitSpringBarrier(),
                    ctx -> {
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantA"));
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantB"));
                    }
            ));
        }

        @Test
        @CitrusTest
        void nonSpring2(@CitrusResource TestActionRunner runner) {
            runner.run(sequential().actions(
                    ctx -> ParallelSpringContextASample.awaitSpringBarrier(),
                    ctx -> {
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantA"));
                        Assertions.assertFalse(ctx.getReferenceResolver().isResolvable("onlyInTenantB"));
                    }
            ));
        }
    }

    @CitrusSupport
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Execution(ExecutionMode.CONCURRENT)
    static class PerClassParameterInjectionSample implements TestActionSupport {

        static volatile CyclicBarrier barrier = new CyclicBarrier(3);

        static void reset() {
            barrier = new CyclicBarrier(3);
        }

        @CitrusFrameworkInjectedHolder
        @org.citrusframework.annotations.CitrusFramework
        private Citrus citrus;

        @Test
        @CitrusTest
        void m1(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            Assertions.assertNotNull(citrus);
            runIsolated(runner, context, "v1");
        }

        @Test
        @CitrusTest
        void m2(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            Assertions.assertNotNull(citrus);
            runIsolated(runner, context, "v2");
        }

        @Test
        @CitrusTest
        void m3(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
            Assertions.assertNotNull(citrus);
            runIsolated(runner, context, "v3");
        }

        private void runIsolated(TestActionRunner runner, TestContext context, String value) {
            runner.run(sequential().actions(
                    createVariable("k", value),
                    action(ctx -> {
                        try {
                            barrier.await(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }),
                    apply(new VerifyPerClassVarBehavior(value))
            ));
            Assertions.assertEquals(value, context.getVariable("k"));
        }

        @interface CitrusFrameworkInjectedHolder {
        }
    }

    static class VerifyPerClassVarBehavior implements TestBehavior, TestActionSupport {
        private final String expected;

        VerifyPerClassVarBehavior(String expected) {
            this.expected = expected;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertEquals(expected, ctx.getVariable("k")));
        }
    }

    @CitrusSupport
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Execution(ExecutionMode.SAME_THREAD)
    static class PerClassSameThreadFieldInjectionSample implements TestActionSupport {

        @CitrusResource
        private TestContext context;

        @CitrusResource
        private TestActionRunner runner;

        @Test
        @CitrusTest
        void first() {
            runner.run(createVariable("onlyInFirst", "ok"));
            Assertions.assertEquals("ok", context.getVariable("onlyInFirst"));
        }

        @Test
        @CitrusTest
        void second() {
            Assertions.assertFalse(context.getVariables().containsKey("onlyInFirst"));
            runner.run(createVariable("onlyInSecond", "ok2"));
            Assertions.assertEquals("ok2", context.getVariable("onlyInSecond"));
        }
    }

    @CitrusSupport
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Execution(ExecutionMode.CONCURRENT)
    static class PerClassConcurrentUnsafeFieldSample implements TestActionSupport {

        static volatile CyclicBarrier barrier = new CyclicBarrier(2);

        static void reset() {
            barrier = new CyclicBarrier(2);
        }

        @CitrusResource
        private TestContext sharedContext;

        @Test
        @CitrusTest
        void first(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> awaitBarrier());
            Assertions.assertNotNull(sharedContext);
        }

        @Test
        @CitrusTest
        void second(@CitrusResource TestActionRunner runner) {
            runner.run(ctx -> awaitBarrier());
            Assertions.assertNotNull(sharedContext);
        }

        private static void awaitBarrier() {
            try {
                barrier.await(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }
}
