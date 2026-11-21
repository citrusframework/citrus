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

import java.util.Collections;
import java.util.List;

import org.citrusframework.TestSource;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.junit.jupiter.source.FakeMultiTestLoader;
import org.citrusframework.junit.jupiter.source.MultiTestFactoryFixture;
import org.citrusframework.junit.jupiter.source.MultiTestParallelFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

@Isolated("Runs nested JUnit engines and changes JUnit parallel system properties")
class CitrusTestFactorySupportTest {

    @BeforeEach
    void resetFakeLoader() {
        FakeMultiTestLoader.reset();
    }

    @Test
    void shouldExpandMultiTestSourcesIntoContainers() {
        TestExecutionSummary summary = run(MultiTestFactoryFixture.class);

        // multi-failing-disabled: 1 passed, 1 failed, 1 skipped; single: passed; broken: failed; multi: 3 passed
        Assertions.assertEquals(5, summary.getTestsSucceededCount());
        Assertions.assertEquals(2, summary.getTestsFailedCount());
        Assertions.assertEquals(1, summary.getTestsAbortedCount());

        List<String> events = FakeMultiTestLoader.getEvents();
        Assertions.assertTrue(events.contains("single:single"));
        Assertions.assertEquals(1, Collections.frequency(events, "close:multi"));
        Assertions.assertEquals(1, Collections.frequency(events, "close:multi-failing-disabled"));
        Assertions.assertTrue(events.indexOf("close:multi") > events.indexOf("end:multi:third"),
                "afterAll must run after the last test: " + events);
        Assertions.assertFalse(events.contains("start:multi-failing-disabled:third"));
    }

    @Test
    void shouldRunTestsOfOneSourceSequentiallyInParallelMode() {
        String enabled = System.getProperty("junit.jupiter.execution.parallel.enabled");
        String mode = System.getProperty("junit.jupiter.execution.parallel.mode.default");
        try {
            System.setProperty("junit.jupiter.execution.parallel.enabled", "true");
            System.setProperty("junit.jupiter.execution.parallel.mode.default", "concurrent");

            TestExecutionSummary summary = run(MultiTestParallelFixture.class);
            Assertions.assertEquals(6, summary.getTestsSucceededCount());
        } finally {
            restore("junit.jupiter.execution.parallel.enabled", enabled);
            restore("junit.jupiter.execution.parallel.mode.default", mode);
        }

        List<String> events = FakeMultiTestLoader.getEvents();
        for (String source : List.of("multi-slow-a", "multi-slow-b")) {
            List<String> ofSource = events.stream().filter(event -> event.contains(source)).toList();
            Assertions.assertEquals(List.of(
                    "start:%s:first".formatted(source), "end:%s:first".formatted(source),
                    "start:%s:second".formatted(source), "end:%s:second".formatted(source),
                    "start:%s:third".formatted(source), "end:%s:third".formatted(source),
                    "close:%s".formatted(source)), ofSource);
        }
    }

    private static TestExecutionSummary run(Class<?> fixture) {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(fixture)));

        JUnitJupiterEngine engine = new JUnitJupiterEngine(configuration);
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        engine.addTestListener(listener);
        engine.run();
        return listener.getSummary();
    }

    private static void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
