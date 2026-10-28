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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated("Replaces the JVM-wide environment variables and system properties")
@ExtendWith(SystemStubsExtension.class)
public class TestFlowReporterSettingsTest {

    private static final String HISTORY_PROPERTY = "citrus.test.flow.report.history";
    private static final String HISTORY_ENV = "CITRUS_TEST_FLOW_REPORT_HISTORY";

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @SystemStub
    private SystemProperties systemProperties;

    @Test
    public void isHistoryEnabled_shouldReturnTrueByDefault() {
        systemProperties.remove(HISTORY_PROPERTY);
        environmentVariables.remove(HISTORY_ENV);

        assertThat(TestFlowReporterSettings.isHistoryEnabled())
                .isTrue();
    }

    @Test
    public void isHistoryEnabled_shouldReturnFalse_whenDisabled() {
        systemProperties.set(HISTORY_PROPERTY, "false");

        assertThat(TestFlowReporterSettings.isHistoryEnabled())
                .isFalse();
    }

    @Test
    public void isHistoryEnabled_shouldReturnTrue_whenEnabled() {
        systemProperties.set(HISTORY_PROPERTY, "true");

        assertThat(TestFlowReporterSettings.isHistoryEnabled())
                .isTrue();
    }

    @Test
    public void isHistoryEnabled_shouldReturnEnvVarValue() {
        systemProperties.remove(HISTORY_PROPERTY);
        environmentVariables.set(HISTORY_ENV, "false");

        assertThat(TestFlowReporterSettings.isHistoryEnabled())
                .isFalse();
    }

    @Test
    public void isHistoryEnabled_shouldReturnPropertyValue_overEnvVarValue() {
        systemProperties.set(HISTORY_PROPERTY, "true");

        // disregarded due to resolving sequence
        environmentVariables.set(HISTORY_ENV, "false");

        assertThat(TestFlowReporterSettings.isHistoryEnabled())
                .isTrue();
    }
}
