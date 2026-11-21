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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.common.MultiTestRun;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the test cases of a multi-test source one at a time. The shared declarations and beforeAll run once on
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

    private final List<TestCase> testCases = new ArrayList<>();

    private boolean started;
    private boolean closed;
    private Throwable beforeAllError;

    /**
     * Constructor with the first parse of the source and the parser used for each test case.
     * @param definition the first, validated parse providing names, shared declarations, beforeAll and afterAll.
     * @param parser supplies a new parse of the source for each test case.
     * @param fileContext the test context of the source: shared declarations and the hooks beforeAll/afterAll run on it.
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
        test.setSharedVariables(sharedState());

        TestContext context = citrusContext.createTestContext();
        injectReferenceResolver(test.getActionBuilders(), context.getReferenceResolver());
        injectReferenceResolver(parsed.getAfterEach(), context.getReferenceResolver());

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
     * Runs the shared declarations and beforeAll on the file context. A failure is kept and reported for each
     * enabled test case.
     */
    private void runBeforeAll() {
        logger.info("Running beforeAll of '{}'", name);

        DefaultTestCase shared = definition.getShared();
        prepare(shared);

        try {
            shared.initializeTestResources(fileContext);
            injectReferenceResolver(definition.getBeforeAll(), fileContext.getReferenceResolver());
            executeActions(definition.getBeforeAll(), fileContext);
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
     * Reports the given test case as failed without running it, because the shared declarations or beforeAll failed.
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

    /**
     * Snapshot of the file context variables, without the variables that identify the running test.
     */
    private Map<String, Object> sharedState() {
        Map<String, Object> variables = new LinkedHashMap<>(fileContext.getVariables());
        variables.remove(CitrusSettings.getTestNameVariable());
        variables.remove(CitrusSettings.getTestPackageVariable());
        return variables;
    }

    private static boolean isDisabled(TestCase test) {
        return TestCaseMetaInfo.Status.DISABLED.equals(test.getMetaInfo().getStatus());
    }

    /**
     * Builds and executes the given actions in order. Errors the actions only add to the context fail as well.
     */
    private static void executeActions(List<TestActionBuilder<?>> builders, TestContext context) {
        for (TestActionBuilder<?> builder : builders) {
            TestAction action = builder.build();
            if (!action.isDisabled(context)) {
                action.execute(context);
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
}
