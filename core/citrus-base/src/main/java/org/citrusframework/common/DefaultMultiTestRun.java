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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusSettings;
import org.citrusframework.Completable;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.common.MultiTestRun;
import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.base.endpoint.DefaultEndpointFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the test cases of a multi-test source one at a time. beforeAll runs once on
 * the file context, before the first enabled test case. Each test case runs as a normal {@link DefaultTestCase}
 * with its own test context, with beforeEach in front of its actions and afterEach behind its finally actions.
 * afterAll and the cleanups registered on the file context run on {@link #close()}.
 * <p>
 * Each test case is taken from a new parse of the source, so action builders are never built twice.
 *
 * @since 5.3
 */
public class DefaultMultiTestRun implements MultiTestRun {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultMultiTestRun.class);

    private final String name;
    private final MultiTestDefinition definition;
    private final Supplier<MultiTestDefinition> parser;
    private final TestContext fileContext;
    private final CitrusContext citrusContext;
    private final Class<?> testClass;
    private final String packageName;
    private final List<Consumer<TestCase>> configurers;
    private final ReferenceResolver parentResolver;
    private final RunReferenceResolver runResolver;
    private final EndpointFactory parentEndpointFactory;
    private final EndpointFactory runEndpointFactory;

    private final List<TestCase> testCases = new ArrayList<>();

    private boolean started;
    private boolean closed;
    private Throwable beforeAllError;
    private Map<String, Object> fileVariables;

    /**
     * Constructor with the first parse of the source and the parser used for each test case.
     * @param definition the first, validated parse providing names, beforeAll and afterAll.
     * @param parser supplies a new parse of the source for each test case.
     * @param fileContext the test context on which beforeAll and afterAll run.
     * @param citrusContext the Citrus context creating a new test context per test case.
     * @param testClass the test class reported for each test case.
     * @param packageName the package name reported for each test case.
     * @param configurers the test case configurers of the loader, applied to the shared declarations and each test case.
     */
    public DefaultMultiTestRun(MultiTestDefinition definition, Supplier<MultiTestDefinition> parser,
                               TestContext fileContext, CitrusContext citrusContext,
                               Class<?> testClass, String packageName, List<Consumer<TestCase>> configurers) {
        this.definition = definition;
        this.name = definition.getName();
        this.parser = parser;
        this.fileContext = fileContext;
        this.citrusContext = citrusContext;
        this.testClass = testClass;
        this.packageName = packageName;
        this.configurers = List.copyOf(configurers);
        this.parentResolver = fileContext.getReferenceResolver();
        this.runResolver = new RunReferenceResolver(parentResolver);
        this.parentEndpointFactory = fileContext.getEndpointFactory();
        this.runEndpointFactory = parentEndpointFactory != null && parentEndpointFactory.getClass() == DefaultEndpointFactory.class
                ? new DefaultEndpointFactory() {
                    @Override
                    public void destroyEndpoint(Endpoint endpoint) {
                        runResolver.destroyOwned(endpoint);
                    }
                } : parentEndpointFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getTestNames() {
        return definition.getTestNames();
    }

    @Override
    public synchronized TestCase runTest(int index) {
        if (closed) {
            throw new CitrusRuntimeException("Multi-test run of '%s' is already closed".formatted(name));
        }

        DefaultTestCase declared = definition.getTests().get(index);
        if (isDisabled(declared)) {
            return runDisabled(declared);
        }

        if (!started) {
            started = true;
            runBeforeAll();
        }

        if (beforeAllError != null) {
            throw reportNotRun(declared);
        }

        MultiTestDefinition parsed = parser.get();
        DefaultTestCase test = parsed.getTests().get(index);
        test.getActionBuilders().addAll(0, parsed.getBeforeEach());
        parsed.getAfterEach().forEach(test::addFinalAction);

        prepare(test);
        test.setInheritedVariables(fileVariables);

        TestContext context = citrusContext.createTestContext();
        context.setReferenceResolver(runResolver);
        context.setEndpointFactory(runEndpointFactory);
        injectReferenceResolver(test.getActionBuilders(), context.getReferenceResolver());
        injectReferenceResolver(test.getFinalActionBuilders(), context.getReferenceResolver());

        testCases.add(test);
        TestCaseFailedException failure = null;
        try {
            test.execute(context);
        } catch (TestCaseFailedException e) {
            failure = e;
        } finally {
            try {
                test.finish(context);
            } catch (TestCaseFailedException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }

        if (failure != null) {
            throw failure;
        }

        return test;
    }

    @Override
    public synchronized List<TestCase> getTestCases() {
        return Collections.unmodifiableList(new ArrayList<>(testCases));
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        if (!started) {
            return;
        }

        logger.info("Running afterAll of '{}'", name);

        List<Throwable> errors = new ArrayList<>();
        try {
            injectReferenceResolver(definition.getAfterAll(), fileContext.getReferenceResolver());
            executeActions(definition.getAfterAll(), fileContext);
        } catch (Exception | Error e) {
            errors.add(e);
        }

        for (TestActionBuilder<?> cleanup : fileContext.getFinalActions()) {
            try {
                executeActions(List.of(cleanup), fileContext);
            } catch (Exception | Error e) {
                errors.add(e);
            }
        }

        try {
            if (runEndpointFactory != parentEndpointFactory) {
                runEndpointFactory.destroy();
            }
        } catch (Exception | Error e) {
            errors.add(e);
        }
        try {
            runResolver.destroy();
        } catch (Exception | Error e) {
            errors.add(e);
        } finally {
            fileContext.setReferenceResolver(parentResolver);
            fileContext.setEndpointFactory(parentEndpointFactory);
        }

        if (errors.isEmpty()) {
            logger.info("Finished afterAll of '{}'", name);
            return;
        }

        CitrusRuntimeException failure = new CitrusRuntimeException("afterAll of '%s' failed".formatted(name), errors.get(0));
        errors.stream().skip(1).forEach(failure::addSuppressed);
        logger.error(failure.getMessage(), errors.get(0));

        DefaultTestCase afterAllResult = new DefaultTestCase();
        afterAllResult.setName(name + ".afterAll");
        identify(afterAllResult);
        reportFailed(afterAllResult, failure, fileContext.getTestListeners());

        throw failure;
    }

    /**
     * Runs beforeAll on the file context. A failure is kept and reported for each
     * enabled test case.
     */
    private void runBeforeAll() {
        logger.info("Running beforeAll of '{}'", name);

        try {
            fileContext.setReferenceResolver(runResolver);
            fileContext.setEndpointFactory(runEndpointFactory);
            DefaultTestCase file = new DefaultTestCase();
            file.setName(name);
            prepare(file);

            fileContext.setVariable(CitrusSettings.getTestNameVariable(), file.getName());
            fileContext.setVariable(CitrusSettings.getTestPackageVariable(), file.getPackageName());
            file.getParameters().forEach(fileContext::setVariable);

            injectReferenceResolver(definition.getBeforeAll(), fileContext.getReferenceResolver());
            executeActions(definition.getBeforeAll(), fileContext);
            fileVariables = new LinkedHashMap<>(fileContext.getVariables());
            fileVariables.remove(CitrusSettings.getTestNameVariable());
            fileVariables.remove(CitrusSettings.getTestPackageVariable());
            logger.info("Finished beforeAll of '{}'", name);
        } catch (Exception | Error e) {
            beforeAllError = e;
            logger.error("beforeAll of '{}' failed", name, e);
        }
    }

    /**
     * Runs a disabled test case as a normal test case run does: it is reported as skipped.
     */
    private TestCase runDisabled(DefaultTestCase test) {
        prepare(test);
        testCases.add(test);
        test.execute(citrusContext.createTestContext());
        return test;
    }

    /**
     * Reports the given test case as failed without running it, because beforeAll failed.
     */
    private TestCaseFailedException reportNotRun(DefaultTestCase test) {
        prepare(test);

        CitrusRuntimeException error = new CitrusRuntimeException("beforeAll of '%s' failed".formatted(name), beforeAllError);
        reportFailed(test, error, fileContext.getTestListeners());
        return new TestCaseFailedException(error);
    }

    /**
     * Fires the test listener events of a failed test case that did not run any action.
     */
    private void reportFailed(DefaultTestCase test, Throwable error, TestListeners listeners) {
        testCases.add(test);
        listeners.onTestStart(test);
        test.fail(error);
        listeners.onTestExecutionEnd(test);
        listeners.onTestFailure(test, error);
        listeners.onTestFinalization(test);
    }

    /**
     * Sets the test identity and applies the loader's configurers.
     */
    private void prepare(DefaultTestCase test) {
        identify(test);
        configurers.forEach(configurer -> configurer.accept(test));
    }

    private void identify(DefaultTestCase test) {
        if (testClass != null) {
            test.setTestClass(testClass);
        }

        if (packageName != null) {
            test.setPackageName(packageName);
        }
    }

    private static boolean isDisabled(TestCase test) {
        return TestCaseMetaInfo.Status.DISABLED.equals(test.getMetaInfo().getStatus());
    }

    /**
     * Builds and executes the given actions in order. Errors the actions only add to the context fail as well.
     */
    private static void executeActions(List<TestActionBuilder<?>> builders, TestContext context) {
        List<Completable> completion = new ArrayList<>();
        Throwable failure = null;
        try {
            for (TestActionBuilder<?> builder : builders) {
                TestAction action = builder.build();
                if (!action.isDisabled(context)) {
                    if (action instanceof Completable completable) {
                        completion.add(completable);
                    }
                    action.execute(context);
                }
            }
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                TestUtils.waitForCompletion(ctx -> completion.stream().allMatch(action -> action.isDone(ctx)), context);
            } catch (RuntimeException | Error e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }

        if (context.hasExceptions()) {
            CitrusRuntimeException error = context.getExceptions().remove(0);
            context.getExceptions().clear();
            throw error;
        }
    }

    private static void injectReferenceResolver(List<TestActionBuilder<?>> builders, ReferenceResolver referenceResolver) {
        builders.stream()
                .filter(ReferenceResolverAware.class::isInstance)
                .map(ReferenceResolverAware.class::cast)
                .forEach(builder -> builder.setReferenceResolver(referenceResolver));
    }

    /** A run owns new bindings and can still resolve resources registered by the host. */
    private static final class RunReferenceResolver extends SimpleReferenceResolver {

        private final ReferenceResolver parent;
        private final Set<ShutdownPhase> borrowed = Collections.synchronizedSet(
                Collections.newSetFromMap(new IdentityHashMap<>()));
        private final Set<ShutdownPhase> destroyed = Collections.synchronizedSet(
                Collections.newSetFromMap(new IdentityHashMap<>()));

        private RunReferenceResolver(ReferenceResolver parent) {
            this.parent = parent;
        }

        @Override
        public <T> T resolve(Class<T> type) {
            return super.isResolvable(type) ? super.resolve(type) : borrow(parent.resolve(type));
        }

        @Override
        public Object resolve(String name) {
            return super.isResolvable(name) ? super.resolve(name) : borrow(parent.resolve(name));
        }

        @Override
        public <T> T resolve(String name, Class<T> type) {
            return super.isResolvable(name) ? super.resolve(name, type) : borrow(parent.resolve(name, type));
        }

        @Override
        public <T> Map<String, T> resolveAll(Class<T> type) {
            Map<String, T> references = new LinkedHashMap<>(super.resolveAll(type));
            parent.resolveAll(type).forEach((name, value) -> {
                if (!super.isResolvable(name)) {
                    references.put(name, borrow(value));
                }
            });
            return references;
        }

        @Override
        public boolean isResolvable(String name) {
            return super.isResolvable(name) || parent.isResolvable(name);
        }

        @Override
        public boolean isResolvable(Class<?> type) {
            return super.isResolvable(type) || parent.isResolvable(type);
        }

        @Override
        public boolean isResolvable(String name, Class<?> type) {
            return super.isResolvable(name) ? super.isResolvable(name, type) : parent.isResolvable(name, type);
        }

        private <T> T borrow(T value) {
            if (value instanceof ShutdownPhase resource) {
                borrowed.add(resource);
            }
            return value;
        }

        private void destroyOwned(Object value) {
            if (value instanceof ShutdownPhase resource && !borrowed.contains(resource) && destroyed.add(resource)) {
                resource.destroy();
            }
        }

        @Override
        public void destroy() {
            CitrusRuntimeException failure = null;
            for (ShutdownPhase resource : super.resolveAll(ShutdownPhase.class).values()) {
                try {
                    destroyOwned(resource);
                } catch (RuntimeException | Error e) {
                    if (failure == null) {
                        failure = new CitrusRuntimeException("Failed to shut down a multi-test resource", e);
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        }
    }
}
