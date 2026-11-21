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

package org.citrusframework.yaml.main;

import java.util.Collections;

import org.citrusframework.TestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.junit.jupiter.JUnitJupiterEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

@Isolated("Runs a nested JUnit engine on the shared Citrus instance")
class JUnitJupiterEngineMultiTest {

    /**
     * Other unit tests of this module publish a Citrus instance with a mocked context; the engine needs a real one.
     */
    @BeforeEach
    @AfterEach
    void resetCitrus() {
        CitrusExtension.reset();
    }

    @Test
    void shouldReportEachTestOfMultiTestFile() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(TestLoader.YAML, "multi-failing",
                "classpath:org/citrusframework/yaml/main/multi-failing.citrus.it.yaml")));

        JUnitJupiterEngine engine = new JUnitJupiterEngine(configuration);
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        engine.addTestListener(listener);
        engine.run();

        Assertions.assertEquals(3L, listener.getSummary().getTestsFoundCount());
        Assertions.assertEquals(2L, listener.getSummary().getTestsSucceededCount());
        Assertions.assertEquals(1L, listener.getSummary().getTestsFailedCount());
    }
}
