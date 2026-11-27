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

package org.citrusframework.base.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionResult;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.api.container.AfterTest;
import org.citrusframework.api.container.BeforeTest;
import org.citrusframework.container.AbstractIteratingActionContainer;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.AbstractTestReporter;
import org.citrusframework.report.TestActionContainerListener;
import org.citrusframework.report.TestActionListener;
import org.citrusframework.report.TestListener;
import org.citrusframework.report.TestResults;
import org.citrusframework.util.json.JsonNodeStringBuilder;
import org.citrusframework.util.json.JsonStringBuilder;
import org.citrusframework.message.MessageAwareTestAction;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.util.yaml.YamlNodeStringBuilder;
import org.citrusframework.util.yaml.YamlStringBuilder;

/**
 * Reporter generates a detailed report of executed tests by capturing its test action execution.
 * Each executed test action is provided with an action path that can be used to identify the test action within the test.
 * Test action containers report their nested test actions and iterating test containers report on the individual iterations executed.
 * Failed test actions hold failure cause information.
 * Test actions that exchange messages with send or receive operations provide message content information.
 * The result is a graph of executed test actions that represents the test flow.
 * Graphical designers may use this test flow graph to visualize the test result in detail.
 * <p>
 * The reporter is shared by all tests, which may run concurrently. Each test case run records into its own report entry,
 * so concurrent runs of tests with the same name do not mix their actions. The report holds one entry per test name:
 * the entry of the run that started or completed most recently.
 */
