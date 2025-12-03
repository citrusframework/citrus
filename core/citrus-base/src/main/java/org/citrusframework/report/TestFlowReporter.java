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

package org.citrusframework.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionResult;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.container.AbstractIteratingActionContainer;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.JsonNodeStringBuilder;
import org.citrusframework.json.JsonStringBuilder;
import org.citrusframework.message.MessageAwareTestAction;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.yaml.YamlNodeStringBuilder;
import org.citrusframework.yaml.YamlStringBuilder;

/**
 * Reporter generates a detailed report of executed tests by capturing its test action execution.
 * Each executed test action is provided with an action path that can be used to identify the test action within the test.
 * Test action containers report their nested test actions and iterating test containers report on the individual iterations executed.
 * Failed test actions hold failure cause information.
 * Test actions that exchange messages with send or receive operations provide message content information.
 * The result is a graph of executed test actions that represents the test flow.
 * Graphical designers may use this test flow graph to visualize the test result in detail.
 */
public class TestFlowReporter extends AbstractTestReporter implements TestListener, TestActionListener, ShutdownPhase {

    private final TestFlowReport flowReport = new TestFlowReport();

    private final boolean enabled = TestFlowReporterSettings.isReportEnabled();

    @Override
    protected void generate(TestResults testResults) {
        testResults.doWithResults(result -> {
            if (flowReport.entries.containsKey(result.getTestName())) {
                flowReport.entries.get(result.getTestName()).setResult(result);
            }
        });

        if (enabled) {
            File targetDirectory = new File(getReportDirectory());
            if (!targetDirectory.exists()) {
                if (!targetDirectory.mkdirs()) {
                    throw new CitrusRuntimeException("Unable to create report output directory: " + getReportDirectory());
                }
            }

            flowReport.getEntries()
                .forEach(reportEntry -> {
                    if (TestFlowReporterSettings.isJsonReport()) {
                        writeTestFlowReport(targetDirectory, "%s-flow.json".formatted(reportEntry.getName()), MessagePayloadUtils.prettyPrintJson(reportEntry.toJson()));
                    }

                    if (TestFlowReporterSettings.isYamlReport()) {
                        writeTestFlowReport(targetDirectory, "%s-flow.yaml".formatted(reportEntry.getName()), reportEntry.toYaml());
                    }
                });
        }
    }

    private void writeTestFlowReport(File targetDirectory, String fileName, String content) {
        try (Writer fileWriter = new FileWriter(new File(targetDirectory, fileName))) {
            fileWriter.append(content);
            fileWriter.flush();
            logger.info("Generated test report: {}{}{}", targetDirectory, File.separator, fileName);
        } catch (IOException e) {
            logger.error("Failed to create test report", e);
        }
    }

    @Override
    public void onTestActionStart(TestCase test, TestAction testAction) {
    }

    @Override
    public void onTestActionFinish(TestCase test, TestAction testAction) {
        String actionPath = getActionPath(test, testAction);
        TestActionResult tar = new TestActionResult(testAction.getName(), actionPath);

        if (testAction instanceof MessageAwareTestAction messageAware) {
            messageAware.getMessage().ifPresent(tar::setMessage);
        }

        ReportEntry reportEntry = flowReport.getOrCreateNew(test.getName());
        reportEntry.addAction(tar);

        if (testAction instanceof TestActionContainer actionContainer) {
            addNestedTestActions(actionPath, actionContainer, tar);
        }
    }

    @Override
    public void onTestActionFailed(TestCase test, TestAction testAction, Throwable cause) {
        String actionPath = getActionPath(test, testAction);
        TestActionResult tar = new TestActionResult(testAction.getName(), actionPath);

        if (testAction instanceof MessageAwareTestAction messageAware) {
            messageAware.getMessage().ifPresent(tar::setMessage);
        }

        ReportEntry reportEntry = flowReport.getOrCreateNew(test.getName());
        reportEntry.addAction(tar);
        tar.setError(Optional.ofNullable(cause.getMessage()).orElse(""));

        if (testAction instanceof TestActionContainer actionContainer) {
            addNestedTestActions(actionPath, actionContainer, tar);
        }
    }

