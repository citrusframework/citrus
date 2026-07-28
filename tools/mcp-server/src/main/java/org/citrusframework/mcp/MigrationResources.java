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

import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.ResourceTemplateArg;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MigrationResources {

    @Inject
    MigrationData migrationData;

    @ResourceTemplate(uriTemplate = "citrus://docs/migration-guide/{version}",
            name = "citrus_docs_migration_guide",
            title = "Citrus Migration Guide",
            description = "Retrieves the Citrus version migration guide for a given target version (e.g. 5.0). " +
                    "Returns structured JSON with package renames, Maven dependency changes, API changes, and SPI changes " +
                    "to assist with automated migration from the previous major version.",
            mimeType = "application/json")
    public TextResourceContents citrus_docs_migration_guide(@ResourceTemplateArg(name = "version") String version) {
        String uri = "citrus://docs/migration-guide/" + version;

        if (version == null || version.isBlank()) {
            return error(uri, "Version is required. Available versions: " + migrationData.getAvailableVersions());
        }

        try {
            MigrationData.MigrationGuide guide = migrationData.getGuide(version);
            if (guide == null) {
                return error(uri, "Migration guide not found for version: " + version +
                        ". Available versions: " + migrationData.getAvailableVersions());
            }

            return new TextResourceContents(uri,
                    JsonSupport.json().writeValueAsString(guide), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    private TextResourceContents error(String uri, String message) {
        return new TextResourceContents(uri, "{ \"error\": \"%s\" }".formatted(message), "application/json");
    }
}