public class TestFlowReporter extends AbstractTestReporter implements TestListener, TestActionListener,
        TestActionContainerListener, ShutdownPhase {

    private final TestFlowReport flowReport = new TestFlowReport();

    /** Report entry of each test case run that has started and not yet ended */
    private final Map<TestCase, ReportEntry> runs = Collections.synchronizedMap(new WeakHashMap<>());

    private final Set<TestCase> finalActionsTests = ConcurrentHashMap.newKeySet();

    private final ThreadLocal<Deque<BoundaryExecution>> boundaryExecutions = ThreadLocal.withInitial(ArrayDeque::new);

    private final boolean enabled = TestFlowReporterSettings.isReportEnabled();

    private enum BoundaryPhase {
        BEFORE_TEST("beforeTest"),
        AFTER_TEST("afterTest");

        private final String pathPrefix;

        BoundaryPhase(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
    }

    private record BoundaryExecution(TestCase test, BoundaryPhase phase) {
    }

    @Override
    protected void generate(TestResults testResults) {
        testResults.doWithResults(result -> flowReport.get(result.getTestName())
                .ifPresent(reportEntry -> reportEntry.setResult(result)));

        if (enabled && !flowReport.getEntries().isEmpty()
                && (TestFlowReporterSettings.isJsonReport() || TestFlowReporterSettings.isYamlReport())) {
            File targetDirectory = new File(getReportDirectory());
            if (!targetDirectory.exists()) {
                if (!targetDirectory.mkdirs()) {
                    throw new CitrusRuntimeException("Unable to create report output directory: " + getReportDirectory());
                }
            }

            String reportFileSuffix = TestFlowReporterSettings.isHistoryEnabled()
                    ? createReportFileSuffix(targetDirectory, flowReport.getEntries().stream().map(reportEntry -> reportEntry.getName()).toList(), Instant.now().getEpochSecond())
                    : "";

            flowReport.getEntries()
                .forEach(reportEntry -> {
                    if (TestFlowReporterSettings.isJsonReport()) {
                        String jsonContent = MessagePayloadUtils.prettyPrintJson(reportEntry.toJson());
                        writeTestFlowReport(targetDirectory, "%s-flow%s.json".formatted(reportEntry.getName(), reportFileSuffix), jsonContent);
                        if (!reportFileSuffix.isEmpty()) {
                            writeTestFlowReport(targetDirectory, "%s-flow.json".formatted(reportEntry.getName()), jsonContent);
                        }
                    }

                    if (TestFlowReporterSettings.isYamlReport()) {
                        String yamlContent = reportEntry.toYaml();
                        writeTestFlowReport(targetDirectory, "%s-flow%s.yaml".formatted(reportEntry.getName(), reportFileSuffix), yamlContent);
                        if (!reportFileSuffix.isEmpty()) {
                            writeTestFlowReport(targetDirectory, "%s-flow.yaml".formatted(reportEntry.getName()), yamlContent);
                        }
                    }
                });
        }
    }

    /**
     * Create a unique file name suffix for the current report generation. The suffix contains a timestamp
     * and gets a numeric suffix when report files with the timestamp already exist.
     */
    String createReportFileSuffix(File targetDirectory, List<String> reportNames, long timestamp) {
        for (int attempt = 0; attempt < 100; attempt++) {
            String suffix = attempt == 0 ? "-%d".formatted(timestamp) : "-%d-%d".formatted(timestamp, attempt);

            if (!reportFilesExist(targetDirectory, reportNames, suffix)) {
                return suffix;
            }
        }

        throw new CitrusRuntimeException("Unable to determine unique flow report file names for report directory: " + targetDirectory);
    }

    private boolean reportFilesExist(File targetDirectory, List<String> reportNames, String suffix) {
        return reportNames.stream()
                .anyMatch(reportName -> {
                    if (TestFlowReporterSettings.isJsonReport() && new File(targetDirectory, "%s-flow%s.json".formatted(reportName, suffix)).exists()) {
                        return true;
                    }

                    return TestFlowReporterSettings.isYamlReport() && new File(targetDirectory, "%s-flow%s.yaml".formatted(reportName, suffix)).exists();
                });
    }

    private void writeTestFlowReport(File targetDirectory, String fileName, String content) {
        try (Writer fileWriter = new FileWriter(new File(targetDirectory, fileName))) {
            fileWriter.append(content);
            fileWriter.flush();
            logger.debug("Generated test report: {}{}{}", targetDirectory, File.separator, fileName);
        } catch (IOException e) {
            logger.error("Failed to create test report", e);
        }
    }

    @Override
    public void onTestActionStart(TestCase test, TestAction testAction) {
    }

    @Override
    public void onTestActionFinish(TestCase test, TestAction testAction) {
        recordTestAction(test, testAction, null);
    }

    @Override
    public void onTestActionFailed(TestCase test, TestAction testAction, Throwable cause) {
        recordTestAction(test, testAction, Optional.ofNullable(cause.getMessage()).orElse(""));
    }

    private void recordTestAction(TestCase test, TestAction testAction, String error) {
        boolean finalAction = finalActionsTests.contains(test);
        ReportEntry reportEntry = getReportEntry(test);
        String actionPath = finalAction
                ? "finally.%d.%s".formatted(reportEntry.getFinalActions().size(), testAction.getName())
                : getActionPath(test, testAction);
        TestActionResult tar = createTestActionResult(testAction, actionPath, error);

        if (finalAction) {
            reportEntry.addFinalAction(tar);
        } else {
            reportEntry.addAction(tar);
        }
    }

    private void recordBoundaryAction(TestActionContainer container, TestAction action, String error) {
        BoundaryPhase phase = resolveBoundaryPhase(container);
        if (phase == null) {
            return;
        }

        TestCase test = findActiveBoundaryTest(phase);
        if (test == null) {
            return;
        }

        ReportEntry reportEntry = getReportEntry(test);
        List<TestActionResult> target = phase == BoundaryPhase.BEFORE_TEST
                ? reportEntry.getBeforeTestActions()
                : reportEntry.getAfterTestActions();
        synchronized (target) {
            String actionPath = "%s.%d.%s".formatted(phase.pathPrefix, target.size(), action.getName());
            target.add(createTestActionResult(action, actionPath, error));
        }
    }

    private TestActionResult createTestActionResult(TestAction testAction, String actionPath, String error) {
        TestActionResult tar = new TestActionResult(testAction.getName(), actionPath);

        if (testAction instanceof MessageAwareTestAction messageAware) {
            messageAware.getMessage().ifPresent(tar::setMessage);
        }

        if (error != null) {
            tar.setError(error);
        }

        if (testAction instanceof TestActionContainer actionContainer) {
            addNestedTestActions(actionPath, actionContainer, tar);
        }

        return tar;
    }

    private void addNestedTestActions(String actionPath, TestActionContainer container, TestActionResult tar) {
        if (container instanceof AbstractIteratingActionContainer iteratingActionContainer) {
            int iterations = iteratingActionContainer.getIterations();
            int actionsSize = iteratingActionContainer.getActions().size();
            for (int iteration = 0; iteration < iterations; iteration++) {
                TestActionResult iterationResult = new TestActionResult(String.valueOf(iteration), actionPath);

                List<TestAction> executed = container.getExecutedActions();
                for (int i = iteration * actionsSize; i < executed.size()
                        && i < ((iteration + 1) * actionsSize); i++) {
                    TestAction action = executed.get(i);
                    String nestedActionPath;
                    if (iteration == 0) {
                        nestedActionPath = actionPath + "." + getActionPath(container, action);
                    } else {
                        nestedActionPath = actionPath + "." + getActionPath(container, action, iteration * actionsSize);
                    }

                    iterationResult.addAction(createTestActionResult(action, nestedActionPath, null));
                }

                tar.addIteration(iterationResult);
            }
        } else {
            List<TestAction> executed = container.getExecutedActions();
            for (TestAction action : executed) {
                tar.addAction(createTestActionResult(action, actionPath + "." + getActionPath(container, action), null));
            }
        }
    }

    @Override
    public void onTestActionSkipped(TestCase test, TestAction testAction) {
    }

    @Override
    public void onNestedActionSuccess(TestActionContainer container, TestAction action, TestContext context) {
        recordBoundaryAction(container, action, null);
    }

    @Override
    public void onNestedActionFailed(TestActionContainer container, TestAction action, TestContext context, Throwable cause) {
        recordBoundaryAction(container, action, cause != null ? Optional.ofNullable(cause.getMessage()).orElse("") : "");
    }

    @Override
    public void onBeforeTestStart(TestCase test) {
        boundaryExecutions.get().push(new BoundaryExecution(test, BoundaryPhase.BEFORE_TEST));
    }

    @Override
    public void onBeforeTestEnd(TestCase test) {
        clearBoundaryExecution(test, BoundaryPhase.BEFORE_TEST);
    }

    @Override
    public void onAfterTestStart(TestCase test) {
        boundaryExecutions.get().push(new BoundaryExecution(test, BoundaryPhase.AFTER_TEST));
    }

    @Override
    public void onAfterTestEnd(TestCase test) {
        clearBoundaryExecution(test, BoundaryPhase.AFTER_TEST);
    }

    @Override
    public void onFinalActionsStart(TestCase test) {
        finalActionsTests.add(test);
    }

    @Override
    public void onFinalActionsEnd(TestCase test) {
        finalActionsTests.remove(test);
    }

    private static BoundaryPhase resolveBoundaryPhase(TestActionContainer container) {
        if (container instanceof BeforeTest) {
            return BoundaryPhase.BEFORE_TEST;
        }
        if (container instanceof AfterTest) {
            return BoundaryPhase.AFTER_TEST;
        }
        return null;
    }

    private TestCase findActiveBoundaryTest(BoundaryPhase phase) {
        Deque<BoundaryExecution> stack = boundaryExecutions.get();
        for (BoundaryExecution execution : stack) {
            if (execution.phase() == phase) {
                return execution.test();
            }
        }
        if (stack.isEmpty()) {
            boundaryExecutions.remove();
        }
        return null;
    }

    private void clearBoundaryExecution(TestCase test, BoundaryPhase phase) {
        Deque<BoundaryExecution> stack = boundaryExecutions.get();
        stack.removeIf(entry -> Objects.equals(entry.test(), test) && entry.phase() == phase);
        if (stack.isEmpty()) {
            boundaryExecutions.remove();
        }
    }

    /**
     * Get the report entry of the given test case run. The entry is added to the report when no entry with the test name exists yet.
     * Falls back to the entry with the test name for tests that did not report a test start.
     */
    private ReportEntry getReportEntry(TestCase test) {
        ReportEntry reportEntry = runs.get(test);
        if (reportEntry == null) {
            return flowReport.getOrCreateNew(test.getName());
        }

        flowReport.entries.putIfAbsent(test.getName(), reportEntry);
        return reportEntry;
    }

    private void completeReportEntry(TestCase test) {
        if (test.getTestResult() != null) {
            ReportEntry reportEntry = getReportEntry(test);
            reportEntry.setResult(test.getTestResult());
            flowReport.add(test.getName(), reportEntry);
        }
    }

    private static String getActionPath(TestActionContainer container, TestAction testAction) {
        return "actions.%d.%s".formatted(container.getActionIndex(testAction), testAction.getName());
    }

    private static String getActionPath(TestActionContainer container, TestAction testAction, int iterationIndex) {
        return "actions.%d.%s".formatted(container.getActionIndex(testAction) % iterationIndex, testAction.getName());
    }

    @Override
    public void onTestStart(TestCase test) {
        ReportEntry reportEntry = new ReportEntry(test.getName());
        runs.put(test, reportEntry);
        // Replace the report of a previous run with the same name
        flowReport.entries.replace(test.getName(), reportEntry);
    }

    @Override
    public void onTestSuccess(TestCase test) {
        completeReportEntry(test);
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        completeReportEntry(test);
    }

    @Override
    public void onTestEnd(TestCase test) {
        runs.remove(test);
        finalActionsTests.remove(test);
        Deque<BoundaryExecution> stack = boundaryExecutions.get();
        stack.removeIf(entry -> Objects.equals(entry.test(), test));
        if (stack.isEmpty()) {
            boundaryExecutions.remove();
        }
    }

    @Override
    public void onTestSkipped(TestCase test) {
    }

    public String getJsonReport() {
        return MessagePayloadUtils.prettyPrintJson(flowReport.toJson());
    }

    public String getYamlReport() {
        return flowReport.toYaml();
    }

    TestFlowReport getFlowReport() {
        return flowReport;
    }

    @Override
    public void destroy() {
        clear();
    }

    public void clear() {
        flowReport.clear();
        runs.clear();
        finalActionsTests.clear();
        boundaryExecutions.remove();
    }

    public static class TestFlowReport {

        private final Map<String, ReportEntry> entries = new ConcurrentHashMap<>();

        public void add(String name, ReportEntry entry) {
            entries.put(name, entry);
        }

        public Optional<ReportEntry> get(String name) {
            return Optional.ofNullable(entries.get(name));
        }

        public ReportEntry getOrCreateNew(String name) {
            return entries.computeIfAbsent(name, ReportEntry::new);
        }

        /**
         * Replace the report entry with the given name by a new empty entry.
         * @deprecated no longer used by the reporter, which creates a new report entry for each test run on test start.
         */
        @Deprecated(since = "5.3.0")
        public void reset(String name) {
            entries.put(name, new ReportEntry(name));
        }

        public List<ReportEntry> getEntries() {
            return new ArrayList<>(entries.values());
        }

        public String toJson() {
            JsonNodeStringBuilder builder = new JsonStringBuilder().withArray();

            entries.values()
                    .stream()
                    .map(ReportEntry::toJson)
                    .forEach(builder::append);

            return builder.closeArray().toString();
        }

        public String toYaml() {
            YamlNodeStringBuilder builder = new YamlStringBuilder(2).withArray();

            entries.values()
                    .stream()
                    .map(ReportEntry::toYaml)
                    .forEach(builder::append);

           return builder.closeArray().toString();
        }

        public void clear() {
            entries.clear();
        }
    }

    /**
     * Report entry of a single test run. Its action lists are safe to use from several threads.
     */
    public static class ReportEntry {
        private final String name;
        private volatile TestResult result;
        private final List<TestActionResult> beforeTestActions = Collections.synchronizedList(new ArrayList<>());
        private final List<TestActionResult> actions = Collections.synchronizedList(new ArrayList<>());
        private final List<TestActionResult> finalActions = Collections.synchronizedList(new ArrayList<>());
        private final List<TestActionResult> afterTestActions = Collections.synchronizedList(new ArrayList<>());

        public ReportEntry(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public TestResult getResult() {
            return result;
        }

        public void setResult(TestResult result) {
            this.result = result;
        }

        public List<TestActionResult> getBeforeTestActions() {
            return beforeTestActions;
        }

        public void addBeforeTestAction(TestActionResult action) {
            this.beforeTestActions.add(action);
        }

        public List<TestActionResult> getActions() {
            return actions;
        }

        public void addAction(TestActionResult action) {
            this.actions.add(action);
        }

        public List<TestActionResult> getFinalActions() {
            return finalActions;
        }

        public void addFinalAction(TestActionResult action) {
            this.finalActions.add(action);
        }

        public List<TestActionResult> getAfterTestActions() {
            return afterTestActions;
        }

        public void addAfterTestAction(TestActionResult action) {
            this.afterTestActions.add(action);
        }

        public String toJson() {
            List<TestActionResult> beforeTest = List.copyOf(this.beforeTestActions);
            List<TestActionResult> actions = List.copyOf(this.actions);
            List<TestActionResult> finalActions = List.copyOf(this.finalActions);
            List<TestActionResult> afterTest = List.copyOf(this.afterTestActions);
            var builder = new JsonStringBuilder()
                .withObject()
                    .withProperty("name", name)
                    .withProperty("result").append(Optional.ofNullable(result).map(TestResult::toJson).orElse("{}"));

            if (!beforeTest.isEmpty()) {
                appendJsonActionsProperty(builder, "beforeTest", beforeTest);
            }

            appendJsonActionsProperty(builder, "actions", actions);

            if (!finalActions.isEmpty()) {
                appendJsonActionsProperty(builder, "finally", finalActions);
            }

            if (!afterTest.isEmpty()) {
                appendJsonActionsProperty(builder, "afterTest", afterTest);
            }

            return builder.closeObject().toString();
        }

        public String toYaml() {
            List<TestActionResult> beforeTest = List.copyOf(this.beforeTestActions);
            List<TestActionResult> actions = List.copyOf(this.actions);
            List<TestActionResult> finalActions = List.copyOf(this.finalActions);
            List<TestActionResult> afterTest = List.copyOf(this.afterTestActions);
            var builder = new YamlStringBuilder()
                .withProperty("name", name)
                .withObject("result").append(Optional.ofNullable(result).map(TestResult::toYaml).orElse("{}")).closeObject();

            if (!beforeTest.isEmpty()) {
                appendYamlActionsProperty(builder, "beforeTest", beforeTest);
            }

            appendYamlActionsProperty(builder, "actions", actions);

            if (!finalActions.isEmpty()) {
                appendYamlActionsProperty(builder, "finally", finalActions);
            }

            if (!afterTest.isEmpty()) {
                appendYamlActionsProperty(builder, "afterTest", afterTest);
            }

            return builder.toString();
        }

        private static void appendJsonActionsProperty(JsonNodeStringBuilder builder, String propertyName, List<TestActionResult> actionResults) {
            builder.withProperty(propertyName)
                    .withArray()
                        .append(actionResults.stream().map(TestActionResult::toJson).collect(Collectors.joining(",")))
                    .closeArray();
        }

        private static void appendYamlActionsProperty(YamlNodeStringBuilder builder, String propertyName, List<TestActionResult> actionResults) {
            builder.withObject(propertyName)
                .withArray()
                    .append(actionResults.stream().map(TestActionResult::toYaml).toList())
                .closeArray()
                .closeObject();
        }
    }
}