    private void addNestedTestActions(String actionPath, TestActionContainer container, TestActionResult tar) {
        if (container instanceof AbstractIteratingActionContainer iteratingActionContainer) {
            int iterations = iteratingActionContainer.getIterations();
            int actionsSize = iteratingActionContainer.getActions().size();
            for (int iteration = 0; iteration < iterations; iteration++) {
                TestActionResult iterationResult = new TestActionResult(String.valueOf(iteration), actionPath);

                for (int i = iteration * actionsSize; i < container.getExecutedActions().size()
                        && i < ((iteration + 1) * actionsSize); i++) {
                    TestAction action = container.getExecutedActions().get(i);
                    String nestedActionPath;
                    if (iteration == 0) {
                        nestedActionPath = actionPath + "." + getActionPath(container, action);
                    } else {
                        nestedActionPath = actionPath + "." + getActionPath(container, action, iteration * actionsSize);
                    }

                    TestActionResult nested = new TestActionResult(action.getName(), nestedActionPath);
                    if (action instanceof MessageAwareTestAction messageAware) {
                        messageAware.getMessage().ifPresent(nested::setMessage);
                    }
                    iterationResult.addAction(nested);
                    if (action instanceof TestActionContainer actionContainer) {
                        addNestedTestActions(nestedActionPath, actionContainer, nested);
                    }
                }

                tar.addIteration(iterationResult);
            }
        } else {
            for (int i = 0; i < container.getExecutedActions().size(); i++) {
                TestAction action = container.getExecutedActions().get(i);
                TestActionResult nested = new TestActionResult(action.getName(), actionPath + "." + getActionPath(container, action));
                if (action instanceof MessageAwareTestAction messageAware) {
                    messageAware.getMessage().ifPresent(nested::setMessage);
                }
                tar.addAction(nested);
                if (action instanceof TestActionContainer actionContainer) {
                    addNestedTestActions(actionPath + "." + getActionPath(container, action), actionContainer, nested);
                }
            }
        }
    }

    @Override
    public void onTestActionSkipped(TestCase test, TestAction testAction) {
    }

    private static String getActionPath(TestActionContainer container, TestAction testAction) {
        return "actions.%d.%s".formatted(container.getActionIndex(testAction), testAction.getName());
    }

    private static String getActionPath(TestActionContainer container, TestAction testAction, int iterationIndex) {
        return "actions.%d.%s".formatted(container.getActionIndex(testAction) % iterationIndex, testAction.getName());
    }

    @Override
    public void onTestStart(TestCase test) {
        if (flowReport.get(test.getName()).isPresent()) {
            flowReport.reset(test.getName());
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
        if (test.getTestResult() != null) {
            flowReport.getOrCreateNew(test.getName()).setResult(test.getTestResult());
        }
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        if (test.getTestResult() != null) {
            flowReport.getOrCreateNew(test.getName()).setResult(test.getTestResult());
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

    @Override
    public void destroy() {
        clear();
    }

    public void clear() {
        flowReport.clear();
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
            if (!entries.containsKey(name)) {
                entries.put(name, new ReportEntry(name));
            }

            return entries.get(name);
        }

        public void reset(String name) {
            entries.put(name, new ReportEntry(name));;
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

    public static class ReportEntry {
        private final String name;
        private TestResult result;
        private final List<TestActionResult> actions = new ArrayList<>();

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

        public List<TestActionResult> getActions() {
            return actions;
        }

        public void addAction(TestActionResult action) {
            this.actions.add(action);
        }

        public String toJson() {
            return new JsonStringBuilder()
                .withObject()
                    .withProperty("name", name)
                    .withProperty("result").append(Optional.ofNullable(result).map(TestResult::toJson).orElse("{}"))
                    .withProperty("actions")
                        .withArray()
                            .append(actions.stream().map(TestActionResult::toJson).collect(Collectors.joining(",")))
                        .closeArray()
                    .closeObject()
                .toString();
        }

        public String toYaml() {
            return new YamlStringBuilder()
                .withProperty("name", name)
                .withObject("result").append(Optional.ofNullable(result).map(TestResult::toYaml).orElse("{}")).closeObject()
                .withObject("actions")
                    .withArray()
                        .append(actions.stream().map(TestActionResult::toYaml).toList())
                    .closeArray()
                .closeObject()
                .toString();
        }
    }
}
