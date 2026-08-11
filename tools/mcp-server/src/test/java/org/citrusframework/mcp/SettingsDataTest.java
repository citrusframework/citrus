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

package org.citrusframework.mcp;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsDataTest {

    private final SettingsData settingsData = new SettingsData();

    @Test
    void shouldHaveAllGroups() {
        List<String> groupNames = settingsData.getGroupNames();

        assertThat(groupNames).contains(
                "core", "logging", "reporting", "spring", "agent",
                "camel", "camel-cli", "camel-infra",
                "http", "ftp", "mail", "openapi",
                "json", "yaml-validation",
                "jbang", "kubernetes", "knative",
                "kafka",
                "testcontainers", "testcontainers-kafka",
                "testcontainers-localstack", "testcontainers-postgresql",
                "testcontainers-mongodb", "testcontainers-redpanda");
    }

    @Test
    void shouldGetGroupByName() {
        SettingsData.SettingsGroup group = settingsData.getGroup("core");

        assertThat(group).isNotNull();
        assertThat(group.name()).isEqualTo("core");
        assertThat(group.title()).isEqualTo("Core Settings");
        assertThat(group.module()).isEqualTo("citrus-api");
        assertThat(group.settings()).isNotEmpty();
    }

    @Test
    void shouldReturnNullForUnknownGroup() {
        assertThat(settingsData.getGroup("unknown")).isNull();
    }

    @Test
    void shouldGetAllSettings() {
        List<SettingsData.SettingEntry> allSettings = settingsData.getAllSettings();

        assertThat(allSettings).isNotEmpty();
        assertThat(allSettings.size()).isGreaterThan(100);
    }

    @Test
    void shouldContainCoreSettings() {
        SettingsData.SettingsGroup core = settingsData.getGroup("core");

        assertThat(core.settings()).isNotEmpty();
        assertThat(core.settings()).anySatisfy(entry -> {
            assertThat(entry.property()).isEqualTo("citrus.default.message.type");
            assertThat(entry.envVariable()).isEqualTo("CITRUS_DEFAULT_MESSAGE_TYPE");
            assertThat(entry.defaultValue()).isEqualTo("XML");
            assertThat(entry.type()).isEqualTo("string");
        });
    }

    @Test
    void shouldContainLoggingSettings() {
        SettingsData.SettingsGroup logging = settingsData.getGroup("logging");

        assertThat(logging.settings()).isNotEmpty();
        assertThat(logging.settings()).anySatisfy(entry -> {
            assertThat(entry.property()).isEqualTo("citrus.logger.mask.keywords");
            assertThat(entry.envVariable()).isEqualTo("CITRUS_LOG_MASK_KEYWORDS");
            assertThat(entry.defaultValue()).isEqualTo("password,secret,secretKey");
        });
    }

    @Test
    void shouldContainKubernetesSettings() {
        SettingsData.SettingsGroup k8s = settingsData.getGroup("kubernetes");

        assertThat(k8s).isNotNull();
        assertThat(k8s.module()).isEqualTo("citrus-kubernetes");
        assertThat(k8s.settings()).anySatisfy(entry -> {
            assertThat(entry.property()).isEqualTo("citrus.kubernetes.namespace");
            assertThat(entry.envVariable()).isEqualTo("CITRUS_KUBERNETES_NAMESPACE");
        });
    }

    @Test
    void shouldContainTestcontainersSettings() {
        SettingsData.SettingsGroup tc = settingsData.getGroup("testcontainers");

        assertThat(tc).isNotNull();
        assertThat(tc.module()).isEqualTo("citrus-testcontainers");
        assertThat(tc.settings()).anySatisfy(entry -> {
            assertThat(entry.property()).isEqualTo("citrus.testcontainers.enabled");
            assertThat(entry.envVariable()).isEqualTo("CITRUS_TESTCONTAINERS_ENABLED");
            assertThat(entry.defaultValue()).isEqualTo("true");
            assertThat(entry.type()).isEqualTo("boolean");
        });
    }

    @Test
    void shouldGetModuleNames() {
        List<String> moduleNames = settingsData.getModuleNames();

        assertThat(moduleNames).contains(
                "citrus-api", "citrus-spring", "citrus-camel",
                "citrus-http", "citrus-kafka", "citrus-kubernetes", "citrus-testcontainers");
    }

    @Test
    void shouldGetGroupsByModule() {
        List<SettingsData.SettingsGroup> groups = settingsData.getGroupsByModule("citrus-camel");

        assertThat(groups).hasSize(3);
        assertThat(groups).extracting(SettingsData.SettingsGroup::name)
                .containsExactlyInAnyOrder("camel", "camel-cli", "camel-infra");
    }

    @Test
    void shouldReturnEmptyListForUnknownModule() {
        assertThat(settingsData.getGroupsByModule("unknown")).isEmpty();
    }

    @Test
    void shouldHaveConsistentSettingEntryStructure() {
        Map<String, SettingsData.SettingsGroup> groups = settingsData.getGroups();

        for (SettingsData.SettingsGroup group : groups.values()) {
            assertThat(group.name()).isNotBlank();
            assertThat(group.title()).isNotBlank();
            assertThat(group.description()).isNotBlank();
            assertThat(group.module()).isNotBlank();
            assertThat(group.settings()).isNotEmpty();

            for (SettingsData.SettingEntry entry : group.settings()) {
                assertThat(entry.property()).as("property in group %s", group.name()).isNotBlank();
                assertThat(entry.envVariable()).as("envVariable in group %s", group.name()).isNotBlank();
                assertThat(entry.type()).as("type for %s", entry.property()).isNotBlank();
                assertThat(entry.description()).as("description for %s", entry.property()).isNotBlank();
            }
        }
    }
}
