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

package org.citrusframework.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.api.common.MultiTestRun;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("CitrusInstanceManager publishes a shared static Citrus instance")
class MultiTestYamlTest {

    private static final String BASE = "classpath:org/citrusframework/yaml/multi/";

    private CitrusContext citrusContext;
    private Citrus citrus;
    private List<String> steps;

    @BeforeEach
    void setup() {
        citrusContext = CitrusContext.create();
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);
        steps = Collections.synchronizedList(new ArrayList<>());

        for (String step : List.of("beforeAll", "beforeEach", "afterEach", "afterAll", "a", "b", "c")) {
            bind(step, context -> steps.add(step));
        }
        bind("recordUser", context -> steps.add("user=" + context.getVariable("user")));
        bind("recordBrokerUrl", context -> steps.add("brokerUrl=" + context.getVariable("brokerUrl")));
        bind("recordOrderIdKnown", context -> steps.add("orderIdKnown=" + context.getVariables().containsKey("orderId")));
        bind("recordSharedBean", context -> steps.add("sharedBean=" + context.getReferenceResolver().isResolvable("sharedBean")));
        bind("failWhenBroken", context -> {
            if ("broken".equals(context.getVariable("mode"))) {
                throw new CitrusRuntimeException("broken");
            }
        });
    }

    @Test
    void shouldRunHooksAroundEveryTest() {
        YamlTestLoader loader = loader("hooks");

        loader.load();

        Assertions.assertEquals(List.of("beforeAll", "beforeEach", "a", "afterEach", "beforeEach", "b", "afterEach",
                "beforeEach", "c", "afterEach", "afterAll"), steps);
        Assertions.assertEquals(List.of("a", "b", "c"), names(loader.getTestCases()));
        Assertions.assertEquals(List.of("a", "b", "c"),
                citrusContext.getTestResults().asList().stream().map(TestResult::getTestName).toList());
        Assertions.assertEquals("First test", loader.getTestCases().get(0).getDescription());
        for (TestCase testCase : loader.getTestCases()) {
            Assertions.assertEquals(MultiTestYamlTest.class, testCase.getTestClass());
            Assertions.assertEquals(MultiTestYamlTest.class.getPackageName(), testCase.getPackageName());
        }
    }

    @Test
    void shouldAcceptKebabCaseHooks() {
        loader("kebab-case-hooks").load();

        Assertions.assertEquals(List.of("beforeAll", "beforeEach", "a", "afterEach", "afterAll"), steps);
    }

    @Test
    void shouldShareVariablesAndBeans() {
        loader("variables").load();

        Assertions.assertEquals(List.of("user=alice", "brokerUrl=tcp://localhost:61616",
                "user=bob", "orderIdKnown=false",
                "user=alice", "sharedBean=true"), steps);
    }

    @Test
    void shouldFailEveryTestWhenBeforeAllFails() {
        YamlTestLoader loader = loader("before-all-fails");

        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals("2 of 2 tests in 'beforeAllFails' failed: a, b", error.getMessage());
        Assertions.assertEquals(List.of("afterAll"), steps);
        Assertions.assertEquals(2, citrusContext.getTestResults().getFailed());
        Assertions.assertEquals("beforeAll of 'beforeAllFails' failed",
                loader.getTestCases().get(0).getTestResult().getCause().getMessage());
    }

    @Test
    void shouldFailOnlyTheTestWhoseBeforeEachFails() {
        YamlTestLoader loader = loader("before-each-fails");

        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals("1 of 3 tests in 'beforeEachFails' failed: b", error.getMessage());
        Assertions.assertEquals(List.of("a", "afterEach", "afterEach", "c", "afterEach"), steps);
        Assertions.assertEquals(2, citrusContext.getTestResults().getSuccess());
    }

    @Test
    void shouldReportFailingAfterAll() {
        YamlTestLoader loader = loader("after-all-fails");

        TestCaseFailedException error = Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals("1 of 2 tests in 'afterAllFails' failed: afterAllFails.afterAll", error.getMessage());
        Assertions.assertEquals(List.of("a", "b", "afterAllFails.afterAll"), names(loader.getTestCases()));
        Assertions.assertEquals(2, citrusContext.getTestResults().getSuccess());
        Assertions.assertEquals(1, citrusContext.getTestResults().getFailed());
    }

    @Test
    void shouldSkipDisabledTest() {
        YamlTestLoader loader = loader("disabled");

        loader.load();

        Assertions.assertEquals(List.of("beforeEach", "a", "afterEach"), steps);
        Assertions.assertTrue(loader.getTestCases().get(1).getTestResult().isSkipped());
    }

    @Test
    void shouldNotRunSharedSetupWhenAllTestsAreDisabled() {
        YamlTestLoader loader = loader("all-disabled");

        loader.load();

        Assertions.assertEquals(List.of(), steps);
    }

    @Test
    void shouldRejectInvalidFiles() {
        assertRejected("duplicate-names", "Multi-test file 'duplicates' has duplicate test names: ok");
        assertRejected("empty-tests", "Multi-test file 'empty' holds no test");
        assertRejected("missing-name", "Multi-test file 'missingName' has a test without name at position 2");
        assertRejected("tests-with-actions", "Multi-test file 'mixed' must not combine 'tests' with 'actions'");
        assertRejected("tests-with-status", "Multi-test file 'mixedStatus' must not combine 'tests' with 'status'");
        Assertions.assertEquals(List.of(), steps);
    }

    @Test
    void shouldRejectUnknownKeys() {
        Assertions.assertThrows(TestCaseFailedException.class, () -> loader("unknown-key").load());
        Assertions.assertThrows(TestCaseFailedException.class, () -> loader("nested-tests").load());
    }

    @Test
    void shouldReportRejectedFileAsOneFailedTest() {
        YamlTestLoader loader = loader("duplicate-names");

        Assertions.assertThrows(TestCaseFailedException.class, loader::load);

        Assertions.assertEquals(1, citrusContext.getTestResults().getSize());
        Assertions.assertEquals("duplicate-names", citrusContext.getTestResults().asList().get(0).getTestName());
    }

    @Test
    void shouldUseLoaderTestNameWithoutFileName() {
        YamlTestLoader loader = loader("no-name");

        MultiTestRun run = loader.openMultiTest().orElseThrow();

        Assertions.assertEquals("no-name", run.getName());
        Assertions.assertEquals(List.of("a"), run.getTestNames());
    }

    @Test
    void shouldOpenMultiTestRunWithoutRunningIt() {
        YamlTestLoader loader = loader("hooks");

        MultiTestRun run = loader.openMultiTest().orElseThrow();
        Assertions.assertEquals("orders", run.getName());
        Assertions.assertEquals(List.of("a", "b", "c"), run.getTestNames());
        Assertions.assertEquals(List.of(), steps);

        run.runTest(0);
        run.close();
        Assertions.assertEquals(List.of("beforeAll", "beforeEach", "a", "afterEach", "afterAll"), steps);
    }

    @Test
    void shouldKeepSingleTestParseForNextLoad() {
        YamlTestLoader loader = loader("../identity");

        Optional<MultiTestRun> run = loader.openMultiTest();
        Assertions.assertTrue(run.isEmpty());

        loader.setSource(TestSourceHelper.create(BASE + "does-not-exist.citrus.it.yaml"));
        loader.load();

        Assertions.assertEquals("IdentityYamlTest", loader.getTestCase().getName());
        Assertions.assertTrue(loader.getTestCase().getTestResult().isSuccess());
    }

    @Test
    void shouldReceiveOncePerTestInBeforeEach() {
        loader("receive-in-before-each").load();

        Assertions.assertEquals(List.of("a", "b", "c"), steps);
        Assertions.assertEquals(3, citrusContext.getTestResults().getSuccess());
    }

    private void assertRejected(String file, String message) {
        YamlTestLoader loader = loader(file);
        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class, loader::openMultiTest);
        Assertions.assertEquals(message, error.getMessage());
    }

    private YamlTestLoader loader(String file) {
        YamlTestLoader loader = new YamlTestLoader(MultiTestYamlTest.class, file.replace("../", ""),
                MultiTestYamlTest.class.getPackageName());
        TestContext fileContext = citrusContext.createTestContext();
        CitrusAnnotations.injectAll(loader, citrus, fileContext);
        loader.setSource(TestSourceHelper.create(BASE + file + ".citrus.it.yaml"));
        return loader;
    }

    private void bind(String name, TestAction action) {
        citrusContext.getReferenceResolver().bind(name, action);
    }

    private static List<String> names(List<TestCase> testCases) {
        return testCases.stream().map(TestCase::getName).toList();
    }
}
