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

import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.ResourceTemplateArg;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MCP Resources exposing Citrus system property and environment variable settings.
 */
@ApplicationScoped
public class SettingsResources {

    @Inject
    SettingsData settingsData;

    /**
     * Resource provides access to all Citrus settings grouped by category.
     */
    @Resource(uri = "citrus://settings/all",
            name = "citrus_settings_all",
            title = "All Citrus Settings",
            description = "Retrieves a catalog of all available Citrus system property and environment variable settings, " +
                    "grouped by category. Each setting includes the system property key, environment variable name, " +
                    "default value, value type, and description.",
            mimeType = "application/json")
    public TextResourceContents citrus_settings_all() {
        String uri = "citrus://settings/all";

        try {
            return new TextResourceContents(uri,
                    JsonSupport.json().writeValueAsString(settingsData.getGroups()), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Resource provides access to Citrus settings for a specific group.
     */
    @ResourceTemplate(uriTemplate = "citrus://settings/group/{name}",
            name = "citrus_settings_group",
            title = "Citrus Settings Group",
            description = "Retrieves Citrus system property and environment variable settings for a specific group " +
                    "(e.g., core, logging, camel, kubernetes, testcontainers). " +
                    "Each setting includes the system property key, environment variable name, " +
                    "default value, value type, and description.",
            mimeType = "application/json")
    public TextResourceContents citrus_settings_group(@ResourceTemplateArg(name = "name") String name) {
        String uri = "citrus://settings/group/" + name;

        if (name == null || name.isBlank()) {
            return error(uri, "Group name is required. Available groups: " + settingsData.getGroupNames());
        }

        try {
            SettingsData.SettingsGroup group = settingsData.getGroup(name);
            if (group == null) {
                return error(uri, "Settings group not found: " + name +
                        ". Available groups: " + settingsData.getGroupNames());
            }

            return new TextResourceContents(uri,
                    JsonSupport.json().writeValueAsString(group), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Resource provides access to Citrus settings for a specific module.
     */
    @ResourceTemplate(uriTemplate = "citrus://settings/module/{module}",
            name = "citrus_settings_module",
            title = "Citrus Settings by Module",
            description = "Retrieves Citrus system property and environment variable settings for a specific module " +
                    "(e.g., citrus-api, citrus-camel, citrus-kubernetes, citrus-testcontainers). " +
                    "A module may contain multiple setting groups. " +
                    "Each setting includes the system property key, environment variable name, " +
                    "default value, value type, and description.",
            mimeType = "application/json")
    public TextResourceContents citrus_settings_module(@ResourceTemplateArg(name = "module") String module) {
        String uri = "citrus://settings/module/" + module;

        if (module == null || module.isBlank()) {
            return error(uri, "Module name is required. Available modules: " + settingsData.getModuleNames());
        }

        try {
            java.util.List<SettingsData.SettingsGroup> groups = settingsData.getGroupsByModule(module);
            if (groups.isEmpty()) {
                return error(uri, "No settings found for module: " + module +
                        ". Available modules: " + settingsData.getModuleNames());
            }

            return new TextResourceContents(uri,
                    JsonSupport.json().writeValueAsString(groups), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    private TextResourceContents error(String uri, String message) {
        return new TextResourceContents(uri, "{ \"error\": \"%s\" }".formatted(message), "application/json");
    }
}
