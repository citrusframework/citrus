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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.api.container.AfterSuite;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

import static org.citrusframework.base.annotations.CitrusAnnotations.injectCitrusFramework;
import static org.citrusframework.junit.jupiter.CitrusExtensionHelper.*;

/**
 * JUnit Jupiter extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 * <p>
 * Extension resolves method parameter of type {@link org.citrusframework.context.TestContext}, {@link TestCaseRunner}
 * or {@link org.citrusframework.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 * <p>
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 */
public class CitrusExtension implements BeforeAllCallback, InvocationInterceptor, AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor, TestExecutionExceptionHandler, AfterEachCallback {

    /**
     * Test suite name
     */
    private static final String SUITE_NAME = "citrus-junit-jupiter-suite";
    private static final String SUITE_STATE_KEY = "citrusSuiteState";

    private static final ReentrantLock SUITE_LOCK = new ReentrantLock();
    private static final AtomicBoolean RESET_REQUESTED = new AtomicBoolean(false);
    private static final Map<Class<?>, Boolean> PER_TEST_RESOURCE_FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CitrusExtension.class);

    private static void failTestCaseIfNotDoneYet(ExtensionContext extensionContext, Throwable throwable) {

        if (throwable instanceof TestAbortedException) {
            return;
        }

        var testCase = getTestCase(extensionContext);
        if (testCase == null) {
            // The test case is not available when the failure happened before the test source was
            // loaded - report the original exception instead of failing to mark the test case.
            return;
        }

        var testResult = testCase.getTestResult();

        if (testResult == null || !testResult.isFailed()) {
            testCase.fail(throwable);
        }
    }

    /**
     * Resolves and binds a scoped {@link Citrus} instance (e.g. per Spring ApplicationContext) without overwriting
     * the root default {@link Citrus} instance used by parallel test classes with different contexts.
     */
    public static Citrus resolveScopedCitrus(ExtensionContext extensionContext, Object scopeKey, Supplier<Citrus> creator) {
        SUITE_LOCK.lock();
        try {
            SuiteState state = getOrCreateSuiteState(extensionContext);
            Citrus citrus = state.scopedInstances.get(scopeKey);
            if (citrus == null) {
                citrus = creator.get();
                state.scopedInstances.put(scopeKey, citrus);
                state.registeredInstances.add(citrus);
            }
            setScopedCitrus(citrus, extensionContext);
            return citrus;
        } finally {
            SUITE_LOCK.unlock();
        }
    }

    private static Citrus ensureCitrusInitialized(ExtensionContext extensionContext) {
        SUITE_LOCK.lock();
        try {
            SuiteState state = getOrCreateSuiteState(extensionContext);
            if (!requiresCitrus(extensionContext)) {
                Citrus existing = getCitrus(extensionContext);
                state.registeredInstances.add(existing);
                return existing;
            }

            if (state.defaultCitrus == null) {
                if (CitrusInstanceManager.hasInstance()
                        && state.scopedInstances.containsValue(CitrusInstanceManager.getOrDefault())) {
                    state.defaultCitrus = Citrus.newInstance();
                } else {
                    state.defaultCitrus = CitrusInstanceManager.getOrDefault();
                }
            }

            setCitrus(state.defaultCitrus, extensionContext);
            state.registeredInstances.add(state.defaultCitrus);
            return state.defaultCitrus;
        } finally {
            SUITE_LOCK.unlock();
        }
    }

    private static SuiteState getOrCreateSuiteState(ExtensionContext extensionContext) {
        ExtensionContext.Store rootStore = extensionContext.getRoot().getStore(NAMESPACE);
        SuiteState existing = rootStore.get(SUITE_STATE_KEY, SuiteState.class);
        if (existing == null) {
            existing = new SuiteState();
            rootStore.put(SUITE_STATE_KEY, existing);
            extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("afterSuiteCallback", existing);
        }
        return existing;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        SUITE_LOCK.lock();
        try {
            if (RESET_REQUESTED.compareAndSet(true, false)) {
                extensionContext.getRoot().getStore(NAMESPACE).remove(SUITE_STATE_KEY);
                removeCitrus(extensionContext);
            }
            SuiteState state = getOrCreateSuiteState(extensionContext);
            Citrus citrus = ensureCitrusInitialized(extensionContext);

            if (state.suiteName == null) {
                // Assertion: If the beforeAll callback is called for a test, the annotated tags are currently
                // included by the groups filter of surefire / failsafe or no specific filter is defined and
                // all groups / tags will run anyway.
                state.tags = extensionContext.getTags().toArray(String[]::new);
                state.suiteName = extensionContext.getTestClass()
                        .map(Class::getName)
                        .orElseGet(() -> extensionContext.getTestMethod()
                                .map(meth -> meth.getDeclaringClass().getName() + ":" + meth.getName())
                                .orElse(SUITE_NAME));
            }

            if (state.beforeSuiteCompleted.add(citrus)) {
                citrus.beforeSuite(state.suiteName, state.tags);
            }
        } finally {
            SUITE_LOCK.unlock();
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        failTestCaseIfNotDoneYet(extensionContext, throwable);
        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        extensionContext.getExecutionException()
                .ifPresent(e -> failTestCaseIfNotDoneYet(extensionContext, e));

        ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
        String baseKey = getBaseKey(extensionContext);
        store.remove(baseKey + TestContext.class.getSimpleName());
        store.remove(baseKey + TestCase.class.getSimpleName());

        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof TestListener testListener) {
            testListener.after(getCitrus(extensionContext).getCitrusContext());
        }
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Object testInstance = extensionContext.getRequiredTestInstance();
        Citrus citrus = ensureCitrusInitialized(extensionContext);
        TestContext context = getTestContext(extensionContext);

        injectTestInstance(testInstance, citrus, context, null, extensionContext, 1);

        if (testInstance instanceof TestListener testListener) {
            testListener.before(citrus.getCitrusContext());
        }

        bindCurrentCitrus(citrus);
        try {
            return invocation.proceed();
        } finally {
            clearCurrentCitrus();
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
        executeTestInvocation(invocation, invocationContext, extensionContext, false);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        executeTestInvocation(invocation, invocationContext, extensionContext, true);
    }

    private void executeTestInvocation(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                       ExtensionContext extensionContext, boolean template) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        Citrus citrus = ensureCitrusInitialized(extensionContext);
        TestContext context = getTestContext(extensionContext);
        TestCaseRunner testRunner = getTestRunner(extensionContext);

        AtomicInteger activeCounter = getActiveCounter(extensionContext, testInstance);
        int activeCount = activeCounter.incrementAndGet();
        try {
            injectTestInstance(testInstance, citrus, context, testRunner, extensionContext, activeCount);

            if (testInstance instanceof TestListener testListener) {
                testListener.before(citrus.getCitrusContext());
            }

            TestLoader testLoader = getTestLoader(extensionContext);
            CitrusAnnotations.injectAll(testLoader, citrus, context);
            CitrusAnnotations.injectTestRunner(testLoader, testRunner);

            if (template && isTestSourceMethod(extensionContext.getRequiredTestMethod())) {
                CitrusParameterHelper.injectTestParameters(testLoader, extensionContext.getRequiredTestMethod(),
                        invocationContext.getArguments());
            }

            testLoader.doWithTestCase(testCase -> {
                try {
                    invocation.proceed();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new CitrusRuntimeException("Test failed", e);
                }
            });

            try {
                testLoader.load();
            } catch (TestCaseFailedException e) {
                throw new CitrusJUnitException(e.getMessage());
            }
        } finally {
            activeCounter.decrementAndGet();
        }
    }

    private static AtomicInteger getActiveCounter(ExtensionContext extensionContext, Object testInstance) {
        SUITE_LOCK.lock();
        try {
            SuiteState state = getOrCreateSuiteState(extensionContext);
            return state.activePerInstance.computeIfAbsent(testInstance, k -> new AtomicInteger(0));
        } finally {
            SUITE_LOCK.unlock();
        }
    }

    private static void injectTestInstance(Object testInstance, Citrus citrus, TestContext context,
                                           TestCaseRunner testRunner, ExtensionContext extensionContext, int activeCount) {
        boolean perClass = extensionContext.getTestInstanceLifecycle()
                .orElse(TestInstance.Lifecycle.PER_METHOD) == TestInstance.Lifecycle.PER_CLASS;

        if (!perClass) {
            CitrusAnnotations.injectAll(testInstance, citrus, context);
            if (testRunner != null) {
                CitrusAnnotations.injectTestRunner(testInstance, testRunner);
            }
            return;
        }

        synchronized (testInstance) {
            boolean concurrent = activeCount > 1 || isConcurrentMethodExecution(extensionContext);
            if (concurrent && hasPerTestResourceFields(testInstance.getClass())) {
                throw new CitrusRuntimeException(String.format(
                        "Cannot inject per-test @CitrusResource fields into shared @TestInstance(PER_CLASS) test instance '%s' during concurrent method execution; inject @CitrusResource via test method parameters instead",
                        testInstance.getClass().getName()));
            }

            if (!concurrent) {
                CitrusAnnotations.injectAll(testInstance, citrus, context);
                if (testRunner != null) {
                    CitrusAnnotations.injectTestRunner(testInstance, testRunner);
                }
            } else {
                CitrusAnnotations.injectCitrusFramework(testInstance, citrus);
                CitrusAnnotations.injectCitrusContext(testInstance, citrus.getCitrusContext());
                citrus.getCitrusContext().parseConfiguration(testInstance);
                CitrusAnnotations.injectTestActionBuilder(testInstance);
                SuiteState state;
                SUITE_LOCK.lock();
                try {
                    state = getOrCreateSuiteState(extensionContext);
                } finally {
                    SUITE_LOCK.unlock();
                }
                if (state.endpointsInjectedPerClass.add(testInstance)) {
                    CitrusAnnotations.injectEndpoints(testInstance, context);
                }
            }
        }
    }

    private static boolean hasPerTestResourceFields(Class<?> clazz) {
        return PER_TEST_RESOURCE_FIELD_CACHE.computeIfAbsent(clazz, type -> {
            Class<?> current = type;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(CitrusResource.class)) {
                        Class<?> fieldType = field.getType();
                        if (TestContext.class.isAssignableFrom(fieldType)
                                || TestCaseRunner.class.isAssignableFrom(fieldType)
                                || GherkinTestActionRunner.class.isAssignableFrom(fieldType)
                                || TestActionRunner.class.isAssignableFrom(fieldType)) {
                            return true;
                        }
                    }
                }
                current = current.getSuperclass();
            }
            return false;
        });
    }

    private static boolean isConcurrentMethodExecution(ExtensionContext extensionContext) {
        Optional<Method> method = extensionContext.getTestMethod();
        if (method.isPresent()) {
            Optional<Execution> methodExecution = AnnotationSupport.findAnnotation(method.get(), Execution.class);
            if (methodExecution.isPresent()) {
                return methodExecution.get().value() == ExecutionMode.CONCURRENT;
            }
        }

        Optional<Class<?>> testClass = extensionContext.getTestClass();
        while (testClass.isPresent()) {
            Optional<Execution> classExecution = AnnotationSupport.findAnnotation(testClass.get(), Execution.class);
            if (classExecution.isPresent()) {
                return classExecution.get().value() == ExecutionMode.CONCURRENT;
            }
            testClass = Optional.ofNullable(testClass.get().getEnclosingClass());
        }

        boolean parallelEnabled = extensionContext.getConfigurationParameter("junit.jupiter.execution.parallel.enabled")
                .map(Boolean::parseBoolean)
                .orElse(false);
        if (!parallelEnabled) {
            return false;
        }

        return extensionContext.getConfigurationParameter("junit.jupiter.execution.parallel.mode.default")
                .map(mode -> "concurrent".equalsIgnoreCase(mode.trim()))
                .orElse(false);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        // Runs for every invocation, including failed ones. Without this, a failed invocation of a
        // parameterized test leaves its TestLoader in the store and the following invocations never run.
        clearStoredObjects(extensionContext);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        injectCitrusFramework(testInstance, ensureCitrusInitialized(extensionContext));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        ensureCitrusInitialized(extensionContext);
        return CitrusExtensionHelper.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Reset Citrus extension forcing a new instance of Citrus and execution of before suite.
     */
    public static void reset() {
        SUITE_LOCK.lock();
        try {
            CitrusInstanceManager.reset();
            RESET_REQUESTED.set(true);
        } finally {
            SUITE_LOCK.unlock();
        }
    }

    /**
     * Listener able to perform changes on Citrus context before/after a test.
     */
    public interface TestListener {

        /**
         * Runs tasks on given Citrus context before test.
         */
        default void before(CitrusContext context) {
        }

        /**
         * Runs tasks on given Citrus context after test.
         */
        default void after(CitrusContext context) {
        }
    }

    @SuppressWarnings("deprecation")
    private static final class SuiteState implements ExtensionContext.Store.CloseableResource, AutoCloseable {
        private Citrus defaultCitrus;
        private final Map<Object, Citrus> scopedInstances = new IdentityHashMap<>();
        private final Set<Citrus> registeredInstances = new LinkedHashSet<>();
        private final Set<Citrus> beforeSuiteCompleted = new LinkedHashSet<>();
        private final Map<Object, AtomicInteger> activePerInstance = new IdentityHashMap<>();
        private final Set<Object> endpointsInjectedPerClass = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
        private String suiteName;
        private String[] tags = new String[0];
        private boolean closed;

        @Override
        public void close() {
            SUITE_LOCK.lock();
            try {
                if (closed) {
                    return;
                }
                closed = true;

                if (beforeSuiteCompleted.isEmpty()) {
                    return;
                }

                List<Citrus> completed = new ArrayList<>(beforeSuiteCompleted);
                Citrus primary = completed.get(completed.size() - 1);
                String effectiveSuiteName = suiteName != null ? suiteName : SUITE_NAME;

                for (int i = 0; i < completed.size() - 1; i++) {
                    Citrus secondary = completed.get(i);
                    secondary.getCitrusContext().getTestSuiteListeners().onFinish();
                    for (AfterSuite sequenceAfterSuite : secondary.getCitrusContext().getAfterSuite()) {
                        try {
                            if (sequenceAfterSuite.shouldExecute(effectiveSuiteName, tags)) {
                                sequenceAfterSuite.execute(secondary.getCitrusContext().createTestContext());
                            }
                        } catch (Exception e) {
                            secondary.getCitrusContext().getTestSuiteListeners().onFinishFailure(e);
                            throw new AssertionError("After suite failed with errors", e);
                        }
                    }
                    primary.getCitrusContext().handleTestResults(secondary.getCitrusContext().getTestResults());
                }

                primary.afterSuite(effectiveSuiteName, tags);
            } finally {
                SUITE_LOCK.unlock();
            }
        }
    }
}
