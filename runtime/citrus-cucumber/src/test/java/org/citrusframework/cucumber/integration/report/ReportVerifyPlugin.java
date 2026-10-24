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

package org.citrusframework.cucumber.integration.report;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import org.citrusframework.cucumber.CucumberSettings;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public class ReportVerifyPlugin implements EventListener {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ReportVerifyPlugin.class);

    @Override
    @SuppressWarnings("unchecked")
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunFinished.class, event -> {
            try {
                Assertions.assertTrue(Files.exists(CucumberSettings.getTerminationLog()), "Verify termination log exists");
                List<String> lines = Files.readAllLines(CucumberSettings.getTerminationLog());
                Assertions.assertEquals(1L, lines.size());

                Map<String, Object> testResults = JsonMapper.shared().readValue(lines.get(0), new TypeReference<Map<String, Object>>() {
                });
                Assertions.assertEquals("Test reporter", testResults.get("suiteName"));

                Map<String, Object> summary = (Map<String, Object>) testResults.get("summary");
                Assertions.assertEquals(1L, numberValue(summary, "passed"));
                Assertions.assertEquals(0L, numberValue(summary, "failed"));
                Assertions.assertEquals(0L, numberValue(summary, "errors"));
                Assertions.assertEquals(0L, numberValue(summary, "skipped"));
                Assertions.assertEquals(0L, numberValue(summary, "pending"));
                Assertions.assertEquals(0L, numberValue(summary, "undefined"));
                Assertions.assertEquals(1L, numberValue(summary, "total"));

                List<Map<String, Object>> tests = (List<Map<String, Object>>) testResults.get("tests");
                Assertions.assertEquals(1, tests.size());
                Assertions.assertEquals("Success test", tests.get(0).get("name"));
                Assertions.assertEquals("report.feature:3", tests.get(0).get("classname"));
            } catch (IOException e) {
                LOG.warn("Failed to verify termination logs", e);
                Assertions.fail(e.getMessage());
            }
        });
    }

    private static long numberValue(Map<String, Object> summary, String key) {
        Object value = summary.get(key);
        Assertions.assertInstanceOf(Number.class, value, () -> "Summary value '" + key + "' is missing");
        return ((Number) value).longValue();
    }
}
