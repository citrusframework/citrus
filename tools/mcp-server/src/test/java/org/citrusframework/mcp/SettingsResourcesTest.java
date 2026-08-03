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

import io.quarkiverse.mcp.server.TextResourceContents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsResourcesTest {

    private SettingsResources createResources() {
        SettingsResources resources = new SettingsResources();
        resources.settingsData = new SettingsData();
        return resources;
    }

    @Test
    void shouldGetAllSettings() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_all();

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://settings/all");
        assertThat(result.text()).contains("\"core\"");
        assertThat(result.text()).contains("\"logging\"");
        assertThat(result.text()).contains("\"kubernetes\"");
        assertThat(result.text()).contains("\"testcontainers\"");
        assertThat(result.text()).contains("citrus.default.message.type");
        assertThat(result.text()).contains("CITRUS_DEFAULT_MESSAGE_TYPE");
    }

    @Test
    void shouldGetSettingsGroup() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_group("core");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://settings/group/core");
        assertThat(result.text()).contains("\"name\":\"core\"");
        assertThat(result.text()).contains("\"title\":\"Core Settings\"");
        assertThat(result.text()).contains("citrus.default.message.type");
    }

    @Test
    void shouldReturnErrorForUnknownGroup() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_group("unknown");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://settings/group/unknown");
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("Settings group not found");
    }

    @Test
    void shouldReturnErrorForBlankGroupName() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_group("");

        assertThat(result).isNotNull();
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("Group name is required");
    }

    @Test
    void shouldGetSettingsByModule() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_module("citrus-camel");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://settings/module/citrus-camel");
        assertThat(result.text()).contains("\"name\":\"camel\"");
        assertThat(result.text()).contains("\"name\":\"camel-cli\"");
        assertThat(result.text()).contains("\"name\":\"camel-infra\"");
    }

    @Test
    void shouldReturnErrorForUnknownModule() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_module("unknown-module");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://settings/module/unknown-module");
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("No settings found for module");
    }

    @Test
    void shouldReturnErrorForBlankModuleName() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_module("");

        assertThat(result).isNotNull();
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("Module name is required");
    }

    @Test
    void shouldContainSettingDetails() {
        SettingsResources resources = createResources();

        TextResourceContents result = resources.citrus_settings_group("logging");

        assertThat(result.text()).contains("\"property\"");
        assertThat(result.text()).contains("\"envVariable\"");
        assertThat(result.text()).contains("\"defaultValue\"");
        assertThat(result.text()).contains("\"type\"");
        assertThat(result.text()).contains("\"description\"");
        assertThat(result.text()).contains("citrus.logger.mask.keywords");
        assertThat(result.text()).contains("CITRUS_LOG_MASK_KEYWORDS");
    }
}
